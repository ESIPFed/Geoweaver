package com.gw.tools;

import java.io.Console;

import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResetPassword {

    public static void main(String[] args){
        
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

    }
    
}
