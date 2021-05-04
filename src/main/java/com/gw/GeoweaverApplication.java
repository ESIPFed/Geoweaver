package com.gw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

 import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
 
@SpringBootApplication
@ServletComponentScan
public class GeoweaverApplication {

	public static void main(String[] args) {
		
//		BasicConfigurator.configure();
		
		SpringApplication.run(GeoweaverApplication.class, args);
        // browse("http://localhost:8070/Geoweaver/");


//		openHomePage();	


    
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
  

