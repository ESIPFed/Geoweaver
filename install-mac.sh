#!/bin/bash
# 
# Installation script for Geoweaver on Mac Servers
# Tested on Mac OS
#
# author: Ziheng Sun
# date: 08/20/2019
#

echo "Create install folder and enter it"

mkdir install

cd install

if [ -f openjdk-12.0.2_linux-x64_bin.tar.gz ];then
	echo "++++++++++++++++found openjdk-12.0.2_linux-x64_bin.tar.gz "
else
	echo -e "Download OpenJDK"

	wget https://download.java.net/java/GA/jdk12.0.2/e482c34c86bd4bf8b56c0b35558996b9/10/GPL/openjdk-12.0.2_osx-x64_bin.tar.gz

	tar -zxvf openjdk-12.0.2_osx-x64_bin.tar.gz
fi

if [ -f apache-tomcat-9.0.22.tar.gz ];then
	echo "++++++++++++++++found apache-tomcat-9.0.22.tar.gzz "
else
	echo "Download Tomcat"

	wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22.tar.gz

	tar -zxvf apache-tomcat-9.0.22.tar.gz
fi



echo "change the default jdk to the downloaded jdk"

sed '109 a JAVA_HOME='$PWD'/jdk-12.0.2/' apache-tomcat-9.0.22/bin/catalina.sh > apache-tomcat-9.0.22/bin/catalina2.sh

mv apache-tomcat-9.0.22/bin/catalina2.sh apache-tomcat-9.0.22/bin/catalina.sh

echo "download Geoweaver into Apache Tomcat Webapp Folder"

wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/Geoweaver.war

mv Geoweaver.war apache-tomcat-9.0.22/webapps/

echo "Move database in place"

cp ../db/geoweaver* ~/

echo "start the tomcat.."

chmod 755 apache-tomcat-9.0.22/bin/catalina.sh

chmod 755 apache-tomcat-9.0.22/bin/startup.sh

./apache-tomcat-9.0.22/bin/startup.sh

sleep 3

echo "modify the Geoweaver configuration"

# there is no need to do this

echo "Geoweaver is successfully installed!"

echo "********************************************************************"

echo "Please visit in browser http://localhost:8080/Geoweaver to use Geoweaver!"

echo "********************************************************************"
