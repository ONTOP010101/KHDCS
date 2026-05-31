@echo off
chcp 65001 >nul
echo ========================================
echo   拍照管理系统 - 一键构建
echo ========================================
echo.

set MVN=d:\客户端测试\apache-maven-3.9.6\bin\mvn.cmd
set BACKEND_DIR=d:\客户端测试\backend
set ELECTRON_DIR=d:\客户端测试\electron
set JAR=%BACKEND_DIR%\target\photo-management-1.0.0.jar
set JRE_DIR=%ELECTRON_DIR%\jre

echo [1/3] 编译后端...
call %MVN% -f %BACKEND_DIR%\pom.xml package -DskipTests -q
if %errorlevel% neq 0 (
    echo [错误] 后端编译失败
    pause
    exit /b 1
)
echo       完成

echo [2/3] 打包 Electron .exe...
rd /s /q "%ELECTRON_DIR%\dist" 2>nul
call npx electron-builder --win portable
if %errorlevel% neq 0 (
    echo [错误] Electron 打包失败
    pause
    exit /b 1
)
echo       完成

echo [3/3] 复制后端和 JRE 到安装包...
if not exist "%ELECTRON_DIR%\dist\win-unpacked\resources\backend" mkdir "%ELECTRON_DIR%\dist\win-unpacked\resources\backend"
copy /y "%JAR%" "%ELECTRON_DIR%\dist\win-unpacked\resources\backend\app.jar" >nul
xcopy /e /y "%JRE_DIR%" "%ELECTRON_DIR%\dist\win-unpacked\resources\jre\" >nul
echo       完成

echo.
echo ========================================
echo   构建完成！
echo   安装包: %ELECTRON_DIR%\dist\win-unpacked\
echo ========================================
pause
