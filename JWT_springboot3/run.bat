@echo off
title Spring Boot 3 - Nimbus JWT Server
cd /d "%~dp0"
echo ====================================================================
echo  DANG KHOI DONG HE THONG XAC THUC NIMBUS JWT (PORT 8005)
echo ====================================================================
echo  - Trang dang nhap:  http://localhost:8005/login
echo  - Trang Profile:    http://localhost:8005/user/profile
echo  - H2 Console:       http://localhost:8005/h2-console
echo  - Tai khoan mau:    trungnh@hcmute.edu.vn / 123456
echo ====================================================================
mvn spring-boot:run
pause
