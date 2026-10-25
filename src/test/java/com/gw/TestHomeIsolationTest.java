package com.gw;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.utils.BaseTool;
import java.io.File;
import org.junit.jupiter.api.Test;

class TestHomeIsolationTest {

  @Test
  void testsNeverUseTheRealHomeFolder() {
    File home = new File(System.getProperty("user.home"));
    assertThat(home.getName()).isEqualTo(TestHomeIsolation.TEST_HOME_DIR);
    String realHome = System.getenv("HOME");
    if (realHome != null) {
      assertThat(home.getAbsoluteFile()).isNotEqualTo(new File(realHome).getAbsoluteFile());
    }
    assertThat(new BaseTool().getGeoweaverConfigFolder()).startsWith(home.getAbsolutePath());
  }
}
