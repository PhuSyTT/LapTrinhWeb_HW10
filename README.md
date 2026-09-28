# HƯỚNG DẪN BÀI TẬP TUẦN 10: XÁC THỰC JWT VỚI THƯ VIỆN NIMBUS JOSE + JWT

Dự án đã được triển khai hoàn chỉnh theo yêu cầu của bài giảng JWT (`04_JWT.pdf`) và slide hướng dẫn thư viện Nimbus JOSE + JWT (`image (1).png`).

## 1. Mã nguồn dự án
* Thư mục dự án: [`d:/LT WEB/HW10/JWT_springboot3`](file:///d:/LT%20WEB/HW10/JWT_springboot3)
* Tài liệu chi tiết: [`d:/LT WEB/HW10/JWT_springboot3/README.md`](file:///d:/LT%20WEB/HW10/JWT_springboot3/README.md)

## 2. Điểm cải tiến chính
Thư viện **JJWT (`io.jsonwebtoken`)** trong bài giảng gốc đã được thay thế thành công bằng **Nimbus JOSE + JWT (`com.nimbusds:nimbus-jose-jwt:9.37.3`)**:
* Sử dụng `MACSigner` & `MACVerifier` để ký và xác thực token HMAC-SHA256.
* Sử dụng `JWTClaimsSet.Builder` và `SignedJWT` để sinh token đúng chuẩn RFC 7515 & RFC 7519.
* Tích hợp hoàn hảo với **Spring Security 6** và **Spring Boot 3.3.4**.
* Đầy đủ giao diện web Thymeleaf + AJAX (`/login`, `/user/profile`) theo slide 32 - 34.
* Đã vượt qua 10/10 automated tests (Unit test & MockMvc Integration test).

## 3. Lệnh chạy nhanh kiểm thử
```bash
cd "d:\LT WEB\HW10\JWT_springboot3"
mvn test
```

## 4. Khởi động ứng dụng
```bash
cd "d:\LT WEB\HW10\JWT_springboot3"
mvn spring-boot:run
```
Truy cập: `http://localhost:8005/login`
Tài khoản mẫu đã tạo sẵn: `trungnh@hcmute.edu.vn` / `123456`
