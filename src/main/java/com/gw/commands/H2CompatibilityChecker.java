package com.gw.commands;

import com.gw.utils.BeanTool;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class represents a command-line command for checking the compatibility of an H2 database
 * file with the current Geoweaver system. It uses the picocli library to define the command and its
 * parameters. When executed, it checks the database schema for compatibility.
 */

@Command(name = "checkH2Compatibility", description = "Check compatibility of an H2 database file with Geoweaver")
@Component
public class H2CompatibilityChecker implements Runnable {

//    @Parameters(index = "0", description = "Path to the H2 database file")
//    String dbFilePath;

    /**
     * The run method is called when this command is executed. It checks the compatibility of the H2
     * database file specified by dbFilePath and prints the result.
     */
    @Override
    public void run() {
        // Load the H2 Driver
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 Driver not found. Make sure H2 is included in the classpath.");
            System.exit(1);
            return;
        }

        // Connect to the database
        try (Connection conn = DriverManager.getConnection("jdbc:h2:file:~/h2/gw", "geoweaver", "DFKHH9V6ME");
             Statement stmt = conn.createStatement()) {

            // Check for a specific table and columns (replace with actual checks)
            ResultSet rs = stmt.executeQuery("SHOW TABLES");

            if (rs.next()) {
                System.out.println(rs.getString(1));
                // Perform additional checks if necessary
                System.out.println("The H2 database is compatible with Geoweaver.");
                System.exit(0);
            } else {
                System.out.println("The H2 database is not compatible with Geoweaver.");
                System.exit(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error connecting to the H2 database or executing the query.");
            System.exit(1);
        }
    }
}
