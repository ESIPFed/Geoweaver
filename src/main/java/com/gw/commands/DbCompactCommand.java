package com.gw.commands;

import com.gw.utils.BeanTool;
import com.gw.utils.H2StorageMaintenance;
import java.io.File;
import java.util.concurrent.Callable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "db-compact",
    description =
        "Shrink the embedded H2 database file by removing space left by old data. "
            + "Stop the Geoweaver server first.")
@Component
public class DbCompactCommand implements Callable<Integer> {

  @Option(
      names = {"--dry-run"},
      description = "only report the file size and how much of it is in use")
  boolean dryRun;

  @Override
  public Integer call() {
    Environment env = BeanTool.getBean(Environment.class);
    String url = env.getProperty("spring.datasource.url");
    String username = env.getProperty("spring.datasource.username", "");
    String password = env.getProperty("spring.datasource.password", "");
    File file = H2StorageMaintenance.databaseFile(url);
    if (file == null) {
      System.out.println("The configured database is not an embedded H2 file; nothing to do.");
      return 0;
    }
    System.out.println("Database file: " + file);
    H2StorageMaintenance.Result result =
        H2StorageMaintenance.compact(url, username, password, H2StorageMaintenance.Options.always(dryRun));
    System.out.println(result.describe());
    return result.outcome() == H2StorageMaintenance.Outcome.SKIPPED && file.isFile() ? 1 : 0;
  }
}
