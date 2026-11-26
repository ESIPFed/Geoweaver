@echo off

REM Require the patched Java 17 release (or a newer Java release).
java -version > nul 2>&1
if %errorlevel% neq 0 (
    echo Oops! Java is not installed on your system and Geoweaver requires JDK 17.0.21 or newer.
    echo Please download and install Eclipse Temurin JDK 17.0.21.
    timeout /t 5 >nul
    start https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.21%%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.21_9.msi
    exit /b %errorlevel%
)

powershell -NoProfile -Command "$m = (& java -version 2>&1 | Out-String) -match 'version .([0-9]+(\.[0-9]+){0,3})'; if (-not $m) { exit 1 }; $v = $Matches[1]; if ($v -notmatch '\.') { $v = $v + '.0' }; $version = [version]$v; if ($version.Major -lt 17 -or ($version.Major -eq 17 -and $version -lt [version]'17.0.21')) { exit 1 }"
if %errorlevel% neq 0 (
    echo Geoweaver requires JDK 17.0.21 or a newer Java release.
    timeout /t 5 >nul
    start https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.21%%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.21_9.msi
    exit /b 1
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
