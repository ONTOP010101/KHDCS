@echo off
chcp 65001 >nul 2>&1
set JAVA_HOME=C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot
set Path=%JAVA_HOME%\bin;%Path%

echo ============================================
echo   拍照管理系统 - 完整启动
echo ============================================
echo.

echo [1/2] 启动后端服务...
cd /d "%~dp0backend"
start "后端服务" cmd /k "call .mvn\wrapper\bin\mvn.cmd spring-boot:run"

echo [2/2] 启动前端 (Electron)...
cd /d "%~dp0frontend"
call npx vite

pause
