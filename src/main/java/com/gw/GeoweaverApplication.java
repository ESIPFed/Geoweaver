package com.gw;

import java.awt.Desktop;
import java.io.Console;
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
 
@SpringBootApplication
@ServletComponentScan
public class GeoweaverApplication {

    @Value("${geoweaver.workspace}")
    private static String           workspace;


	public static void main(String[] args) {
		
//		BasicConfigurator.configure();

        if(args.length==1 && "resetpassword".equals(args[0])){

            Console console = System.console();
            if (console == null) {
                System.out.println("Couldn't get Console instance");
                System.exit(0);
            }

            console.printf("Reset Geoweaver Localhost password%n");
            char[] passwordArray = console.readPassword("Enter your secret password: ");
            // console.printf("Password entered was: %s%n", new String(passwordArray));


            String originalpassword = new String(passwordArray);
        
            BaseTool bt = new BaseTool();

            bt.setLocalhostPassword(originalpassword, true);

            console.printf("Password updated.");

        }else{

            SpringApplication.run(GeoweaverApplication.class, args);

            openHomePage();	

            addDefaultPublicUser();

            addLocalhost();

        }
		
		
    
	}

    public static void addLocalhost(){

        HostTool ht = BeanTool.getBean(HostTool.class);

        Host h = ht.getHostById("100001");

        if(h==null){

            System.out.println("Localhost doesn't exist. Adding now..");

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

            System.out.println("Localhost exists.");

        }

        // read password file
        try{

            BaseTool bt = BeanTool.getBean(BaseTool.class);

            if(bt.isNull(bt.getLocalhostPassword())){

                String initialpassword = new RandomString(30).nextString();

                System.out.println("\n~~~~~~~~~\n");

                System.out.println("Default Password for Localhost Execution: " + initialpassword+ "\n");

                System.out.println("Please copy and remember the password in a safe place. This message only shows once.");

                System.out.println("To change the password, please use command: java -jar geoweaver.jar resetpassword");

                System.out.println("\n~~~~~~~~~\n");
                
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

            System.out.println("Public User doesn't exist. Adding now..");

            publicuser = new GWUser();

            publicuser.setEmail("publicuser@geoweaver.com");

            publicuser.setId("111111");

            publicuser.setIsactive(true);

            publicuser.setPassword("wsorpwiuerkls;kldjfuiwperuewsldjf;lks"); //none can decrypt this code

            publicuser.setUsername("publicuser");

            ut.save(publicuser);

        }else{

            System.out.println("Public user exists.");

        }

        System.out.println("test what is going on");

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
  

