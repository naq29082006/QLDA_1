#!/bin/bash

echo "===================================="
echo "  KHOI DONG MONGODB SERVER"
echo "===================================="
echo ""
echo "Dang kiem tra MongoDB..."
echo ""

# Kiểm tra MongoDB có đang chạy không
if pgrep -x "mongod" > /dev/null; then
    echo "[OK] MongoDB da dang chay"
else
    echo "[WARNING] MongoDB chua chay. Dang thu khoi dong..."
    
    # Thử khởi động MongoDB
    if command -v systemctl &> /dev/null; then
        sudo systemctl start mongod
    elif command -v service &> /dev/null; then
        sudo service mongod start
    else
        echo "[ERROR] Khong the khoi dong MongoDB. Vui long khoi dong thu cong."
        exit 1
    fi
    
    sleep 2
    
    if pgrep -x "mongod" > /dev/null; then
        echo "[OK] MongoDB da duoc khoi dong"
    else
        echo "[ERROR] Khong the khoi dong MongoDB. Vui long khoi dong thu cong."
        exit 1
    fi
fi

echo ""
echo "Dang khoi dong Node.js Server..."
echo "Server se chay tren: http://localhost:3000"
echo ""
echo "Nhan Ctrl+C de dung server"
echo ""

node ./bin/www

