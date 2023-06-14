package com.gw.commands;

import java.io.Console;
import java.util.Arrays;

import com.gw.utils.BaseTool;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "resetpassword", description = "Reset password of local host")
@Component
public class PasswordResetCommand implements Runnable {

    @Option(names = { "-p", "--password" }, description = "the new password")
    String new_password;

    public void run() {

        BaseTool bt = new BaseTool();

        if(new_password != null){

            bt.setLocalhostPassword(new_password, true);

            System.out.println("NOTE: Password updated.");

        }else{

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

                bt.setLocalhostPassword(originalpassword, true);

                System.out.println("NOTE: Password updated.");

            }else{

                System.err.println("ERROR: The two entered passwords don't match.");

            }

        }

        

    }
}
