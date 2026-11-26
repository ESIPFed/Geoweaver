#!/bin/bash
# 
# Installation script for Geoweaver on Linux Servers
# Tested on Ubuntu, CentOS
#

echo "Create install folder and enter it"

mkdir install

cd install

JDK_VERSION="17.0.21"
JDK_BUILD="9"
JDK_ARCHIVE="OpenJDK17U-jdk_x64_linux_hotspot_${JDK_VERSION}_${JDK_BUILD}.tar.gz"
JDK_HOME="jdk-${JDK_VERSION}+${JDK_BUILD}"
JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VERSION}%2B${JDK_BUILD}/${JDK_ARCHIVE}"
JDK_SHA256="${JDK_ARCHIVE}.sha256.txt"

if [ -f "$JDK_ARCHIVE" ];then
	echo "++++++++++++++++found $JDK_ARCHIVE"
else
	echo -e "Download Temurin JDK $JDK_VERSION"

	wget "$JDK_URL" -O "$JDK_ARCHIVE"
fi

wget "${JDK_URL}.sha256.txt" -O "$JDK_SHA256"
sha256sum -c "$JDK_SHA256" || exit 1
tar -zxvf "$JDK_ARCHIVE"

if [ -f apache-tomcat-9.0.22.tar.gz ];then
	echo "++++++++++++++++found apache-tomcat-9.0.22.tar.gzz "
else
	echo "Download Tomcat"

	wget https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22.tar.gz

	tar -zxvf apache-tomcat-9.0.22.tar.gz
fi



echo "change the default jdk to the downloaded jdk"

sed '109 a JAVA_HOME='$PWD'/'"$JDK_HOME"'/' apache-tomcat-9.0.22/bin/catalina.sh > apache-tomcat-9.0.22/bin/catalina2.sh

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
