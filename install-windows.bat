rem 
rem Installation script for Geoweaver on Windows Servers
rem Tested on Windows
rem
rem author: Ziheng Sun
rem date: 08/15/2019
rem

@echo off

setlocal

echo "Create install folder and enter it"

mkdir install

cd install

if EXIST "openjdk-12.0.2_linux-x64_bin.tar.gz" (
	echo ++++++++++++++++ found openjdk-12.0.2_linux-x64_bin.tar.gz
) else (
	
	echo -e Download OpenJDK
	
	curl https://download.java.net/java/GA/jdk12.0.2/e482c34c86bd4bf8b56c0b35558996b9/10/GPL/openjdk-12.0.2_windows-x64_bin.zip

rem	tar -zxvf openjdk-12.0.2_linux-x64_bin.tar.gz 
	
	Call :UnZipFile "." "openjdk-12.0.2_linux-x64_bin.tar.gz"

)

if EXIST "apache-tomcat-9.0.22.tar.gz" (
	
	echo ++++++++++++++++ found apache-tomcat-9.0.22.tar.gz
	
) else (
	
	echo "Download Tomcat"

	curl https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22-windows-x64.zip

	rem tar -zxvf apache-tomcat-9.0.24-windows-x64.zip
	
	Call :UnZipFile "." "apache-tomcat-9.0.24-windows-x64.zip"
	
)

echo change the default jdk to the downloaded jdk

echo current directory is %CD%

for /f "tokens=*" %%a in (apache-tomcat-9.0.22/bin/catalina.bat) do (
	
	echo %%a >> temp.txt
	
	rem if "%%a" == "" (  ) else ( echo %%a >> temp.txt )

	if "%%a" == "setlocal" (
	
		rem echo this is the position should the new line being inserted >> temp.txt
		
		echo set JAVA_HOME=%CD%\jdk-12.0.2\ >> temp.txt
	
	)
	
)

rem echo ***New bottom line*** >> temp.txt

echo y|del apache-tomcat-9.0.22/bin/catalina.bat

rename temp.txt apache-tomcat-9.0.22/bin/catalina.bat

rem echo JAVA_HOME='$PWD'/jdk-12.0.2/ 109 >> apache-tomcat-9.0.22/bin/catalina.sh

echo download Geoweaver into Apache Tomcat Webapp Folder

curl https://github.com/ESIPFed/Geoweaver/releases/download/latest/Geoweaver.war

mv Geoweaver.war apache-tomcat-9.0.22/webapps/

echo Move database in place

cp ../db/geoweaver* ~/

echo start the tomcat..

chmod 755 apache-tomcat-9.0.22/bin/catalina.sh

chmod 755 apache-tomcat-9.0.22/bin/startup.sh

./apache-tomcat-9.0.22/bin/startup.sh

sleep 3

echo modify the Geoweaver configuration

 there is no need to do this

echo Geoweaver is successfully installed!

echo ********************************************************************

echo Please visit in browser http://localhost:8080/Geoweaver to use Geoweaver!

echo ********************************************************************
