## Logging in Geoweaver

Geoweaver is built with Spring Boot. Logging follows the usual Spring Boot approach; see the current [Spring Boot logging documentation](https://docs.spring.io/spring-boot/reference/features/logging.html) (Boot 3.x).

## Change Logging Level in Source Code

1. Open the Geoweaver project in your IDE.

2. Open `src/main/resources/log4j.properties` (or the active Logback / logging config used by your build).

3. Set the desired level for `com.gw`, for example:

```properties
log4j.logger.com.gw=DEBUG
```

4. Restart Geoweaver (from the IDE or `java -jar …` with **Java 17+**).

Example startup lines on a modern runtime:

```log
INFO  com.gw.GeoweaverApplication - Starting GeoweaverApplication using Java 17 ...
INFO  com.gw.GeoweaverApplication - Running with Spring Boot v3.3.x
INFO  com.gw.GeoweaverApplication - Started GeoweaverApplication
GeoWeaver is started and ready for use..
URL: http://localhost:8070/Geoweaver
```

Default file logs (when configured) typically live under `~/geoweaver/logs/`.
