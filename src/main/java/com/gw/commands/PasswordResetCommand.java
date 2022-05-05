package com.gw.commands;

import java.io.Console;
import java.util.Arrays;

import com.gw.utils.BaseTool;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Command(name = "resetpassword")
@Component
public class PasswordResetCommand implements Runnable {

    public void run() {
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }

        console.printf("Reset Geoweaver Localhost password%n");
        char[] passwordArray = console.readPassword("Enter password: ");
        // console.printf("Password entered was: %s%n", new String(passwordArray));
        char[] secondpasswordArray = console.readPassword("Retype password: ");

        if(Arrays.equals(passwordArray, secondpasswordArray)){

            String originalpassword = new String(passwordArray);
        
            BaseTool bt = new BaseTool();

            bt.setLocalhostPassword(originalpassword, true);

            System.out.println("NOTE: Password updated.");

        }else{

            System.err.println("ERROR: The two entered passwords don't match.");

        }

    }
}
