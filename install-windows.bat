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

if exist "openjdk-12.0.2_windows-x64_bin.zip" (

	echo ++++++++++++++++ found openjdk-12.0.2_windows-x64_bin.zip
	
) else (
	
	echo -e "Download OpenJDK"
	
	curl -L "https://download.java.net/java/GA/jdk12.0.2/e482c34c86bd4bf8b56c0b35558996b9/10/GPL/openjdk-12.0.2_windows-x64_bin.zip" -o "openjdk-12.0.2_windows-x64_bin.zip"

rem	tar -zxvf openjdk-12.0.2_linux-x64_bin.tar.gz 

	

)

if not exist "jdk-12.0.2" (

	echo "Expand-Archive" %CD%\openjdk-12.0.2_windows-x64_bin.zip %CD%\
	
	powershell -Command "Expand-Archive -Path '%CD%\openjdk-12.0.2_windows-x64_bin.zip' -DestinationPath '%CD%\' "
	
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
			
			echo set JAVA_HOME=%CD%\jdk-12.0.2>> "temp.txt"
			
			echo set JRE_HOME=%CD%\jdk-12.0.2>> "temp.txt"
		
		)
		
		echo %%a >> temp.txt
	)

)


rem echo ***New bottom line*** >> temp.txt

rem echo y|del "apache-tomcat-9.0.2F2\bin\catalina.bat"

rem rename "temp.txt" "apache-tomcat-9.0.22\bin\catalina.bat"

copy "temp.txt" "apache-tomcat-9.0.22\bin\startup.bat"

rem echo JAVA_HOME='$PWD'/jdk-12.0.2/ 109 >> apache-tomcat-9.0.22/bin/catalina.sh

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
