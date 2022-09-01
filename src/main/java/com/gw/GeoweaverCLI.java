package com.gw;

import com.gw.commands.TopEntryCommand;
import com.gw.ssh.SSHSessionImpl;
import com.gw.tools.BuiltinTool;
import com.gw.utils.BeanTool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
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
"com.gw.utils",  "com.gw.workers", "com.gw.ssh",})
public class GeoweaverCLI implements CommandLineRunner {

    @Autowired
    BeanTool beantool;


    public void displayAllBeans() {
        String[] allBeanNames = beantool.getApplicationContext().getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            System.out.println(beanName);
        }
    }

    @Override
    public void run(String... args) {

        TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);

        new CommandLine(topEntryCommand).execute(args);

    }

    public static void main(String[] args) throws Exception {
        
        System.exit(SpringApplication.exit(new SpringApplicationBuilder(GeoweaverCLI.class)
                                    .web(WebApplicationType.NONE)
                                    .bannerMode(Banner.Mode.OFF)
                                    .run(args)));

		
    }
    
}
