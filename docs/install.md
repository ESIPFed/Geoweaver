
# Installation

## Dependencies

Java 1.8+ (OpenJDK 8 or higher)

!(only for install via docker) [Docker](https://docs.docker.com/install/) 18.09.1+

## Quick Install

(Applicalbe in Linux, Mac, and Windows)

* Step 1: Download the latest version of [geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar)

* Step 2: Run the command: 

```shell
java -jar geoweaver.jar 
```

* Step 3: Open browser and enter: http://localhost:8070/Geoweaver/ .That's it!

## Build from source

Use maven to build. In the command line go to the root folder and execute `mvn install`. After a success build, the Geoweaver jar package will be under the directory: `Geoweaver/target/Geoweaver-<version>.jar`. 

## Install using Docker

* Step 1: Install Docker Desktop if you haven't (please refer to [Get Docker](https://docs.docker.com/get-docker/)).

* Step 2: Pull geoweaver image using command: 

`docker pull jensensun/geoweaver`

* Step 3: Run geoweaver using command:

`docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 jensensun/geoweaver`

> *What is going on?* : `-v <YOUR_HOME_DIRECTORY>:/home/marsvegan/` is for mounting your current home directory into the docker containers. `marsvegan` is the user name within geoweaver containers. `-p 8070:8070` maps the port so you can access Geoweaver from your browser. `jensensun/geoweaver` is the published docker image url in DockerHub.

> *Tip*: You can create an alias so next time you won't copy and paste such a long command:

`alias geoweaver="docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 jensensun/geoweaver"`

Then start it by simply typing:

`geoweaver`

* Step 4: Open web browser and input `http://localhost:8070/Geoweaver`. Geoweaver should show up in a sec. That is it!



## Reset Password for Localhost

Geoweaver will automatically create a password for localhost. It will only show once at first run of Geoweaver. It is recommended to copy and save it at a safe place. If forget or missed that password, please run the following command to reset:

```
java -jar geoweaver.jar resetpassword
```

If you used `Docker`, use the following command:

```
docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 jensensun/geoweaver resetpassword
```

## [Set up HTTP Proxy](http-proxy.md)