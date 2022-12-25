#!/bin/bash
# 
# Installation script for Geoweaver on Mac Servers
# Tested on Mac OS
#

echo "Create install folder and enter it"

mkdir install

cd install

#if [ -f openjdk-8u40-b25-linux-x64-10_feb_2015.tar.gz ];then
#	echo "++++++++++++++++found openjdk-12.0.2_linux-x64_bin.tar.gz "
#else
#	echo -e "Download OpenJDK"

#	curl https://download.java.net/openjdk/jdk8u40/ri/openjdk-8u40-b25-linux-x64-10_feb_2015.tar.gz --output openjdk-8u40-b25-linux-x64-10_feb_2015.tar.gz

#	tar -zxvf openjdk-8u40-b25-linux-x64-10_feb_2015.tar.gz
#fi

echo "install java via brew. Make sure HomeBrew is installed."

brew tap AdoptOpenJDK/openjdk

brew cask install adoptopenjdk8

echo "download and unzip tomcat"

if [ -f apache-tomcat-9.0.22.tar.gz ];then
	echo "++++++++++++++++found apache-tomcat-9.0.22.tar.gzz "
else
	echo "Download Tomcat"

	curl https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22.tar.gz --output apache-tomcat-9.0.22.tar.gz

	tar -zxvf apache-tomcat-9.0.22.tar.gz
fi

#echo "change the default jdk to the downloaded jdk"

#sed '108 a\'$'\n''JAVA_HOME='$PWD'/java-se-8u40-ri/' apache-tomcat-9.0.22/bin/catalina.sh > apache-tomcat-9.0.22/bin/catalina2.sh

#mv apache-tomcat-9.0.22/bin/catalina2.sh apache-tomcat-9.0.22/bin/catalina.sh

echo "change the default port to 3030"

sed -i '' 's/8080/3030/g' apache-tomcat-9.0.22/conf/server.xml

sed -i '' 's/8009/3009/g' apache-tomcat-9.0.22/conf/server.xml

sed -i '' 's/8005/3005/g' apache-tomcat-9.0.22/conf/server.xml

echo "download Geoweaver into Apache Tomcat Webapp Folder"

curl -L https://github.com/ESIPFed/Geoweaver/releases/download/latest/Geoweaver.war --output Geoweaver.war

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

echo "Please visit in browser http://localhost:3030/Geoweaver to use Geoweaver!"

echo "********************************************************************"
