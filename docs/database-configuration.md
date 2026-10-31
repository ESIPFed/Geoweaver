# Switching to a Different Database in Geoweaver

## Introduction
In this guide, we will walk you through the steps to switch the database used by Geoweaver to either PostgreSQL or MySQL. Follow the instructions for your preferred database below.

## Keeping the built-in H2 database small

By default Geoweaver stores everything in an embedded H2 database at `~/h2/gw.mv.db`. H2 does not
overwrite data in place: when a row changes, the new version is appended and the old one becomes
free space that H2 only partly reuses while it is running. Process logs used to be saved again on
every output line, so a busy installation could end up with a database file many times larger
than its data (for example 60 GB for 2 GB of records).

Geoweaver now handles this in two ways:

- **Running logs are saved in batches.** Output still streams to the browser line by line, but the
  database copy of a running process's log is updated at most every 5 seconds, and always when the
  process finishes. Set `GEOWEAVER_LOG_FLUSH_MS` to change the interval (`0` saves every line, as
  before).
- **The file is compacted at start-up when it has grown.** Before the server opens the database it
  checks the file. If the file is at least 512 MB and has never been compacted, or has doubled in size
  since the last compaction, Geoweaver runs H2's `SHUTDOWN COMPACT`. This copies the live data,
  compressed, into a new file and replaces the old one. A large file can take a few minutes, and
  the progress is printed to the console. The size after compaction is remembered in
  `~/h2/gw.mv.db.compacted`.

Compaction is safe to interrupt. H2 writes the new copy to `gw.mv.db.tempFile` and replaces the
original only once the copy is complete. If the process stops part way, the original file is
unchanged and the leftover temp file is removed the next time the database is opened. Geoweaver
skips compaction when less than 1 GB plus 10% of the file size is free on the disk. If a compaction
still runs out of space, the original file is kept, and automatic compaction waits until the file
has doubled again. Free some space and run `db-compact` by hand.

Settings (environment variables):

| Variable | Default | Meaning |
| --- | --- | --- |
| `GEOWEAVER_DB_AUTO_COMPACT` | `true` | `false` turns start-up compaction off |
| `GEOWEAVER_DB_COMPACT_MIN_MB` | `512` | smallest file worth compacting |
| `GEOWEAVER_LOG_FLUSH_MS` | `5000` | how often a running log is saved to the database |

To compact on demand, stop the Geoweaver server and run:

```shell
java -jar geoweaver.jar db-compact --dry-run   # show what would happen
java -jar geoweaver.jar db-compact
```

As with any maintenance on important data, keep a copy of `~/h2/gw.mv.db` before the first
compaction if you have the disk space.

## Switching to PostgreSQL

### Step 1: Install PostgreSQL

Ensure PostgreSQL is installed on your system. You can download it from the [official PostgreSQL website](https://www.postgresql.org/download/).

### Step 2: Create a Database and User

Open the PostgreSQL shell or use a database management tool like pgAdmin. Run the following commands to create a new database and user:

```shell
psql postgres
```

```sql
CREATE USER myuser WITH PASSWORD 'mypassword';
CREATE DATABASE mydatabase OWNER myuser;
```
- To connect to the database use the below command

```shell
psql -U myuser -d mydatabase
```


enter \q to exit from the postgres shell

### Step 3: Create and Configure application.properties File



**Windows** 

- Open command prompt, navigate to home directory and create the `geoweaver` directory

```shell
cd %HOMEPATH%
mkdir geoweaver
cd geoweaver
```

- Create a new application.properties file using Notepad: 

```shell
notepad application.properties
```

- Add the PostgreSQL configuration:

```text
spring.datasource.url=jdbc:postgresql://localhost:5432/geoweaver_db
spring.datasource.username=geoweaver_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

- Save and close the file.

**Linux and Mac:** 

- Open a terminal, Navigate to your home directory and Create the `geoweaver` directory:

```shell
cd ~
mkdir geoweaver
cd geoweaver
```

- Create a new application.properties file using a text editor. For example, using nano:

```shell
nano application.properties
```

- Add the PostgreSQL configuration:

```text
spring.datasource.url=jdbc:postgresql://localhost:5432/geoweaver_db
spring.datasource.username=geoweaver_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```
- Save and close the file by pressing Ctrl + O, then Enter, and exit by pressing Ctrl + X.



### Step 4: Restart Geoweaver
Restart your Geoweaver application to apply the changes. Verify the connection by checking  accessing the application.
**Windows**
- Find the PID (Process ID) using the specified port:
```shell
netstat -ano | findstr :8070
```

- Kill the process using the PID found in the previous step:
```shell
taskkill /PID <PID> /F
```

**Linux or Mac**
- Kill the process using below command
```shell
kill -9 $(lsof -t -i:8070)
```

<hr>

## Switching to MySQL

### Step 1: Install MySQL

Ensure MySQL is installed on your system. You can download it from the [official MySQL website](https://dev.mysql.com/downloads/mysql/).


### Step 2: Create a Database and User

Open the MySQL shell or use a database management tool like MySQL Workbench. Run the following commands to create a new database and user:

```shell
mysql -u root -p
```

```sql
CREATE DATABASE geoweaver_db;
CREATE USER 'geoweaver_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON geoweaver_db.* TO 'geoweaver_user'@'localhost';
FLUSH PRIVILEGES;
```


### Step 3: Create and Configure application.properties File


**Windows** 

- Open command prompt, navigate to home directory and create the `geoweaver` directory

```shell
cd %HOMEPATH%
mkdir geoweaver
cd geoweaver
```

- Create a new application.properties file using Notepad: 

```shell
notepad application.properties
```

- Add the MySQL configuration:

```text
spring.datasource.url=jdbc:mysql://localhost:3306/geoweaver
spring.datasource.username=root
spring.datasource.password=Research-12
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

- Save and close the file.

**Linux and Mac:**

- Open a terminal, Navigate to your home directory and Create the `geoweaver` directory:

```shell
cd ~
mkdir geoweaver
cd geoweaver
```

- Create a new application.properties file using a text editor. For example, using nano:

```shell
nano application.properties
```

- Add the MySQL configuration:

```text
spring.datasource.url=jdbc:mysql://localhost:3306/geoweaver
spring.datasource.username=root
spring.datasource.password=Research-12
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```
- Save and close the file by pressing Ctrl + O, then Enter, and exit by pressing Ctrl + X.



### Step 4: Restart Geoweaver
Restart your Geoweaver application to apply the changes. Verify the connection by checking  accessing the application.


**Windows**

- Find the PID (Process ID) using the specified port:
```shell
netstat -ano | findstr :8070
```

- Kill the process using the PID found in the previous step:
```shell
taskkill /PID <PID> /F
```

**Linux or Mac**
- Kill the process using below command
```shell
kill -9 $(lsof -t -i:8070)
```


