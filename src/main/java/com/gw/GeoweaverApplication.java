package com.gw;

import com.gw.jpa.GWUser;
import com.gw.jpa.Host;
import com.gw.tools.HostTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.H2StartupCompaction;
import com.gw.utils.SharedDatabaseCheck;
import com.gw.utils.JavaRuntimeRequirements;
import com.gw.utils.RandomString;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@ServletComponentScan
public class GeoweaverApplication {

  static Logger logger = Logger.getLogger(GeoweaverApplication.class);

  /** The command-line arguments of this process, for commands that start a copy of themselves. */
  public static String[] cliArgs = new String[0];

  public static void main(String[] args) {
    cliArgs = args.clone();
    if (args.length > 0) {
      com.gw.commands.CliOutput.begin(args);
      // Commands show warnings and errors only; the log file keeps everything.
      if (System.getProperty("GEOWEAVER_CONSOLE_LOG_LEVEL") == null) {
        System.setProperty("GEOWEAVER_CONSOLE_LOG_LEVEL", "WARN");
      }
    }
    // Create log directory before anything else to avoid NoSuchFileException
    createLogDirectory();
    // Latest Geoweaver requires Java 17+ (Spring Boot 3); refuse older JDKs early.
    JavaRuntimeRequirements.requireSupportedJavaOrExit();
    applyDatabaseBindAddress();

    // if we have a command line argument, we assume it is a command
    if (args.length > 0) {

      // Do not open homepage if we are running a command
      // Run the spring boot application and command it to exit, so that only the command is run
      // Create a spring boot application without tomcat
      ConfigurableApplicationContext cli;
      try {
        cli =
            new SpringApplicationBuilder(GeoweaverCLI.class)
                .lazyInitialization(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                // Same database settings as the server, so either one can hold the shared file.
                .listeners(
                    new SharedDatabaseCheck(),
                    (ApplicationListener<ApplicationEnvironmentPreparedEvent>)
                        e -> H2StartupCompaction.enableCompression(e.getEnvironment()))
                .run(args);
      } catch (RuntimeException e) {
        String inUse = databaseInUse(e);
        if (inUse == null) throw e;
        System.err.println(inUse);
        System.exit(1);
        return;
      }
      int code = SpringApplication.exit(cli);
      System.exit(code != 0 ? code : com.gw.commands.CliOutput.exitCode());

    } else {

      if (BaseTool.isPortInUse("localhost", BaseTool.get_current_port())) {
        System.out.println("Port " + BaseTool.get_current_port() + " is already used. Cannot start Geoweaver.");
        System.out.println("Could set the environment variable GEOWEAVER_PORT to another port and try again.");
        System.exit(1);
      }

      show_ascii_art();

      try {
        new SpringApplicationBuilder(GeoweaverApplication.class)
            .bannerMode(Banner.Mode.OFF)
            // Shrink an oversized embedded H2 file before the connection pool opens it.
            .listeners(new SharedDatabaseCheck(), new H2StartupCompaction())
            .run(args);
      } catch (RuntimeException e) {
        String inUse = databaseInUse(e);
        if (inUse == null) throw e;
        System.err.println(inUse);
        System.exit(1);
        return;
      }

      addDefaultPublicUser();

      com.gw.web.GeoweaverController.watchStopRequests();

      addLocalhost();


      System.out.println("GeoWeaver is started and ready for use..");
      System.out.println("URL: http://localhost:"+BaseTool.get_current_port()+"/Geoweaver");
    }
  }

  /**
   * The address the shared database is served on to other Geoweaver processes (H2 {@code
   * AUTO_SERVER}). By default H2 uses this machine's network address, which lets a Geoweaver on
   * another machine with the same home folder join; {@code GEOWEAVER_DB_BIND_ADDRESS=127.0.0.1}
   * keeps it on this machine, which also survives network changes (a laptop on a new Wi-Fi).
   */
  static void applyDatabaseBindAddress() {
    String bind = System.getenv("GEOWEAVER_DB_BIND_ADDRESS");
    if (bind != null && !bind.isBlank() && System.getProperty("h2.bindAddress") == null) {
      System.setProperty("h2.bindAddress", bind.trim());
    }
  }

  /**
   * A readable message when the command could not open the database because another Geoweaver
   * process holds it and cannot be reached, or null for any other failure.
   */
  static String databaseInUse(Throwable e) {
    for (Throwable t = e; t != null; t = t.getCause()) {
      String m = t.getMessage();
      if (m != null
          && (m.contains("Database may be already in use")
              || m.contains("The file is locked")
              || (m.contains("Connection is broken")
                  && (m.contains("timed out")
                      || m.contains("refused")
                      || m.contains("No route")
                      || m.contains("unreachable"))))) {
        return "Could not open the Geoweaver database: another Geoweaver process holds it and this"
            + " one cannot connect to it.\n" + m.lines().findFirst().orElse(m)
            + "\nUse that Geoweaver (browser page or Agent API), run this command on the machine where"
            + " it runs, or stop it and try again. See docs/ssh-and-gui.md.";
      }
    }
    return null;
  }

  /**
   * Creates the log directory structure before logging starts
   * to prevent NoSuchFileException for lock files
   */
  private static void createLogDirectory() {
    try {
      String logDir = System.getProperty("user.home") + "/geoweaver/logs";
      File logDirectory = new File(logDir);
      if (!logDirectory.exists()) {
        boolean created = logDirectory.mkdirs();
        if (created) {
          System.out.println("Created log directory: " + logDir);
        } else {
          System.err.println("Failed to create log directory: " + logDir);
        }
      }
    } catch (Exception e) {
      System.err.println("Error creating log directory: " + e.getMessage());
    }
  }

  public static void show_ascii_art(){
    // ANSI escape codes for colors
    String reset = "\u001B[0m";
    String yellow = "\u001B[33m";
    String green = "\u001B[32m";
    String blue = "\u001B[34m";
    String geoweaverArt = green+ "\n"+
            "  ____    ___   ___   __    __    ___   ____  __ __    ___  ____\n"+
            " /    T  /  _] /   \\ |  T__T  T  /  _] /    T|  T  |  /  _]|    \\ \n" +
            "Y   __j /  [_ Y     Y|  |  |  | /  [_ Y  o  ||  |  | /  [_ |  D  )\n" +
            "|  T  |Y    _]|  O  ||  |  |  |Y    _]|     ||  |  |Y    _]|    / \n" +
            "|  l_ ||   [_ |     |l  `  '  !|   [_ |  _  |l  :  !|   [_ |    \\ \n" +
            "|     ||     Tl     ! \\      / |     T|  |  | \\   / |     T|  .  Y\n" +
            "l___,_jl_____j \\___/   \\_/\\_/  l_____jl__j__j  \\_/  l_____jl__j\\_j\n\n"+reset;

    // Print Geoweaver ASCII art name and additional art
    System.out.println(blue + "Welcome to Geoweaver - A Workflow Tool for Research Productivity\n");
    System.out.println(geoweaverArt);
    System.out.println("Geoweaver is rolling out of bed and warming up its gears. Hang on.. ");
  }

  public static void addLocalhost() {

    HostTool ht = BeanTool.getBean(HostTool.class);

    Host h = ht.getHostById("100001");

    if (h == null) {

      logger.info("Localhost doesn't exist. Adding now..");

      h = new Host();

      h.setId("100001");

      h.setIp("127.0.0.1");

      h.setConfidential("FALSE");

      h.setName("Localhost");

      h.setOwner("111111");

      h.setPort("22");

      h.setType("ssh");

      h.setUrl("http://localhost/");

      h.setUsername("publicuser");

      ht.save(h);

    } else {

      logger.info("Localhost exists.");
    }

    // read password file
    try {

      BaseTool bt = BeanTool.getBean(BaseTool.class);

      if (BaseTool.isNull(bt.getLocalhostPassword())) {

        String initialpassword = new RandomString(30).nextString();

        logger.warn("\n============\n");

        logger.warn("Default password for Localhost: \n\n    " + initialpassword + "\n\n");

        logger.warn("Please copy and save the password in a safe place");

        logger.warn("Change password: <java -jar geoweaver.jar resetpassword>");

        logger.warn("\n============\n");

        bt.setLocalhostPassword(initialpassword, false);
      }

    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  public static void addDefaultPublicUser() {

    // fixed public user "public_user", id: "111111"
    // all the created resources will be assigned to this user

    UserTool ut = BeanTool.getBean(UserTool.class);

    GWUser publicuser = ut.getUserById("111111");

    if (publicuser == null) {

      logger.warn("Public User doesn't exist. Adding now..");

      publicuser = new GWUser();

      publicuser.setEmail("publicuser@geoweaver.com");

      publicuser.setId("111111");

      publicuser.setIsactive(true);

      publicuser.setPassword("wsorpwiuerkls;kldjfuiwperuewsldjf;lks"); // none can decrypt this code

      publicuser.setUsername("publicuser");

      ut.save(publicuser);

    } else {

      logger.info("Public user exists.");
    }

    // set everything that doesn't have an owner to this user
    ut.belongToPublicUser();
  }

  public static void openHomePage() {

    browse("http://localhost:8070/Geoweaver/");
  }

  public static void browse(String url) {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      try {
        desktop.browse(new URI(url));
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    } else {
      Runtime runtime = Runtime.getRuntime();
      try {
        runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
