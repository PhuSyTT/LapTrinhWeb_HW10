# BÀI TẬP: XÁC THỰC VÀ PHÂN QUYỀN JWT VỚI SPRING BOOT 3 & SPRING SECURITY 6
## SỬ DỤNG THƯ VIỆN NIMBUS JOSE + JWT THAY THẾ JJWT

---

## 1. Giới thiệu tổng quan
Dự án được xây dựng theo bài giảng **Demo JWT với Spring Boot 3 – Security 6** (tài liệu `04_JWT.pdf`), trong đó thư viện xử lý token mặc định **JJWT (`io.jsonwebtoken`)** được thay thế hoàn toàn bằng **Nimbus JOSE + JWT (`com.nimbusds:nimbus-jose-jwt`)** chuẩn RFC 7515, 7516, 7517, 7518, 7519.

### Điểm nổi bật khi chuyển sang Nimbus JOSE + JWT:
* **Chuẩn hóa RFC**: Hỗ trợ đầy đủ các chuẩn JOSE (JWS - Chữ ký số, JWE - Mã hóa, JWK/JWKS - Biểu diễn khóa).
* **Hiệu năng & Bảo mật cao**: Được sử dụng chính thức bên dưới Spring Security 6 Resource Server, OAuth2 và OpenID Connect.
* **Tương thích hoàn toàn API**: `JwtService` giữ nguyên các method chuẩn (`generateToken`, `isTokenValid`, `extractUsername`, `extractExpiration`,...) giúp các tầng Controller, Filter và Exception Handler hoạt động liền mạch.

---

## 2. So sánh thay đổi giữa JJWT và Nimbus JOSE + JWT

| Thành phần | Phiên bản cũ (JJWT) | Phiên bản mới (Nimbus JOSE + JWT) |
| :--- | :--- | :--- |
| **Dependency** | `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` | `com.nimbusds:nimbus-jose-jwt` (v9.37.3) |
| **Tạo Claims** | `Jwts.builder().claims(map).subject(...)` | `new JWTClaimsSet.Builder().subject(...).build()` |
| **Header** | Thiết lập ngầm định hoặc qua `Jwts.header()` | `new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build()` |
| **Ký Token** | `.signWith(key, Jwts.SIG.HS256).compact()` | `JWSSigner signer = new MACSigner(secretKey); signedJWT.sign(signer); signedJWT.serialize()` |
| **Xác thực chữ ký** | `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | `SignedJWT signedJWT = SignedJWT.parse(token); JWSVerifier verifier = new MACVerifier(secretKey); signedJWT.verify(verifier)` |
| **Bắt ngoại lệ** | `SignatureException`, `ExpiredJwtException` | `JOSEException`, `ParseException`, `BadJOSEException` |

---

## 3. Cấu trúc thư mục dự án

```
JWT_springboot3/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/vn/iotstar/
│   │   │   ├── Jwtspringboot3Application.java    # Main Spring Boot App & Data Seeder
│   │   │   ├── configs/
│   │   │   │   ├── ApplicationConfiguration.java # Beans: PasswordEncoder, UserDetailsService, AuthManager
│   │   │   │   └── SecurityConfiguration.java    # SecurityFilterChain, Stateless, CORS, Filter registration
│   │   │   ├── controllers/
│   │   │   │   ├── AuthenticationController.java # REST API: /auth/signup, /auth/login
│   │   │   │   ├── UserController.java           # REST API: /users/me, /users
│   │   │   │   └── AuthController.java           # View Controller: /login, /user/profile
│   │   │   ├── entity/
│   │   │   │   └── User.java                     # Entity User implements UserDetails
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java   # @RestControllerAdvice xử lý lỗi JWT & Authentication
│   │   │   ├── filter/
│   │   │   │   └── JwtAuthenticationFilter.java  # OncePerRequestFilter xác thực Bearer Token
│   │   │   ├── models/
│   │   │   │   ├── LoginResponse.java            # DTO trả về token và thời gian hết hạn
│   │   │   │   ├── LoginUserModel.java           # DTO nhận email & password đăng nhập
│   │   │   │   └── RegisterUserModel.java        # DTO nhận email, password, fullName đăng ký
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java           # Spring Data JPA Repository
│   │   │   └── services/
│   │   │       ├── AuthenticationService.java    # Xử lý nghiệp vụ đăng ký và đăng nhập
│   │   │       ├── JwtService.java               # Xử lý sinh/giải mã/xác thực Token bằng Nimbus
│   │   │       └── UserService.java              # Xử lý lấy danh sách người dùng
│   │   └── resources/
│   │       ├── application.properties            # Cấu hình kết nối MySQL và JWT Secret
│   │       ├── application-h2.properties         # Cấu hình H2 Database In-Memory test nhanh
│   │       ├── static/
│   │       │   ├── images/                       # Thư mục hình ảnh avatar
│   │       │   └── js/
│   │       │       └── mainjs.js                 # Javascript xử lý AJAX, LocalStorage token
│   │       └── templates/
│   │           ├── login.html                    # Giao diện Đăng nhập / Đăng ký
│   │           └── profile.html                  # Giao diện Trang cá nhân hiển thị token & API
│   └── test/
│       ├── java/vn/iotstar/
│       │   ├── JwtServiceTest.java               # 5 Unit Tests cho Nimbus JOSE + JWT
│       │   └── AuthControllerIntegrationTest.java# 5 Integration Tests cho luồng Signup/Login/Protected API
│       └── resources/
│           └── application.properties            # Cấu hình H2 Test cho automated test
```

---

## 4. Cấu hình cơ sở dữ liệu

### Cấu hình mặc định (`src/main/resources/application.properties`):
Theo slide 21 của bài giảng:
```properties
spring.application.name=JWT_springboot3
server.port=8005

spring.datasource.url=jdbc:mysql://localhost:3306/jwt_springboot3?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=1234567@a$
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=true

security.jwt.secret-key=3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b
security.jwt.expiration-time=3600000
```

> **Lưu ý**: Dự án đã có sẵn profile `h2` (`application-h2.properties`). Nếu chưa bật MySQL, bạn có thể chạy ngay với lệnh:
> `mvn spring-boot:run -Dspring-boot.run.profiles=h2`

---

## 5. Hướng dẫn chạy và kiểm thử

### Cách 1: Chạy kiểm thử tự động (Unit & Integration Tests)
```bash
cd "d:\LT WEB\HW10\JWT_springboot3"
mvn test
```
*Kết quả: Toàn bộ 10/10 test case đều PASS thành công!*

### Cách 2: Khởi động ứng dụng
```bash
cd "d:\LT WEB\HW10\JWT_springboot3"
mvn spring-boot:run
```
(Hoặc chạy file `Jwtspringboot3Application.java` trong STS/Eclipse/IntelliJ).

Sau khi ứng dụng khởi động thành công tại cổng **8005**, hệ thống sẽ tự động khởi tạo sẵn tài khoản quản trị viên demo:
* **Email**: `trungnh@hcmute.edu.vn`
* **Mật khẩu**: `123456`
* **Họ tên**: `Nguyễn Hữu Trung`

---

## 6. Hướng dẫn kiểm thử qua Giao diện Web (AJAX + Thymeleaf)

1. Mở trình duyệt web truy cập: **`http://localhost:8005/login`**
2. Nhập thông tin đăng nhập:
   * Email: `trungnh@hcmute.edu.vn`
   * Password: `123456`
3. Nhấn nút **Login**:
   * AJAX sẽ gửi yêu cầu `POST /auth/login` đến máy chủ.
   * Nimbus JOSE + JWT sẽ tạo chuỗi token và trả về client.
   * Client lưu `token` vào `localStorage.token` và chuyển hướng sang trang **`http://localhost:8005/user/profile`**.
4. Tại trang Profile:
   * JavaScript tự động gắn token vào header: `Authorization: Bearer <token>` để gọi `GET /users/me`.
   * Thông tin người dùng (Họ tên, Email, ID, Avatar) và chuỗi token Nimbus JWT sẽ hiển thị trực quan trên giao diện.
5. Nhấn nút **Logout**:
   * Token trong LocalStorage bị xóa và điều hướng trở lại màn hình đăng nhập.

---

## 7. Hướng dẫn kiểm thử qua Postman / cURL

### 1. Đăng ký tài khoản mới:
* **URL**: `POST http://localhost:8005/auth/signup`
* **Headers**: `Content-Type: application/json`
* **Body (raw JSON)**:
```json
{
  "email": "student@hcmute.edu.vn",
  "password": "mypassword123",
  "fullName": "Sinh Viên HCMUTE"
}
```

### 2. Đăng nhập để lấy Nimbus JWT Token:
* **URL**: `POST http://localhost:8005/auth/login`
* **Headers**: `Content-Type: application/json`
* **Body (raw JSON)**:
```json
{
  "email": "trungnh@hcmute.edu.vn",
  "password": "123456"
}
```
* **Response (JSON)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600000
}
```

### 3. Gọi API bảo vệ `/users/me`:
* **URL**: `GET http://localhost:8005/users/me`
* **Headers**:
  * `Authorization`: `Bearer <token_nhan_duoc_o_buoc_2>`

### 4. Gọi API bảo vệ lấy danh sách người dùng `/users`:
* **URL**: `GET http://localhost:8005/users`
* **Headers**:
  * `Authorization`: `Bearer <token_nhan_duoc_o_buoc_2>`
