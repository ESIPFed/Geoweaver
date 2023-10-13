
## Upgrading Geoweaver to Latest Version

Geoweaver is using H2 database as default database. If the new version of Geoweaver bumped the h2 database library for security reasons (for example, [this release](https://github.com/ESIPFed/Geoweaver/releases/tag/latest)), users might need to migrate your Geoweaver database too. The official document for H2 migration is [here](https://www.h2database.com/html/migration-to-v2.html) 

Here is how to do it for migrating Geoweaver database:

1. First, download the current in-use version of H2 database jar. Replace the `<old_version>` number with the current h2 version number. 

```shell
wget https://github.com/h2database/h2database/releases/download/version-<old_version>/h2-<old_version>.jar
```

2. Run the command to export the data into a SQL script. Replace the `<old_version>` and `<DB_password>`.

```shell
java -cp h2-<old_version>.jar org.h2.tools.Script -url jdbc:h2:~/h2/gw -user geoweaver -script old_gw_db.sql -password <DB_Password>
```

3. Remove the old database files.

```shell
rm ~/h2/* -f
```

4. Run the command below to import the data back and create new Geoweaver database. Replace `<new_version>` and `<DB_password>`.

```shell
java -cp h2-<new_version>.jar org.h2.tools.RunScript -url jdbc:h2:~/h2/gw -user geoweaver -script old_gw_db.sql -password <DB_password>
```

5. That is it. Download the new version of Geoweaver jar and restart. Should work as usual.

```shell
wget https://github.com/ESIPFed/Geoweaver/releases/download/latest/geoweaver.jar
java -jar geoweaver.jar
```
