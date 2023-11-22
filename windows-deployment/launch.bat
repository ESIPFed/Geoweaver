@echo off
start /min cmd /c java -jar geoweaver.jar
:LOOP
timeout /t 5
netstat -ano | find "8070" >nul
if errorlevel 1 goto LOOP
start http://localhost:8070/Geoweaver