package com.gw;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.api.client.util.Value;
import com.gw.jpa.GWUser;
import com.gw.jpa.Host;
import com.gw.tools.HostTool;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.Banner;
 
@SpringBootApplication
@ServletComponentScan
public class GeoweaverApplication {

    static Logger logger = Logger.getLogger(GeoweaverApplication.class);

    @Value("${geoweaver.workspace}")
    private static String workspace;

    public static void main(String[] args) {
		
//		BasicConfigurator.configure();

        // if we have a command line argument, we assume it is a command
        if(args.length > 0) {

            // Do not open homepage if we are running a command
            // Run the spring boot application and command it to exit, so that only the command is run
            // Create a spring boot application without tomcat
            //System.exit(SpringApplication.exit(new SpringApplication(GeoweaverCLI.class).run(args)));
            // System.exit(SpringApplication.exit(new SpringApplicationBuilder(GeoweaverApplication.class).web(WebApplicationType.NONE).run(args)));
            System.exit(SpringApplication.exit(new SpringApplicationBuilder(GeoweaverCLI.class)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args)));


        }else{

            ApplicationContext applicationContext = new SpringApplicationBuilder(GeoweaverApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

            // openHomePage();	

            addDefaultPublicUser();

            addLocalhost();

            System.out.println("GeoWeaver is started and ready for use..");
            System.out.println("URL: http://localhost:8070/Geoweaver");

        }
		
		
    
	}

    public static void addLocalhost(){

        HostTool ht = BeanTool.getBean(HostTool.class);

        Host h = ht.getHostById("100001");

        if(h==null){

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

        }else{

            logger.info("Localhost exists.");

        }

        // read password file
        try{

            BaseTool bt = BeanTool.getBean(BaseTool.class);

            if(BaseTool.isNull(bt.getLocalhostPassword())){

                String initialpassword = new RandomString(30).nextString();

                logger.warn("\n============\n");

                logger.warn("Default password for Localhost: \n\n    " + initialpassword+ "\n\n");

                logger.warn("Please copy and save the password in a safe place");

                logger.warn("Change password: <java -jar geoweaver.jar resetpassword>");

                logger.warn("\n============\n");
                
                bt.setLocalhostPassword(initialpassword, false);

            }

            
            
        }catch(Exception e){

            e.printStackTrace();

        }
        


    }

    public static void addDefaultPublicUser(){

        // fixed public user "public_user", id: "111111"
        // all the created resources will be assigned to this user
        
        UserTool ut = BeanTool.getBean(UserTool.class);

        GWUser publicuser = ut.getUserById("111111");

        if(publicuser==null){

            logger.warn("Public User doesn't exist. Adding now..");

            publicuser = new GWUser();

            publicuser.setEmail("publicuser@geoweaver.com");

            publicuser.setId("111111");

            publicuser.setIsactive(true);

            publicuser.setPassword("wsorpwiuerkls;kldjfuiwperuewsldjf;lks"); //none can decrypt this code

            publicuser.setUsername("publicuser");

            ut.save(publicuser);

        }else{

            logger.info("Public user exists.");

        }

        logger.debug("test what is going on");

        //set everything that doesn't have an owner to this user
        ut.belongToPublicUser();

    }

    public static void openHomePage(){

        browse("http://localhost:8070/Geoweaver/");

    }
	
    public static void browse(String url) {
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
  
}
  

