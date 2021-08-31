package com.gw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.gw.jpa.GWUser;
import com.gw.tools.UserTool;
import com.gw.utils.BeanTool;
 
@SpringBootApplication
@ServletComponentScan
public class GeoweaverApplication {

	public static void main(String[] args) {
		
//		BasicConfigurator.configure();
		
		SpringApplication.run(GeoweaverApplication.class, args);
        // browse("http://localhost:8070/Geoweaver/");


//		openHomePage();	

        addDefaultPublicUser();


    
	}

    public static void addDefaultPublicUser(){

        //fixed public user "public_user", id: "111111"
        //all the created resources will be assigned to this user
        
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
  

