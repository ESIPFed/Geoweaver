
# Installation

## Dependencies

Java 1.8+ (OpenJDK 8 or higher)

!(only for install via docker) [Docker](https://docs.docker.com/install/) 18.09.1+

!(only for install via docker) [Docker-compose](https://docs.docker.com/compose/install/) 1.23.1+ 

## Quick Install

(Recommended for Linux, Mac, and Windows)

* Step 1: Download the latest version of [geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar)

* Step 2: Run the command: 

```shell
java -jar geoweaver.jar 
```

* Step 3: Open browser and enter: http://localhost:8070/Geoweaver/ .That's it!

## Build from source

Use maven to build. In the command line go to the root folder and execute `mvn install`. After a success build, the Geoweaver jar package will be under the directory: `Geoweaver/target/Geoweaver-<version>.jar`. 

## Reset Password for Localhost

Geoweaver will automatically create a password for localhost. It will only show once at first run of Geoweaver. It is recommended to copy and save it at a safe place. If forget or missed that password, please run the following command to reset:

```
java -jar geoweaver.jar resetpassword
```

## [Set up HTTP Proxy](http-proxy.md)