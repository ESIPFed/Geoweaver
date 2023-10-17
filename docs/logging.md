## Logging in Geoweaver

Geoweaver is developed using Spring Boot library, and its logging configuration is very similar to [Spring Boot Logging](https://docs.spring.io/spring-boot/docs/2.1.13.RELEASE/reference/html/boot-features-logging.html).

## Change Logging Level in Source Code

1. Open Geoweaver folder in VS Code or any IDE.

2. Open the file `Geoweaver/src/main/resources/log4j.properties`

3. Change the logging level in the following line to desired level:

```yaml
log4j.logger.com.gw=DEBUG
```

4. Save the `log4j.properties`. Restart Geoweaver by running `Geoweaver/src/main/java/com/gw/GeoweaverApplication.java`.

You should now see logs printed in the files of `com.gw` packages, e.g.:

```log
INFO    2023-10-17 18:34:56,912 0       com.gw.GeoweaverApplication     [restartedMain] Starting GeoweaverApplication using Java 17.0.5 on jS-M1.local with PID 58059 (/Users/joe/Documents/GitHub/Geoweaver/target/classes started by joe in /Users/joe/Documents/GitHub/Geoweaver)
DEBUG   2023-10-17 18:34:56,913 1       com.gw.GeoweaverApplication     [restartedMain] Running with Spring Boot v2.4.1, Spring v5.3.27
INFO    2023-10-17 18:34:56,913 1       com.gw.GeoweaverApplication     [restartedMain] No active profile set, falling back to default profiles: default
DEBUG   2023-10-17 18:35:08,111 11199   com.gw.workers.WorkerManager    [restartedMain] The worknumber setting: 5
DEBUG   2023-10-17 18:35:08,111 11199   com.gw.workers.WorkerManager    [restartedMain] worker manager created a worker 
DEBUG   2023-10-17 18:35:08,111 11199   com.gw.workers.WorkerManager    [restartedMain] worker manager created a worker 
DEBUG   2023-10-17 18:35:08,111 11199   com.gw.workers.WorkerManager    [restartedMain] worker manager created a worker 
DEBUG   2023-10-17 18:35:08,111 11199   com.gw.workers.WorkerManager    [restartedMain] worker manager created a worker 
DEBUG   2023-10-17 18:35:08,111 11199   com.gw.workers.WorkerManager    [restartedMain] worker manager created a worker 
DEBUG   2023-10-17 18:35:08,348 11436   com.gw.web.GoogleEarthController        [restartedMain] A new Google Earth restTemplate is created
DEBUG   2023-10-17 18:35:08,353 11441   com.gw.web.JupyterController    [restartedMain] A new restTemplate is created
DEBUG   2023-10-17 18:35:08,510 11598   com.gw.server.JupyterRedirectServlet    [restartedMain] Initializing Jupyter Websocket Session...
DEBUG   2023-10-17 18:35:08,511 11599   com.gw.server.JupyterHubRedirectServlet [restartedMain] Initializing JupyterHub Websocket Session...
DEBUG   2023-10-17 18:35:08,511 11599   com.gw.server.JupyterLabRedirectServlet [restartedMain] Initializing Jupyter Lab Websocket Session...
INFO    2023-10-17 18:35:08,534 11622   com.gw.ssh.SecurityConfiguration$$EnhancerBySpringCGLIB$$9e6bebbd       [restartedMain] registering SSH authentication provider
INFO    2023-10-17 18:35:09,091 12179   com.gw.GeoweaverApplication     [restartedMain] Started GeoweaverApplication in 17.375 seconds (JVM running for 17.681)
DEBUG   2023-10-17 18:35:09,135 12223   com.gw.commands.TopEntryCommand [restartedMain] should print out all supported commands
INFO    2023-10-17 18:35:09,163 12251   com.gw.GeoweaverApplication     [restartedMain] Public user exists.
DEBUG   2023-10-17 18:35:09,163 12251   com.gw.GeoweaverApplication     [restartedMain] test what is going on
DEBUG   2023-10-17 18:35:09,163 12251   com.gw.tools.UserTool   [restartedMain] Belong the no-owner resources to public user..
INFO    2023-10-17 18:35:09,270 12358   com.gw.GeoweaverApplication     [restartedMain] Localhost exists.
INFO    2023-10-17 18:35:09,270 12358   com.gw.utils.BaseTool   [restartedMain] get existing workspace dir: ~/gw-workspace
INFO    2023-10-17 18:35:09,270 12358   com.gw.utils.BaseTool   [restartedMain] new workspace dir: ~/gw-workspace
GeoWeaver is started and ready for use..
URL: http://localhost:8070/Geoweaver
```