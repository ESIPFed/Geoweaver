package com.gw;

import com.google.api.client.util.Value;
import com.gw.jpa.GWUser;
import com.gw.jpa.Host;
import com.gw.tools.HostTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ServletComponentScan
@EnableSwagger2
public class GeoweaverApplication {

  static Logger logger = Logger.getLogger(GeoweaverApplication.class);

  @Value("${geoweaver.workspace}")
  private static String workspace;

  public static void main(String[] args) {
    // Create log directory before anything else to avoid NoSuchFileException
    createLogDirectory();
    
    // if we have a command line argument, we assume it is a command
    if (args.length > 0) {

      // Do not open homepage if we are running a command
      // Run the spring boot application and command it to exit, so that only the command is run
      // Create a spring boot application without tomcat
      System.exit(
          SpringApplication.exit(
              new SpringApplicationBuilder(GeoweaverCLI.class)
                  .lazyInitialization(true)
                  .web(WebApplicationType.NONE)
                  .bannerMode(Banner.Mode.OFF)
                  .run(args)));

    } else {

      if (BaseTool.isPortInUse("localhost", BaseTool.get_current_port())) {
        System.out.println("Port " + BaseTool.get_current_port() + " is already used. Cannot start Geoweaver.");
        System.out.println("Could set the environment variable GEOWEAVER_PORT to another port and try again.");
        System.exit(1);
      }

      show_ascii_art();

      ApplicationContext applicationContext =
          new SpringApplicationBuilder(GeoweaverApplication.class)
              .bannerMode(Banner.Mode.OFF)
              .run(args);

      addDefaultPublicUser();

      addLocalhost();


      System.out.println("GeoWeaver is started and ready for use..");
      System.out.println("URL: http://localhost:"+BaseTool.get_current_port()+"/Geoweaver");
    }
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

  @Bean
  public Docket geoweaverAPI() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build();
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
