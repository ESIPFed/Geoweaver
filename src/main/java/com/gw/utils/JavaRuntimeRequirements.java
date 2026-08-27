package com.gw.utils;

/**
 * Runtime Java version guard for Geoweaver 2.2+ (Spring Boot 3).
 *
 * <p>Java 17 is the minimum supported runtime. Users who cannot upgrade should stay on Geoweaver
 * 2.1.x, which still runs on Java 11.
 */
public final class JavaRuntimeRequirements {

  public static final int MIN_MAJOR_VERSION = 17;

  /** Last major line that supported Java 11 / Spring Boot 2. */
  public static final String LEGACY_JAVA11_LINE = "Geoweaver 2.1.x";

  public static final String RELEASES_URL = "https://github.com/ESIPFed/Geoweaver/releases";

  public static final String LEGACY_JAR_EXAMPLE =
      "https://github.com/ESIPFed/Geoweaver/releases/download/v2.1.7/geoweaver.jar";

  private JavaRuntimeRequirements() {}

  public static int detectedMajorVersion() {
    return Runtime.version().feature();
  }

  public static boolean isSupported() {
    return detectedMajorVersion() >= MIN_MAJOR_VERSION;
  }

  /** Print a clear console warning for JDK &lt; 17 and instruct how to stay on legacy releases. */
  public static void printUnsupportedJavaWarning(int major) {
    System.err.println();
    System.err.println("========================================================================");
    System.err.println("  Geoweaver WARNING: Unsupported Java version");
    System.err.println("========================================================================");
    System.err.println("  Detected Java major version: " + major);
    System.err.println(
        "  Latest Geoweaver (2.2+ / Spring Boot 3) requires Java "
            + MIN_MAJOR_VERSION
            + " or newer.");
    System.err.println("  JDK versions older than " + MIN_MAJOR_VERSION + " are no longer supported.");
    System.err.println();
    System.err.println("  If you cannot bump your JDK, use an older Geoweaver release instead:");
    System.err.println("    - Stay on " + LEGACY_JAVA11_LINE + " (Java 11 compatible)");
    System.err.println("    - Releases: " + RELEASES_URL);
    System.err.println("    - Example jar: " + LEGACY_JAR_EXAMPLE);
    System.err.println();
    System.err.println("  PyGeoWeaver users: pin an older Geoweaver jar / older pygeoweaver,");
    System.err.println("  or upgrade the system JDK to 17+ before running `gw start`.");
    System.err.println("========================================================================");
    System.err.println();
  }

  /** Exit the process if the runtime is below the supported Java major version. */
  public static void requireSupportedJavaOrExit() {
    int major = detectedMajorVersion();
    if (major < MIN_MAJOR_VERSION) {
      printUnsupportedJavaWarning(major);
      System.exit(1);
    }
  }
}
