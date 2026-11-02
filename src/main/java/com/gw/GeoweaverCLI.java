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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import picocli.CommandLine;

/**
 * Besides clean code structure, another major reason to separate the CLI into a single
 * springbootapplication class is because some of the web environment classes (e.g., com.gw.server)
 * should not be instantiated
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.gw.database"})
@ComponentScan(
    basePackages = {
      "com.gw.commands",
      "com.gw.database",
      "com.gw.jpa",
      "com.gw.local",
      "com.gw.search",
      "com.gw.tasks",
      "com.gw.tools",
      "com.gw.user",
      "com.gw.utils",
      "com.gw.workers",
      "com.gw.ssh",
      // Agent API token mint (agent-token CLI). Web-only beans are
      // @ConditionalOnWebApplication so they stay out of CLI mode.
      "com.gw.api.v1",
    },
    lazyInit = true)
public class GeoweaverCLI implements CommandLineRunner {

  @Autowired BeanTool beantool;

  public void displayAllBeans() {
    String[] allBeanNames = beantool.getApplicationContext().getBeanDefinitionNames();
    for (String beanName : allBeanNames) {
      System.out.println(beanName);
    }
  }

  @Override
  public void run(String... args) {

    TopEntryCommand topEntryCommand = BeanTool.getBean(TopEntryCommand.class);

    // A database used only from the command line (no server ever started) still needs the
    // default user and the localhost host that runs default to.
    try {
      GeoweaverApplication.addDefaultPublicUser();
      GeoweaverApplication.addLocalhost();
    } catch (RuntimeException e) {
      // read-only or older database: commands report their own errors
    }

    // --json is handled before start-up (CliOutput) and may appear anywhere
    String[] rest = java.util.Arrays.stream(args).filter(a -> !"--json".equals(a)).toArray(String[]::new);
    int rc = new CommandLine(topEntryCommand).execute(rest);
    com.gw.commands.CliOutput.setExitCode(rc);
  }

  public static void main(String[] args) throws Exception {
    com.gw.utils.JavaRuntimeRequirements.requireSupportedJavaOrExit();

    System.exit(
        SpringApplication.exit(
            new SpringApplicationBuilder(GeoweaverCLI.class)
                .lazyInitialization(true)
                .web(WebApplicationType.NONE)
                .bannerMode(Banner.Mode.OFF)
                .run(args)));
  }
}
