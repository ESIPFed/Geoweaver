[![Build Status](https://travis-ci.org/ESIPFed/Geoweaver.svg?branch=master)](https://travis-ci.org/ESIPFed/Geoweaver) [![License](https://img.shields.io/github/license/ESIPFed/Geoweaver.svg)](https://github.com/ESIPFed/Geoweaver/blob/master/LICENSE) [![Stars](https://img.shields.io/github/stars/ESIPFed/Geoweaver.svg)](https://github.com/ESIPFed/Geoweaver/stargazers) [![Forks](https://img.shields.io/github/forks/ESIPFed/Geoweaver.svg)](https://github.com/ESIPFed/Geoweaver/network/members) [![Issues](https://img.shields.io/github/issues/ESIPFed/Geoweaver.svg)](https://github.com/ESIPFed/Geoweaver/issues) [![Coverage](https://img.shields.io/badge/covarege-100%25-success.svg)](https://codecov.io/)

# [Geoweaver](https://esipfed.github.io/Geoweaver/)

2018 ESIP Lab Incubator Project

Geoweaver is a web system allowing users to easily compose and execute full-stack deep learning workflows via taking advantage of online spatial data facilities, high-performance computation platforms, and open-source deep learning libraries. It is a perfect alternative to SSH client (e.g., Putty), FTP client, and scientific workflow software. 

GeoWeaver is a community effort. Any contribution is welcome and greatly appreciated! 

[Geoweaver Online API](https://zihengsun.github.io/Geoweaver/)

##### Table of Contents

- [Project Goals](#project-goals)
- [Installation](#installation)
  * [Dependencies](#dependencies)
  * [Quick Install (Recommended)](#quick-install)
  * [Developer Install](#developer-install)
    + [Linux](#linux)
    + [Mac](#mac)
    + [Windows](#windows)
    + [Docker](#docker)
    + [Tomcat War](#tomcat-war)
    + [Cloud VM Template](#cloud-vm-template)
  * [Build from source](#build-from-source)
- [Demo](#demo)
- [Tutorial](#tutorial)
- [Citation](#citation)
- [Dependencies](#dependencies)
- [License](#license)


# Project Goals

1) turning large-scale distributed deep network into manageable modernized workflows;

2) boosting higher utilization ratio of the existing cyberinfrastructures by separating scientists from
tedious technical details;

3) enhancing the frequency and accuracy of classified land cover land use maps for agricultural purposes;

4) enabling the tracking of provenance by recording the execution logs in structured tables to evaluate the
quality of the result maps;

5) proof the effectiveness of operationally using large-scale distributed deep learning models in classifying
Landsat image time series.

# Installation

## Dependencies

Java 1.8+ (OpenJDK 8 or higher)

!(only for install via docker) [Docker](https://docs.docker.com/install/) 18.09.1+

!(only for install via docker) [Docker-compose](https://docs.docker.com/compose/install/) 1.23.1+ 

## Quick Install

(Recommended)

* Step 1: Download the latest version of [geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar)

* Step 2: Run the command: 

```shell
java -jar geoweaver.jar 
```

* Step 3: Open browser and entry: http://localhost:8070/Geoweaver/ .That's it!

## Developer Install

This section is dedicated for developer users who have better background on web technologies and familiar with MySQL, H2, tomcat, docker, and maven. If you are familiar with neither of them, we strongly suggest you use the "Quick Install" way to install Geoweaver. 


### Linux

This way works for most linux releases, e.g., Ubuntu, CentOS, RedHat, OpenBSD, etc.

* Step 1: clone the github repo

```shell
git clone https://github.com/ESIPFed/Geoweaver.git
```

* Step 2: enter the folder and start the install

```shell
cd Geoweaver
chmod 755 install-linux.sh
./install-linux.sh
```

* Once the script stops, Geoweaver should already be up and running. Enter URL http://127.0.0.1:8080/Geoweaver/web/geoweaver in browser to open it. 

* Optional: To stop Geoweaver, type: `install/apache-tomcat-9.0.22/bin/shutdown.sh`. To start Geoweaver again, type: `install/apache-tomcat-9.0.22/bin/startup.sh`

### Mac

* Step 1: clone the github repo

```shell
git clone https://github.com/ESIPFed/Geoweaver.git
```

* Step 2: enter the folder and start the install

```shell
cd Geoweaver
chmod 755 install-mac.sh
./install-mac.sh
```

* Once the script stops, the Geoweaver should already be up and running. Enter URL http://127.0.0.1:8080/Geoweaver/web/geoweaver in browser to open it.

* Optional: To stop Geoweaver, type: `install/apache-tomcat-9.0.22/bin/shutdown.sh`. To start Geoweaver again, type: `install/apache-tomcat-9.0.22/bin/startup.sh`

### Windows

* Step 1: clone the github repo

```shell
git clone https://github.com/ESIPFed/Geoweaver.git
```

* Step 2: enter the folder and start the install

```shell
cd Geoweaver
./install-windows.bat
```

* Once the script stops, the Geoweaver should already be up and running. Enter URL http://127.0.0.1:8080/Geoweaver/web/geoweaver in browser to open it.

* Optional: To stop Geoweaver, type: `install/apache-tomcat-9.0.22/bin/shutdown.bat`. To start Geoweaver again, type: `install/apache-tomcat-9.0.22/bin/startup.bat`

### Docker

We use `docker-compose` to establish the containers for Geoweaver. As the DockerHub is not very friendly for docker-compose yaml at present, we only suggest manual to start from GitHub repo. It only has three steps.

#### Install

* Clone this repo to your machine 
```shell
git clone https://github.com/ESIPFed/Geoweaver.git
```
* Enter the repo and create a new folder `target`. Download a Geoweaver war package from the [release page](https://github.com/ESIPFed/Geoweaver/releases) and save it in the created `target` folder.
```shell
cd Geoweaver && mkdir target && cd target
wget https://github.com/ESIPFed/Geoweaver/releases/download/v0.7.1/Geoweaver.war -O Geoweaver.war
```

* Run docker to start rolling. After the command is finished, Geoweaver should be up and running. 

```shell
cd .. && docker-compose up -d
```

The address is:

```html
http://your-ip:your-port/Geoweaver/web/geoweaver
```

Replace the `your-ip`, `your-port` with the real domain of your tomcat. For example, `localhost:8080`.

Notice: Make sure the local services like mysql and tomcat are shut down before starting `docker-compose`. Otherwise there might be port conflict error on `3306` and `8080`. Or you can change the port to some other free ports in the docker-compose.yml.

If you don't have docker or docker-compose installed, these documents will help. [docker](https://docs.docker.com/install) [docker-compose](https://docs.docker.com/compose/install/)

#### Shutdown

To stop Geoweaver, type:
```shell
docker stop $(docker ps -aq)
```

### Tomcat War

#### Install

* Download [the latest release war](https://github.com/ESIPFed/Geoweaver/releases) and copy it to the webapps directory of Tomcat (e.g. /usr/local/tomcat). Start Tomcat. 


```shell

wget https://github.com/ESIPFed/Geoweaver/releases/download/v0.7.1/Geoweaver.war -O Geoweaver.war
cp Geoweaver.war /usr/local/tomcat/webapps/
/usr/local/tomcat/bin/startup.sh

```

* After the tomcat is fully started, configure the database connection. The configuration files are `WEB-INF/classes/config.properties` 

```shell
nano /usr/local/tomcat/webapps/Geoweaver/WEB-INF/classes/config.properties
nano /usr/local/tomcat/webapps/Geoweaver/WEB-INF/classes/cc_secret.properties
```

Fill the fields with correct values. (database url, default: jdbc:mysql://localhost:3306/cyberconnector) and `WEB-INF/classes/cc_secret.properties` (database username and password: database_user=root database_password=xxxxxxxx). 

(**Note: the MySQL database must be initiated by the SQL file under the folder Geoweaver/docker/db first. If you are using H2 database, please copy the two files: geoweaver.mv.db and geoweaver.trace.db to your user home directory before you start tomcat. **)

```shell
mysql -u root -p < docker/db/gw.sql
```

* Enter the following URL into browser address bar to open Geoweaver:
```html
http://your-ip:your-port/Geoweaver/web/geoweaver
```

#### Shutdown

To stop Geoweaver, use:
```shell
/usr/local/tomcat/bin/shutdown.sh
```

### Cloud VM Template

#### Install

We provide a ready-to-use cloud template for you to install on mainstream cloud platforms like AWS, Google Cloud, Azure, OpenStack and CloudStack. Please go [here](http://cloud.csiss.gmu.edu/public/geoweaver-0.6.8.qcow2) to download the template (3.1 Gigabytes). The username and password of the instance would be `csiss` and `password` respectively. 

To start Geoweaver, go to directory /home/csiss/Geoweaver and execute docker-compose up -d. With no accident, Geoweaver will be up and running. 

```shell
cd /home/csiss/Geoweaver && docker-compose up -d
```

#### Shutdown

To stop Geoweaver, use:

```shell
docker stop $(docker ps -aq)
```

## Build from source

Use maven to build. In the command line go to the root folder and execute `mvn install`. After a success build, the Geoweaver jar package will be under the directory: `Geoweaver/target/Geoweaver-<version>.jar`. 

# Demo

[A live demo site](https://cloud.csiss.gmu.edu/Geoweaver) is available in George Mason University.

# Tutorial

[Geoweaver Tutorial](https://andrewmagill.github.io/#/) - A beginner tutorial about what Geoweaver can do and how it works

# Citation

If you found Geoweaver helpful in your research, please cite us: 

Sun, Ziheng, Liping Di, Annie Burgess, Jason A. Tullis, and Andrew B. Magill. "Geoweaver: Advanced cyberinfrastructure for managing hybrid geoscientific AI workflows." ISPRS International Journal of Geo-Information 9, no. 2 (2020): 119.

# Dependencies

This project is impossible without the support of several fantastic open source libraries.

[d3.js](https://github.com/d3/d3) - BSD 3-Clause

[graph-creator](https://github.com/cjrd/directed-graph-creator) - MIT License

[bootstrap](https://github.com/twbs/bootstrap) - MIT License

[CodeMirror](https://github.com/codemirror/CodeMirror) - MIT License

[JQuery Terminal](https://github.com/jcubic/jquery.terminal) - MIT License

# License

MIT


