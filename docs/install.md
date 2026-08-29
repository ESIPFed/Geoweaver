
# Installation

## Dependencies

- **Java 17+** (OpenJDK / Temurin 17 or higher) for Geoweaver **2.2+** (Spring Boot 3).
- **Docker** [18.09.1+](https://docs.docker.com/install/) only if you install via Docker.

> **JDK &lt; 17 is not supported** by the latest Geoweaver.  
> If you cannot upgrade Java, use **Geoweaver 2.1.x** (Java 11). See [Java 17 migration guide](java17-migration.md) and [Releases](https://github.com/ESIPFed/Geoweaver/releases) (example: [v2.1.7 jar](https://github.com/ESIPFed/Geoweaver/releases/download/v2.1.7/geoweaver.jar)).

Check your runtime:

```shell
java -version
```

## Who should read what?

| You are… | Do this |
|----------|---------|
| **New user** | Install Java 17+, then use Quick Install below. |
| **Existing 2.1.x user upgrading** | Follow [Upgrading](upgrade.md) and [Java 17 migration](java17-migration.md) (back up `~/h2` first). |
| **Stuck on Java 11** | Stay on Geoweaver **2.1.x**; do not use the latest 2.2+ jar until JDK 17 is available. |

## Quick Install

(Applicable on Linux, macOS, and Windows)

### Python Way (recommended)

If you have Python installed:

**Step 1**: Open a Terminal/Prompt and run:

```shell
pip install pygeoweaver --upgrade
```

**Step 2**: After pygeoweaver is installed, run:

```shell
gw start
```

Recent pygeoweaver versions expect **Java 17+** for the current Geoweaver line and will warn / exit if the JDK is too old.

**Step 3**: Open a browser: http://localhost:8070/Geoweaver/

### Java Way

If you only have a JDK (no Python):

**Step 1**: Install **Java 17+**, then download the latest [geoweaver.jar](https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar).

**Step 2**: From the folder that contains the jar:

```shell
java -jar geoweaver.jar
```

**Step 3**: Open http://localhost:8070/Geoweaver/

To stay on the Java 11–compatible line, download a **2.1.x** jar from [Releases](https://github.com/ESIPFed/Geoweaver/releases) instead of `latest`.

## Build from source

Requires **JDK 17+** and Maven.

```shell
mvn clean install
java -jar target/geoweaver.jar
```

The package is produced under `Geoweaver/target/` (e.g. `geoweaver.jar` / versioned artifact).

## Install using Docker

**Step 1**: Install Docker Desktop if you haven't (see [Get Docker](https://docs.docker.com/get-docker/)).

**Step 2**: Pull the image:

```shell
docker pull geoweaver/geoweaver
```

For other tags, see [Docker Hub](https://hub.docker.com/repository/docker/geoweaver/geoweaver/general).

**Step 3**: Run:

```shell
docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 -e PASSWORD="YOUR_PASSWORD" geoweaver/geoweaver
```

> `-v <YOUR_HOME_DIRECTORY>:/home/marsvegan/` mounts your home directory into the container. `marsvegan` is the user inside the image. `-p 8070:8070` exposes the UI. `-e PASSWORD=...` sets the localhost password.

Tip — create an alias:

```shell
alias geoweaver="docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 geoweaver/geoweaver"
```

**Step 4**: Open http://localhost:8070/Geoweaver/

## Reset Password for Localhost

Geoweaver creates a localhost password on first run (shown once). To reset:

```shell
java -jar geoweaver.jar resetpassword
```

With Docker:

```shell
docker run -t -i -v <YOUR_HOME_DIRECTORY>:/home/marsvegan/ -p 8070:8070 jensensun/geoweaver resetpassword
```

## Set up HTTP Proxy

See [HTTP proxy](http-proxy.md).

## Changing the listening port

Default port is **8070**. To change it:

#### Using Environment Variable

**Linux/macOS**:

```shell
export GEOWEAVER_PORT=8081
```

**Windows Command Prompt**:

```shell
set GEOWEAVER_PORT=8081
```

**Windows PowerShell**:

```shell
$env:GEOWEAVER_PORT=8081
```

Then start Geoweaver and open the matching URL (e.g. http://localhost:8081/Geoweaver/).
