
## Upgrading Geoweaver

> **Before you upgrade to 2.2+:** install **Java 17+**. Latest Geoweaver will refuse to start on older JDKs.  
> Full guidance: [Java 17 migration guide](java17-migration.md).

### Existing users checklist

1. Confirm `java -version` reports **17 or higher**.
2. Back up `~/h2/` (database) and `~/gw-workspace/` (files).
3. Upgrade via Python or JAR (below).
4. If the database fails to open after the jump, use [Complex Upgrade (Database Migration)](#complex-upgrade-database-migration).

If you **cannot** use Java 17, **do not upgrade** to 2.2+. Keep **Geoweaver 2.1.x** from [Releases](https://github.com/ESIPFed/Geoweaver/releases).

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
