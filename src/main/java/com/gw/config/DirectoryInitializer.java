package com.gw.config;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class DirectoryInitializer {

    @PostConstruct
    public void createSqliteDirectory() {
        String home = System.getProperty("user.home");
        File dir = new File(home + "/geoweaver/sqlite/");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created directory: " + dir.getAbsolutePath());
            } else {
                System.err.println("Failed to create directory: " + dir.getAbsolutePath());
            }
        }
    }
} 