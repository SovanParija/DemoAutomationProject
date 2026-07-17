package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  MAVEN EXECUTOR                                          ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Runs mvn commands programmatically                      ║
// ║  Captures output, parses pass/fail counts                ║
// ╚══════════════════════════════════════════════════════════╝

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenExecutor {

    private static final Logger log =
            LogManager.getLogger(MavenExecutor.class);

    // ┌─────────────────────────────────────────────────────┐
    // │  RESULT — holds execution outcome                   │
    // └─────────────────────────────────────────────────────┘
    public static class ExecutionResult {
        public final boolean success;
        public final int testsRun;
        public final int failures;
        public final String rawOutput;

        public ExecutionResult(boolean success,
                               int testsRun, int failures,
                               String rawOutput) {
            this.success = success;
            this.testsRun = testsRun;
            this.failures = failures;
            this.rawOutput = rawOutput;
        }

        @Override
        public String toString() {
            return String.format(
                    "%d tests run, %d failed, %d passed",
                    testsRun, failures, testsRun - failures);
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  RUN — executes mvn test with given profile         │
    // └─────────────────────────────────────────────────────┘

    // platform: android | ios | cucumber
    // groups: smoke | regression | null for all
    public ExecutionResult run(String platform,
                               String groups) throws Exception {

        // Build the mvn command
        StringBuilder cmd = new StringBuilder();
        cmd.append("mvn clean test -P").append(platform);

        if (groups != null && !groups.isEmpty()) {
            cmd.append(" -Dgroups=").append(groups);
        }

        log.info("Executing: {}", cmd);

        // Build full shell command with JAVA_HOME set
        String fullCommand = "export JAVA_HOME="
                + JarvisConfig.JAVA_HOME + " && " + cmd;

        ProcessBuilder pb = new ProcessBuilder(
                "/bin/bash", "-c", fullCommand);
        pb.directory(new File(JarvisConfig.PROJECT_ROOT));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Capture output line by line
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            System.out.println(line); // live console output
        }

        // Wait for completion with timeout
        boolean finished = process.waitFor(
                JarvisConfig.MAVEN_TIMEOUT_SECONDS,
                TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException(
                    "Maven command timed out after "
                            + JarvisConfig.MAVEN_TIMEOUT_SECONDS
                            + " seconds");
        }

        int exitCode = process.exitValue();
        String out = output.toString();

        // Parse "Tests run: X, Failures: Y" from output
        int testsRun = parseCount(out,
                "Tests run:\\s*(\\d+)");
        int failures = parseCount(out,
                "Failures:\\s*(\\d+)");

        boolean success = exitCode == 0;

        log.info("Execution complete: exitCode={}, "
                        + "testsRun={}, failures={}",
                exitCode, testsRun, failures);

        return new ExecutionResult(
                success, testsRun, failures, out);
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  COMPILE — quick compile check for generated code   │
    // └─────────────────────────────────────────────────────┘

    public boolean compile() throws Exception {
        String fullCommand = "export JAVA_HOME="
                + JarvisConfig.JAVA_HOME
                + " && mvn compile test-compile -q";

        ProcessBuilder pb = new ProcessBuilder(
                "/bin/bash", "-c", fullCommand);
        pb.directory(new File(JarvisConfig.PROJECT_ROOT));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        boolean finished = process.waitFor(
                60, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return false;
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Compile failed:\n{}", output);
        }
        return exitCode == 0;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  HELPER — regex extraction                          │
    // └─────────────────────────────────────────────────────┘

    private int parseCount(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}