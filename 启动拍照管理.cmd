@echo off
chcp 65001 >nul
title 拍照管理系统

cd /d "%~dp0backend\target"

if not exist "photo-management-1.0.0.jar" (
    echo [错误] 找不到后端文件，请先执行 mvn package
    pause
    exit /b 1
)

echo 正在启动拍照管理系统...
echo.

start "" java -jar photo-management-1.0.0.jar --server.port=8080

echo 等待服务启动...
:wait
timeout /t 2 /nobreak >nul
curl -s -o nul http://localhost:8080 && goto open
goto wait

:open
start http://localhost:8080
echo 系统已启动！
exit
