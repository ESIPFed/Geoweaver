package com.gw.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.server.ResponseStatusException;

/**
 * Output of the command line for people and for programs.
 *
 * <p>With {@code --json} anywhere on the command line, standard output carries exactly one JSON
 * document (the same shapes as the Agent API) and everything else, including log lines, goes to
 * standard error. Errors are printed as {@code {"error": "...", "status": 404}} and the command
 * exits with 1.
 */
public final class CliOutput {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  /** Standard output as it was before {@link #begin} moved other output to standard error. */
  private static PrintStream out = System.out;

  private static boolean json;

  private static volatile int exitCode;

  private CliOutput() {}

  /** Called first thing in main. */
  public static void begin(String[] args) {
    for (String a : args) {
      if ("--json".equals(a)) json = true;
    }
    if (json) {
      out = System.out;
      System.setOut(System.err); // start-up messages and logs must not mix with the JSON
    }
  }

  public static boolean json() {
    return json;
  }

  public static PrintStream out() {
    return out;
  }

  /** Prints a result: JSON in --json mode, otherwise JSON as well (readable, and easy to reuse). */
  public static void print(Object value) {
    try {
      out.println(MAPPER.writeValueAsString(value));
    } catch (Exception e) {
      out.println(String.valueOf(value));
    }
    out.flush();
  }

  /** Prints a plain line to standard output (never in --json mode). */
  public static void line(String text) {
    if (!json) out.println(text);
  }

  /** Reports an error and sets exit code 1. */
  public static void fail(Throwable t) {
    int status = 1;
    String message;
    if (t instanceof ResponseStatusException rse) {
      status = rse.getStatusCode().value();
      message = rse.getReason() != null ? rse.getReason() : rse.getMessage();
    } else {
      message = t.getMessage() != null ? t.getMessage() : t.toString();
    }
    fail(message, status);
  }

  public static void fail(String message, int status) {
    Map<String, Object> err = new LinkedHashMap<>();
    err.put("error", message);
    if (status > 1) err.put("status", status);
    if (json) print(err);
    else System.err.println("Error: " + message);
    setExitCode(1);
  }

  public static void setExitCode(int code) {
    if (code > exitCode) exitCode = code;
  }

  public static int exitCode() {
    return exitCode;
  }

  static ObjectMapper mapper() {
    return MAPPER;
  }
}
