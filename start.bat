@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot
set Path=%JAVA_HOME%\bin;%Path%
cd /d "%~dp0backend"
call "%~dp0backend\.mvn\wrapper\bin\mvn.cmd" clean compile -DskipTests
if errorlevel 1 pause
call "%~dp0backend\.mvn\wrapper\bin\mvn.cmd" spring-boot:run
pause
