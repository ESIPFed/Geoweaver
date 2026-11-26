rem 
rem Installation script for Geoweaver on Windows Servers
rem Tested on Windows
rem

@echo off

setlocal

echo "Create install folder and enter it"

mkdir install

cd install

echo current directory is %CD%

set JDK_VERSION=17.0.21
set JDK_BUILD=9
set JDK_DIR=jdk-%JDK_VERSION%+%JDK_BUILD%
set JDK_ARCHIVE=OpenJDK17U-jdk_x64_windows_hotspot_%JDK_VERSION%_%JDK_BUILD%.zip
set JDK_URL=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-%JDK_VERSION%%%2B%JDK_BUILD%/OpenJDK17U-jdk_x64_windows_hotspot_%JDK_VERSION%_%JDK_BUILD%.zip

if exist "%JDK_ARCHIVE%" (

	echo ++++++++++++++++ found %JDK_ARCHIVE%
	
) else (
	
	echo Download Temurin JDK %JDK_VERSION%
	
	curl -fL "%JDK_URL%" -o "%JDK_ARCHIVE%"

)

powershell -NoProfile -Command "$expected = (Invoke-WebRequest -UseBasicParsing '%JDK_URL%.sha256.txt').Content.Split()[0]; $actual = (Get-FileHash -Algorithm SHA256 '%CD%\%JDK_ARCHIVE%').Hash; if ($actual.ToLower() -ne $expected.ToLower()) { exit 1 }"
if errorlevel 1 (
	echo JDK archive checksum verification failed.
	exit /b 1
)

if not exist "%JDK_DIR%" (

	echo "Expand-Archive" %CD%\%JDK_ARCHIVE% %CD%\
	
	powershell -Command "Expand-Archive -Path '%CD%\%JDK_ARCHIVE%' -DestinationPath '%CD%\' "
	
)

if exist "apache-tomcat-9.0.22-windows-x64.zip" (
	
	echo ++++++++++++++++ found apache-tomcat-9.0.22.tar.gz
	
) else (
	
	echo "Download Tomcat"

	curl -L "https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22-windows-x64.zip" -o "apache-tomcat-9.0.22-windows-x64.zip"

	
)

if not exist "apache-tomcat-9.0.22" (
	
	rem tar -zxvf apache-tomcat-9.0.24-windows-x64.zip
	
	powershell -Command "Expand-Archive -Path '%CD%\apache-tomcat-9.0.22-windows-x64.zip' -DestinationPath '%CD%\' "
	
	echo "change the default jdk to the downloaded jdk"

	del "temp.txt"

	for /f "tokens=*" %%a in (apache-tomcat-9.0.22\bin\startup.bat) do (
		
		rem if "%%a" == "" (  ) else ( echo %%a >> temp.txt )

		if "%%a" == "setlocal" (
			
			rem echo this is the position should the new line being inserted >> temp.txt
			
			echo set CATALINA_HOME=%CD%\apache-tomcat-9.0.22>> "temp.txt"
			
			echo set JAVA_HOME=%CD%\%JDK_DIR%>> "temp.txt"
			
			echo set JRE_HOME=%CD%\%JDK_DIR%>> "temp.txt"
		
		)
		
		echo %%a >> temp.txt
	)

)


rem echo ***New bottom line*** >> temp.txt

rem echo y|del "apache-tomcat-9.0.2F2\bin\catalina.bat"

rem rename "temp.txt" "apache-tomcat-9.0.22\bin\catalina.bat"

copy "temp.txt" "apache-tomcat-9.0.22\bin\startup.bat"

echo download Geoweaver into Apache Tomcat Webapp Folder

del "Geoweaver.war"

curl -L "https://github.com/ESIPFed/Geoweaver/releases/download/latest/Geoweaver.war" -o "Geoweaver.war"

copy "Geoweaver.war" "apache-tomcat-9.0.22\webapps\"


echo Move database in place

echo Checking C:%HOMEPATH%\geoweaver.mv.db

if not exist "geoweaver.mv.db" (

echo Database file not exists Copying ...

copy "..\db\geoweaver.mv.db" "C:%HOMEPATH%\"

copy "..\db\geoweaver.trace.db" "C:%HOMEPATH%\"

}

echo start the tomcat..

rem chmod 755 apache-tomcat-9.0.22/bin/catalina.sh

rem chmod 755 apache-tomcat-9.0.22/bin/startup.sh

apache-tomcat-9.0.22\bin\startup.bat

SLEEP 3

echo modify the Geoweaver configuration

rem there is no need to do this

echo Geoweaver is successfully installed!

echo ********************************************************************

echo Please visit in browser http://localhost:8080/Geoweaver to use Geoweaver!

echo ********************************************************************
