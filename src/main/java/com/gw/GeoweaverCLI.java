package com.gw;

import com.gw.commands.TopEntryCommand;
import com.gw.utils.BeanTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import picocli.CommandLine;

/**
 * Besides clean code structure, another major reason to separate the CLI into a single 
 * springbootapplication class is because some of the web environment classes 
 * (e.g., com.gw.server) should not be instantiated 
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.gw.database"})
@ComponentScan(basePackages = {"com.gw.commands", "com.gw.database", "com.gw.jpa", 
"com.gw.local", "com.gw.search", "com.gw.tasks", "com.gw.tools", "com.gw.user",
"com.gw.utils",  "com.gw.workers"})
public class GeoweaverCLI implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) {

        //pass the context otherwise it will be lost after new CommandLine
        BeanTool.setCLIContext(applicationContext); 

        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);

        new CommandLine(topEntryCommand).execute(args);

    }

    public static void main(String[] args) throws Exception {
        
        SpringApplicationBuilder builder = new SpringApplicationBuilder(GeoweaverCLI.class);

        ApplicationContext applicationContext = builder.web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args);

        System.exit(SpringApplication.exit(applicationContext));
		
    }
    
}
