
# Installation

## Dependencies

Java 1.8+ (OpenJDK 8 or higher)

[Docker](https://docs.docker.com/install/) 18.09.1+ (only for installation via docker)

## Quick Install

(Applicable in Linux, Mac, and Windows)

### Python Way

If you have Python installed already, please do:

**Step 1**: Open a Terminal/Prompt and run 

```shell
pip install pygeoweaver --upgrade
```

**Step 2**: After pygeoweaver is installed, please run:

```shell
gw start
```

**Step 3**: Open browser and enter: http://localhost:8070/Geoweaver/ . That is it. 

### Java Way

If you don't have Python only have JDK installed, please follow:

**Step 1**: Download the latest version of [geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar) and save it in any folder

**Step 2**: Navigate to the Jar file folder and Run the command:

```shell
java -jar geoweaver.jar
```

**Step 3**: Open browser and enter: http://localhost:8070/Geoweaver/ .That's it!

## Build from source

Use maven to build. In the command line, go to the root folder and execute `mvn install`. After a successful build, the Geoweaver jar package will be under the directory: `Geoweaver/target/Geoweaver-<version>.jar`.

## Install using Docker

**Step 1**: Install Docker Desktop if you haven't (please refer to [Get Docker](https://docs.docker.com/get-docker/) to download).

**Step 2**: Pull geoweaver image using command:

`docker pull geoweaver/geoweaver`

for other versions, visit [docker hub](https://hub.docker.com/repository/docker/geoweaver/geoweaver/general)

**Step 3**: Run geoweaver using command:

`docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 -e PASSWORD="YOUR_PASSWORD" geoweaver/geoweaver`

>  *What is going on?* : `-v <YOUR_HOME_DIRECTORY>:/home/marsvegan/` is for mounting your current home directory into the docker containers. `marsvegan` is the user name within geoweaver containers. `-p 8070:8070` maps the port so you can access Geoweaver from your browser. `-e` is for specifying environment variable `PASSWORD`. `geoweaver/geoweaver` is the published docker image url in DockerHub.

*Tip*: You can create an alias, so next time, you won't copy and paste such a long command:

`alias geoweaver="docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 geoweaver/geoweaver"`


Then start it by simply typing: `geoweaver`

**Step 4**: Open web browser and input `http://localhost:8070/Geoweaver`. Geoweaver should show up in a sec. That is it!

## Reset Password for Localhost

Geoweaver will automatically create a password for localhost. It will only show once at the first run of Geoweaver. It is recommended to copy and save it in a safe place. If you forget or miss that password, please run the following command to reset it:

```
java -jar geoweaver.jar resetpassword
```

If you used `Docker`, use the following command:

```
docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 jensensun/geoweaver resetpassword
```

## Set up HTTP Proxy

[Click here](http-proxy.md) to set up

## Changing the listening port

Geoweaver allows one to change the listening port to run multiple instances simultaneously on the same machine.
Default port used by Geoweaver is 8070, to change the port please run the following command:

#### Using Environment Variable

**For Linux/macOS**:
```
export GEOWEAVER_PORT='Port number'
```
***Example: export GEOWEAVER_PORT=8081***

**For Windows-Command Prompt**:
```
set GEOWEAVER_PORT='Port number'
```
***Example: set GEOWEAVER_PORT=8081***

**For Windows-Powershell**:
```
$env GEOWEAVER_PORT='Port number'
```
***Example: $env GEOWEAVER_PORT=8081***

Run the command above in a new terminal, then start the application(using start command) to launch a new instance of Geoweaver on your machine. 

```
java -jar Geoweaver.jar
```

After changing the port using the above command manually change the port number in url.

