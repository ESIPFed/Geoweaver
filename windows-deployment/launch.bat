@echo off

REM Check if Java is installed
java -version > nul 2>&1
if %errorlevel% neq 0 (
    echo Oops! Java is not installed on your system and running Geoweaver requires Java
    echo Please download and install JDK
    echo JDK from other sources can work as well
    timeout /t 5 >nul
    start https://adoptopenjdk.net/
    exit /b %errorlevel%
)

REM Start the Java application
start /min cmd /c java -jar geoweaver.jar

REM Initialize loop counter
set counter=0

:LOOP
REM Increment loop counter
set /a "counter+=5"

REM Check if counter exceeds 100 seconds
if %counter% geq 100 (
    echo Application did not start within 100 seconds.
    exit /b 1
)

REM Wait for 5 seconds
timeout /t 5 >nul

REM Check if port 8070 is open
netstat -ano | find "8070" >nul
if not errorlevel 1 (
    REM If port 8070 is open, wait for 5 seconds and then open the application in the web browser
    timeout /t 5 >nul
    start http://localhost:8070/Geoweaver
    exit /b 0
)

REM If port 8070 is not open, continue looping
goto LOOP
