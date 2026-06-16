@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-25.0.1.8-hotspot
set Path=%JAVA_HOME%\bin;%Path%
set JAVA_OPTS=-Xms256m -Xmx1g -XX:MaxDirectMemorySize=384m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:+DisableExplicitGC
cd /d "%~dp0backend"
call "%~dp0backend\.mvn\wrapper\bin\mvn.cmd" clean compile -DskipTests
if errorlevel 1 pause
call "%~dp0backend\.mvn\wrapper\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"
pause
