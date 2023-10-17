
## Upgrading Geoweaver to Latest Version

### Simple Upgrade

Upgrading Geoweaver to the latest version is a straightforward process. Follow these steps to perform a simple upgrade:

- Download the new version of Geoweaver's JAR file:

```shell
wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
```

- Start Geoweaver with the updated JAR file:

```shell
java -jar geoweaver.jar
```

## Complex Upgrade (Database Migration)

If you have been using Geoweaver for a while, it's important to ensure the safety of your database during the upgrade. Geoweaver relies on the H2 database as its default database. In some cases, the new version of Geoweaver may introduce changes to the H2 database library for security reasons. In such scenarios, users may need to migrate their Geoweaver database.

The official documentation for H2 database migration is available [here](https://www.h2database.com/html/migration-to-v2.html) 

Here are the steps to migrate your Geoweaver database:

1. Begin by downloading the H2 database JAR file of both the old version and the current version. Replace <old_version> and <new_version> respectively: 

```shell
wget https://github.com/h2database/h2database/releases/download/version-<old_version>/h2-<old_version>.jar
wget https://github.com/h2database/h2database/releases/download/version-<new_version>/h2-<new_version>.jar
```

2. Export your data into an SQL script. Make sure to replace <old_version> and <DB_password> with your specific information:

```shell
java -cp h2-<old_version>.jar org.h2.tools.Script -url jdbc:h2:~/h2/gw -user geoweaver -script old_gw_db.sql -password <DB_Password>
```

3. Remove the old database files:

```shell
rm ~/h2/* -f
```

4. Import the data back and create a new Geoweaver database. Use the following command, replacing <new_version> and <DB_password> with the appropriate values:

```shell
java -cp h2-<new_version>.jar org.h2.tools.RunScript -url jdbc:h2:~/h2/gw -user geoweaver -script old_gw_db.sql -password <DB_password>
```

5. That's it! Download the new version of the Geoweaver JAR file and restart. Your Geoweaver installation should now work as usual:

```shell
wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
java -jar geoweaver.jar
```

These steps should help you smoothly upgrade Geoweaver to the latest version while safeguarding your database. Any questions please feel free to report them [here](https://github.com/ESIPFed/Geoweaver/issues).
