
## Upgrading Geoweaver

> **Before you upgrade to 2.2+:** install **Java 17+**. Latest Geoweaver will refuse to start on older JDKs.  
> Full guidance: [Java 17 migration guide](java17-migration.md).

### Existing users checklist

1. Confirm `java -version` reports **17 or higher**.
2. Back up `~/h2/` (database) and `~/gw-workspace/` (files).
3. Upgrade via Python or JAR (below).
4. If the database fails to open after the jump, use [Complex Upgrade (Database Migration)](#complex-upgrade-database-migration).

### Behavior changes in 3.2.0

- **Long process logs are kept in files, not in the database.** A log that grows past 64 KB is written to `~/geoweaver/run-logs/` (compressed when the run ends), and the database keeps only its last 64 KB. The run page and the Agent API read the full log from the file. Point `GEOWEAVER_RUN_LOG_DIR` at any disk with room. Back up this folder together with `~/h2/`. See [Database](database-configuration.md#keeping-the-built-in-h2-database-small).
- **The H2 database file is compacted at start-up when it has grown** (first start after upgrading can take a few minutes on a large file), and `geoweaver db-compact` compacts on demand.
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
