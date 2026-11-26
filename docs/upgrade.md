
## Upgrading Geoweaver

> **Before you upgrade to 2.2+:** install **Java 17+**. Latest Geoweaver will refuse to start on older JDKs.  
> Full guidance: [Java 17 migration guide](java17-migration.md).

### Existing users checklist

1. Confirm `java -version` reports **17 or higher**.
2. Back up `~/h2/` (database) and `~/gw-workspace/` (files).
3. Upgrade via Python or JAR (below).
4. If the database fails to open after the jump, use [Complex Upgrade (Database Migration)](#complex-upgrade-database-migration).

### Behavior changes in 3.3.2

This is a security release. **Upgrade every Geoweaver 3.3.1 and earlier**, especially servers that other computers can reach.

- **The browser login can no longer be faked.** Before, anyone who could reach the server could set a cookie by hand and use the web GUI without the localhost password. The login cookie is now signed by the server, expires after 30 days and stops working when the localhost password is reset. **Everyone has to log in once after upgrading.**
- **Every page that is not a plain static file needs login**, including result files and downloads whose names end in `.png`, `.js` and so on.
- **The Agent API answers only programs on the same computer by default.** Requests from other computers, through a reverse proxy (also one on the same computer), or from web pages get HTTP 403 `local_only`. Agents on another machine use SSH: run `curl http://127.0.0.1:8070/Geoweaver/api/v1/...` on the server, or open a tunnel with `ssh -N -L 8070:127.0.0.1:8070 server`. The web GUI is not affected.
- **To let other computers, a reverse proxy or Open OnDemand use the Agent API**, set both `geoweaver.agent.allow-non-loopback=true` and `geoweaver.agent.acknowledge-network-risk=true` in `~/geoweaver/application.properties`, and use HTTPS. With only the first, Geoweaver refuses to start. The yellow warning banner appears only in this mode. See [Agent API](agent-api.md#security-model).
- An Agent API path written with percent-encoding (for example `/api/%761/...`) can no longer skip the token check.
- Geoweaver no longer takes the client address from `X-Forwarded-For` (Spring Boot did so automatically on Kubernetes).
- **Quieter live collaboration:** the note about an AI agent working on the open workflow or process appears only when an agent or `geoweaver` command actually changed it, hides itself after two minutes, and has a close button. Saving a workflow no longer reloads your own canvas.
- The workflow step log panel shows the full saved log (up to 5 MB) again, and each row in the progress panel has its close button again.
- Installers, launchers and the Docker image require Java 17.0.21 or newer.

### Behavior changes in 3.3.1

- **The command line and the browser can work on the same database at the same time**, also from different machines that share the home folder, for example an SSH login node and an Open OnDemand session. The browser shows runs started with `geoweaver run ...` as they progress, and **Stop** in the browser now stops them. `~/h2` and `~/geoweaver` are made readable only by you (`GEOWEAVER_PRIVATE_FILES=false` to keep permissions as they are). See [Several instances on one database](shared-database.md).
- **Everything can be done from the command line, for people and coding agents:** `geoweaver process` and `geoweaver workflow` (`list`, `show`, `create`, `update`, `delete`), and `geoweaver run status | log | stop | list`. `--json` gives only JSON on standard output. `geoweaver run ... --detach` starts a run that keeps going after the command returns. Commands now print warnings and errors only; the log file keeps the rest. A database used only from the command line gets the localhost host automatically. See [Several instances on one database](shared-database.md).
- `geoweaver run process` and `geoweaver run workflow` run on localhost when no host is given (they used to look for a host that does not exist).
- **Stop now ends runs that wait for their process** (command-line runs and workflow steps), and a run stopped on purpose is recorded as Stopped instead of Failed.
- **The running server compacts the database only while no other Geoweaver process is connected to it.**
- **A Geoweaver on a machine whose own name resolves to a loopback address** (common on Linux VMs, for example `127.0.1.1`) now offers the shared database on its network address, so Geoweaver on other machines with the same home folder can join it. `GEOWEAVER_DB_BIND_ADDRESS` still decides when set.
- **Every open Geoweaver page is kept up to date with the other Geoweaver servers and commands on the same database**, within about a second: new and edited processes, workflows and hosts, runs as they start and end (a "Running now" line and a short notice), step status on the open workflow, and the history tables. Runs that start and end between two checks are reported too. Before, a page only heard about changes made through its own server. This adds a small table, `gw_workspace_feed`, created automatically; its rows are removed after an hour. `GEOWEAVER_WORKSPACE_SYNC=false` turns it off.

### Behavior changes in 3.2.0

- **Long process logs are kept in files, not in the database.** A log that grows past 64 KB is written to `~/geoweaver/run-logs/` (compressed when the run ends), and the database keeps only its last 64 KB. The run page and the Agent API read the full log from the file. Point `GEOWEAVER_RUN_LOG_DIR` at any disk with room. Back up this folder together with `~/h2/`. See [Database](database-configuration.md#keeping-the-built-in-h2-database-small).
- **The H2 database file is compacted at start-up when it has grown** (first start after upgrading can take a few minutes on a large file), and `geoweaver db-compact` compacts on demand.
- **A running server keeps its disk use bounded.** It compacts the database when the file has doubled (pausing database requests for a few seconds; H2 console sessions are disconnected), writes data compressed, caps the run-log folder at 2 GB, and removes temporary files after 7 days. See [Running in a home folder with a disk quota](database-configuration.md#running-in-a-home-folder-with-a-disk-quota).
- **Process output is no longer copied into the server log.** It is kept with each run (see above). Repeated warnings such as "no browser is receiving output" are written at most once a minute, and the server log folder is limited to 300 MB (files are split at 50 MB, so the names now end in `.0.log`, `.1.log`, ...).
- **Agent API calls that wait for a run (`waitSeconds`) no longer hold a database connection while they wait**, so many waiting agents cannot block other requests.
- **Run folders in `~/gw-workspace` no longer keep a copy of every Python process.** Geoweaver removes the files it placed there when a run ends; files your code writes are kept. Set `GEOWEAVER_KEEP_RUN_FILES=true` for the old behavior.
- **Running logs are saved to the database every 5 seconds** instead of on every output line; live output in the browser is unchanged.
- History lookups use new indexes, created automatically on the first start.
### Behavior changes in 3.1.0

- **Localhost runs need the real localhost password again.** An empty password no longer runs code on the Geoweaver server. The GUI and `gw run … -p <password>` are unaffected; `gw run` on localhost without `-p` still works because the CLI user is already on that machine. AI agents use a Bearer token ([Agent API](agent-api.md)).
- **Cross-origin browser pages can no longer call the GUI endpoints with your session cookie.** If another web site (for example a portal) embeds or calls Geoweaver, list it in `geoweaver.cors.allowed-origins=https://portal.example.org`. Same-origin use of the GUI is unchanged.
- **SSH passwords are no longer written to the log** when you log in to a host.
- **Restoring a workflow checkpoint** now restores each step's code from that run (it used to pick the wrong run, or fail).
- The Agent API now covers hosts, remote SSH runs, Python environments, run history, step skipping, checkpoints, workflow export/import and result files. See [Agent API](agent-api.md).

If you **cannot** use Java 17, **do not upgrade** to 2.2+. Keep **Geoweaver 2.1.x** from [Releases](https://github.com/ESIPFed/Geoweaver/releases).

On a cluster **without sudo**, if PyGeoWeaver fails while upgrading `~/jdk` with `Permission denied` on a `jdk-11*` path (and you see `jdk.tar.gz already exists`), follow **[Cluster / no-sudo JDK recovery](java17-migration.md#cluster--no-sudo-pygeoweaver-jdk-upgrade-fails-with-permission-denied)** — usually move aside the stale `~/jdk.tar.gz` and `~/jdk`, then `gw installjdk`.

### Simple Upgrade

#### Python Way

```shell
pip install pygeoweaver --upgrade
gw start --force
```

`--force` re-downloads the newest Geoweaver jar before start. The host JDK must already be 17+.

#### Java Way

```shell
wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
java -jar geoweaver.jar
```

Use a Java 17+ runtime for the `java` command above.

### Staying on Geoweaver 2.1.x (Java 11)

```shell
# Example — pin a known 2.1.x jar (adjust version as needed)
wget https://github.com/ESIPFed/Geoweaver/releases/download/v2.1.7/geoweaver.jar
java -jar geoweaver.jar
```

## Complex Upgrade (Database Migration)

If you have used Geoweaver for a while, protect your database during major upgrades. Geoweaver’s default store is H2. When the bundled H2 library changes, you may need to migrate.

Official H2 migration notes: [Migration to H2 v2](https://www.h2database.com/html/migration-to-v2.html)

Steps:

1. Download H2 JARs for the old and new versions (`<old_version>` / `<new_version>`):

```shell
wget https://github.com/h2database/h2database/releases/download/version-<old_version>/h2-<old_version>.jar
wget https://github.com/h2database/h2database/releases/download/version-<new_version>/h2-<new_version>.jar
```

2. Export data (replace password):

```shell
java -cp h2-<old_version>.jar org.h2.tools.Script -url jdbc:h2:~/h2/gw -user geoweaver -script old_gw_db.sql -password <DB_Password>
```

3. Remove old DB files (after a successful export):

```shell
rm -f ~/h2/*
```

4. Import into a new DB:

```shell
java -cp h2-<new_version>.jar org.h2.tools.RunScript -url jdbc:h2:~/h2/gw -user geoweaver -script old_gw_db.sql -password <DB_Password>
```

5. Start the new Geoweaver jar with Java 17+:

```shell
wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
java -jar geoweaver.jar
```

Questions: [GitHub Issues](https://github.com/ESIPFed/Geoweaver/issues).
