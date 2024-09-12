package com.gw.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class H2Util {
    
    @Autowired
    private DataSource dataSource;

    public void defragH2Database() {
        try (Connection connection = dataSource.getConnection()) {
            // Execute the SHUTDOWN DEFRAG command
            connection.createStatement().execute("SHUTDOWN DEFRAG");
            System.out.println("H2 database defragmented successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
