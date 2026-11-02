package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseToolExecuteLocalTest {

  private final BaseTool bt = new BaseTool();

  @BeforeEach
  void unixOnly() {
    assumeFalse(System.getProperty("os.name").toLowerCase().contains("win"));
  }

  @Test
  void returnsTheOutputLines() {
    assertThat(bt.executeLocal(List.of("sh", "-c", "echo one; echo two >&2"))).containsExactlyInAnyOrder("one", "two");
  }

  @Test
  void aLongOutputDoesNotBlockTheCommand() {
    // far more than a pipe buffer holds; used to hang because the output was read only at the end
    List<String> out = bt.executeLocal(List.of("sh", "-c", "i=0; while [ $i -lt 20000 ]; do echo line-$i-xxxxxxxxxxxxxxxx; i=$((i+1)); done"), 30);
    assertThat(out).hasSize(20000);
  }

  @Test
  void aCommandThatWaitsForInputEndsAtOnce() {
    long t = System.currentTimeMillis();
    bt.executeLocal(List.of("cat"), 30);
    assertThat(System.currentTimeMillis() - t).isLessThan(5000);
  }

  @Test
  void aCommandThatHangsIsStoppedAndWhatItPrintedIsKept() {
    long t = System.currentTimeMillis();
    List<String> out = bt.executeLocal(List.of("sh", "-c", "echo started; exec sleep 60"), 1);
    assertThat(System.currentTimeMillis() - t).isLessThan(10000);
    assertThat(out).containsExactly("started");
  }
}
