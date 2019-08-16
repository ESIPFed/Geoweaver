#!/bin/bash
# 
# Installation script for Geoweaver on Mac OS
#
# author: Ziheng Sun
# date: 08/16/2019
#

echo "Create install folder and enter it"

mkdir install

cd install

echo -e "Download OpenJDK"

wget https://download.java.net/java/GA/jdk12.0.2/e482c34c86bd4bf8b56c0b35558996b9/10/GPL/openjdk-12.0.2_linux-x64_bin.tar.gz 

tar -zxvf openjdk-12.0.2_linux-x64_bin.tar.gz 

echo "Download Tomcat"

wget http://mirrors.ibiblio.org/apache/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22.tar.gz

tar -zxvf apache-tomcat-9.0.22.tar.gz

echo "change the default jdk to the downloaded jdk"

sed '109 a JAVA_HOME=../../openjdk-12.0.2_linux-x64_bin/' apache-tomcat-9.0.22/bin/catalina.sh

echo "download Geoweaver into Apache Tomcat Webapp Folder"

wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/Geoweaver.war

mv Geoweaver.war apache-tomcat-9.0.22/webapps/

echo "start the tomcat.."

apache-tomcat-9.0.22/bin/startup.sh

sleep 3

echo "modify the Geoweaver configuration"



echo "Geoweaver is successfully installed1 Please visit in browser http://localhost:8080/Geoweaver to find it out!"

