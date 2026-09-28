$(document).ready(function() {

    // Nếu đang ở trang profile, tải thông tin người dùng từ /users/me
    if (window.location.pathname.includes('/user/profile') || window.location.pathname.includes('/profile')) {
        if (!localStorage.token) {
            alert("Bạn chưa đăng nhập! Đang chuyển về trang đăng nhập.");
            window.location.href = "/login";
            return;
        }

        // Hiển thị token trong hộp debug
        if ($('#tokenBox').length) {
            $('#tokenBox').text(localStorage.token);
        }

        // Gọi API /users/me với Bearer Token
        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            beforeSend: function(xhr) {
                if (localStorage.token) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
                }
            },
            success: function(data) {
                console.log("SUCCESS : ", data);
                // Cập nhật giao diện theo đúng slide 33
                $('#profile').html(data.fullName);
                var avatarSrc = data.images && data.images.trim() !== '' ? data.images : '/images/avatar.png';
                $('#images').attr('src', avatarSrc);

                if ($('#userEmail').length) {
                    $('#userEmail').html('<i class="bi bi-envelope me-1"></i> ' + data.email);
                }
                if ($('#userId').length) {
                    $('#userId').text(data.id);
                }
                if ($('#userCreatedAt').length && data.createdAt) {
                    $('#userCreatedAt').text(new Date(data.createdAt).toLocaleString());
                }
            },
            error: function(e) {
                var json = e.responseText;
                if ($('#feedback').length) {
                    $('#feedback').html("Lỗi xác thực: " + json);
                }
                console.log("ERROR : ", e);
                alert("Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại!");
                localStorage.clear();
                window.location.href = "/login";
            }
        });
    }

    // Hàm đăng xuất (theo đúng slide 33)
    $('#logout').click(function() {
        localStorage.clear();
        window.location.href = "/login";
    });

    // Hàm Login (theo đúng slide 33)
    $('#login').click(function() {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;

        if (!email || !password) {
            alert("Vui lòng nhập đầy đủ Email và Mật khẩu!");
            return;
        }

        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/login",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: basicInfo,
            success: function(data) {
                console.log("Got a token from the server: " + data.token);
                localStorage.token = data.token;
                window.location.href = "/user/profile";
            },
            error: function(xhr) {
                console.error("Login Failed:", xhr);
                var msg = "Login Failed";
                if (xhr.responseJSON && xhr.responseJSON.description) {
                    msg += ": " + xhr.responseJSON.description;
                }
                alert(msg);
            }
        });
    });

    // Hàm Đăng ký tài khoản (Register)
    $('#btnRegister').click(function() {
        var fullName = $('#regFullName').val();
        var email = $('#regEmail').val();
        var password = $('#regPassword').val();

        if (!fullName || !email || !password) {
            alert("Vui lòng điền đầy đủ các thông tin đăng ký!");
            return;
        }

        var regData = JSON.stringify({
            fullName: fullName,
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/signup",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: regData,
            success: function(data) {
                alert("Đăng ký thành công tài khoản: " + data.email + "! Vui lòng đăng nhập.");
                $('#email').val(data.email);
                $('#password').val(password);
                $('#login-tab').tab('show');
            },
            error: function(xhr) {
                console.error("Register error:", xhr);
                alert("Đăng ký thất bại! Email có thể đã tồn tại.");
            }
        });
    });

});
