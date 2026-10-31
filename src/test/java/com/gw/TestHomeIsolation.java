package com.gw;

import java.io.File;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Keeps every test away from the developer's real Geoweaver files (~/geoweaver/.secret, ~/h2,
 * ~/gw-workspace). Surefire already sets user.home to target/test-home; this extension is loaded
 * automatically for every test (see junit-platform.properties), so the same holds when a test is
 * started from an IDE.
 */
public class TestHomeIsolation implements BeforeAllCallback {

  static final String TEST_HOME_DIR = "test-home";

  static {
    isolate();
  }

  static synchronized void isolate() {
    File current = new File(System.getProperty("user.home", ""));
    if (!TEST_HOME_DIR.equals(current.getName())) {
      File home = new File(System.getProperty("user.dir"), "target" + File.separator + TEST_HOME_DIR);
      System.setProperty("user.home", home.getAbsolutePath());
    }
    File home = new File(System.getProperty("user.home"));
    if (!home.isDirectory() && !home.mkdirs()) {
      throw new IllegalStateException("Cannot create isolated test home " + home);
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    isolate();
  }
}
