# Switching to a Different Database in Geoweaver

## Introduction
In this guide, we will walk you through the steps to switch the database used by Geoweaver to either PostgreSQL or MySQL. Follow the instructions for your preferred database below.

## Keeping the built-in H2 database small

By default Geoweaver stores everything in an embedded H2 database at `~/h2/gw.mv.db`. H2 does not
overwrite data in place: when a row changes, the new version is appended and the old one becomes
free space that H2 reuses only after a while. Process logs used to be saved again on every output
line, which created free space faster than H2 could reuse it, so a busy installation could end up
with a database file many times larger than its data (for example 60 GB for 2 GB of records).

Geoweaver now keeps the file close to the size of its data, also on servers that are never
restarted:

- **Running logs are saved in batches.** Output still streams to the browser line by line, but the
  database copy of a running process's log is updated at most every 5 seconds, and always when the
  process finishes. Set `GEOWEAVER_LOG_FLUSH_MS` to change the interval (`0` saves every line, as
  before).
- **Long logs are stored in files.** A log stays in the database while it is under 64 KB, which
  covers most runs. Past that, the full log goes to `~/geoweaver/run-logs/<xx>/<run id>.log` and
  is compressed to `.log.gz` when the run ends. The database keeps only the last 64 KB. The run
  page (last 5 MB), the Agent API and `geoweaver history` read from the file. Deleting a run
  deletes its file. The database size then depends on how many runs you have, not on how much
  they print.
- **Data is written compressed.** Geoweaver adds `COMPRESS=TRUE` to the H2 address (also to an
  address you set yourself, unless it already sets `COMPRESS`). Existing data stays readable.
  Set `GEOWEAVER_DB_COMPRESS=false` to turn this off.
- **The running server compacts the file.** Every 30 minutes Geoweaver checks the file. Once it is
  at least 64 MB and has doubled since the last compaction (or was never compacted), Geoweaver
  waits until no database work is in progress, pauses new database requests, runs H2's
  `SHUTDOWN COMPACT`, and carries on. Compaction copies the live data into a new file and replaces
  the old one, at roughly 100 MB of live data per second (a database with 1.6 GB of data took
  21 seconds). Requests that arrive meanwhile wait and then continue. Because the trigger is a
  doubling of the file, this happens rarely: 8 times over a simulated two years of heavy use.
  H2 console sessions and other programs connected to the database are disconnected when it runs.
- **The file is also compacted at start-up when it has grown**, before the server opens it. A
  large file can take a few minutes; the progress is printed to the console. The size after each
  compaction is remembered in `~/h2/gw.mv.db.compacted`.

Compaction is safe to interrupt. H2 writes the new copy to `gw.mv.db.tempFile` and replaces the
original only once the copy is complete. If the process stops part way, the original file is
unchanged and the leftover temp file is removed the next time the database is opened. Geoweaver
compacts only when the disk has room for the new copy (twice the last compacted size, or half the
file the first time) plus 64 MB. If a compaction still runs out of space, the original file is
kept, and automatic compaction waits until the file has doubled again. Free some space and run
`db-compact` by hand.

To compact on demand, stop the Geoweaver server and run:

```shell
java -jar geoweaver.jar db-compact --dry-run   # show what would happen
java -jar geoweaver.jar db-compact
```

As with any maintenance on important data, keep a copy of `~/h2/gw.mv.db` before the first
compaction if you have the disk space. Back up the run-log folder together with the database.

## Running in a home folder with a disk quota

Many clusters give each user a small home folder (for example 15 GB). Everything Geoweaver writes
there is kept within limits, and a nearly full disk does not stop runs:

| What | Where | How it is kept small |
| --- | --- | --- |
| Database | `~/h2/gw.mv.db` | compressed, compacted by the running server (above) |
| Long run logs | `~/geoweaver/run-logs` | at most 2 GB together; the oldest log files are removed first, and the database still has the last 64 KB of each |
| Run folders | `~/gw-workspace/<run id>` | the script and process copies Geoweaver puts there are removed when the run ends; files the run itself creates are kept |
| Temporary files | `~/gw-workspace/temp` | exports, uploads and unpacked imports are removed after 7 days |
| Server logs | `~/geoweaver/logs` | 30 days, files of 50 MB, at most 300 MB; process output is not copied here |

When less than 512 MB is free, Geoweaver stops starting new log files: runs still complete, and
the database keeps the last 64 KB of each log with a note that the rest was not saved. It also
compacts the database sooner and writes a warning to the server log. To keep the bulk off your
home folder, point `GEOWEAVER_RUN_LOG_DIR` and `geoweaver.workspace` at a scratch or project disk.

Settings (environment variables):

| Variable | Default | Meaning |
| --- | --- | --- |
| `GEOWEAVER_DB_AUTO_COMPACT` | `true` | `false` turns start-up compaction off |
| `GEOWEAVER_DB_COMPACT_MIN_MB` | `64` | smallest file worth compacting |
| `GEOWEAVER_DB_COMPRESS` | `true` | `false` writes uncompressed data, as H2 does by default |
| `GEOWEAVER_STORAGE_MAINTENANCE` | `true` | `false` turns off compaction and clean-up in the running server |
| `GEOWEAVER_STORAGE_CHECK_MINUTES` | `30` | how often the running server checks |
| `GEOWEAVER_DB_ONLINE_COMPACT_MAX_MB` | `12288` | larger files are compacted only at start-up or with `db-compact` |
| `GEOWEAVER_LOG_FLUSH_MS` | `5000` | how often a running log is saved to the database |
| `GEOWEAVER_RUN_LOG_DIR` | `~/geoweaver/run-logs` | where long logs are stored; any mounted disk |
| `GEOWEAVER_RUN_LOG_DB_LIMIT_KB` | `64` | largest log kept entirely in the database |
| `GEOWEAVER_RUN_LOG_MAX_MB` | `2048` | most space all log files may use (`0` = no limit) |
| `GEOWEAVER_MIN_FREE_MB` | `512` | free space below which no new log files are started |
| `GEOWEAVER_TEMP_RETENTION_DAYS` | `7` | age after which temporary files are removed (`0` = keep) |
| `GEOWEAVER_KEEP_RUN_FILES` | `false` | `true` keeps the script and process copies in run folders |

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


