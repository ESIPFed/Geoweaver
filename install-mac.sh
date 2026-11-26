#!/bin/bash
# 
# Installation script for Geoweaver on Mac Servers
# Tested on Mac OS
#

echo "Create install folder and enter it"

mkdir install

cd install

JDK_VERSION="17.0.21"
JDK_BUILD="9"
JDK_ARCHIVE_VERSION="${JDK_VERSION}_${JDK_BUILD}"
JDK_ARCHITECTURE="$(uname -m)"

case "$JDK_ARCHITECTURE" in
  x86_64) JDK_ARCH="x64" ;;
  arm64) JDK_ARCH="aarch64" ;;
  *) echo "Unsupported macOS architecture: $JDK_ARCHITECTURE"; exit 1 ;;
esac

JDK_ARCHIVE="OpenJDK17U-jdk_${JDK_ARCH}_mac_hotspot_${JDK_ARCHIVE_VERSION}.tar.gz"
JDK_URL="https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VERSION}%2B${JDK_BUILD}/${JDK_ARCHIVE}"

echo "Download and install Temurin JDK ${JDK_VERSION}."
curl -fL --retry 3 "$JDK_URL" --output "$JDK_ARCHIVE" || exit 1
curl -fsSL "${JDK_URL}.sha256.txt" | shasum -a 256 -c - || exit 1
tar -xzf "$JDK_ARCHIVE" || exit 1
export JAVA_HOME="$PWD/jdk-${JDK_VERSION}+${JDK_BUILD}/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "download and unzip tomcat"

if [ -f apache-tomcat-9.0.22.tar.gz ];then
	echo "++++++++++++++++found apache-tomcat-9.0.22.tar.gzz "
else
	echo "Download Tomcat"

	curl https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.22/bin/apache-tomcat-9.0.22.tar.gz --output apache-tomcat-9.0.22.tar.gz

	tar -zxvf apache-tomcat-9.0.22.tar.gz
fi

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
