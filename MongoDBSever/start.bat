@echo off
echo ====================================
echo   KHOI DONG MONGODB SERVER
echo ====================================
echo.
echo Dang kiem tra MongoDB...
echo.

REM Kiểm tra MongoDB có đang chạy không
tasklist /FI "IMAGENAME eq mongod.exe" 2>NUL | find /I /N "mongod.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [OK] MongoDB da dang chay
) else (
    echo [WARNING] MongoDB chua chay. Vui long khoi dong MongoDB truoc.
    echo Ban co muon khoi dong MongoDB ngay bay gio? (Y/N)
    set /p startMongo=
    if /i "%startMongo%"=="Y" (
        echo Dang khoi dong MongoDB...
        net start MongoDB
        if errorlevel 1 (
            echo [ERROR] Khong the khoi dong MongoDB. Vui long khoi dong thu cong.
            pause
            exit /b 1
        )
    ) else (
        echo Vui long khoi dong MongoDB truoc khi chay server.
        pause
        exit /b 1
    )
)

echo.
echo Dang khoi dong Node.js Server...
echo Server se chay tren: http://localhost:3000
echo.
echo Nhan Ctrl+C de dung server
echo.

node ./bin/www

pause

