package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  JARVIS CONFIG                                          ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Central settings for Jarvis system                     ║
// ║  Paths, timeouts, package locations                     ║
// ╚══════════════════════════════════════════════════════════╝

public class JarvisConfig {

    // Project root — used to build absolute paths
    public static final String PROJECT_ROOT =
            System.getProperty("user.dir");

    // Where generated test classes are saved
    public static final String TEST_PACKAGE_PATH =
            "src/test/java/com/framework/tests/android";

    // Where generated page objects are saved
    public static final String PAGE_PACKAGE_PATH =
            "src/main/java/com/framework/pages/common";

    // Java package names for generated code
    public static final String TEST_PACKAGE_NAME =
            "com.framework.tests.android";

    public static final String PAGE_PACKAGE_NAME =
            "com.framework.pages.common";

    // Java home — used to run Maven commands
    public static final String JAVA_HOME =
            "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home";

    // Timeouts
    public static final int MAVEN_TIMEOUT_SECONDS = 180;
    public static final int CLAUDE_TIMEOUT_SECONDS = 60;

    // Allure results location
    public static final String ALLURE_RESULTS_PATH =
            "target/allure-results";

    private JarvisConfig() {} // utility class
}