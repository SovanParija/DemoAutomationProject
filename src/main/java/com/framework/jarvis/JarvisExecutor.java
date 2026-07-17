package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  JARVIS EXECUTOR                                         ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Routes parsed commands to the correct handler           ║
// ║  This is the brain that decides what to actually do      ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.ai.PageObjectGenerator;
import com.framework.ai.TestGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class JarvisExecutor {

    private static final Logger log =
            LogManager.getLogger(JarvisExecutor.class);

    private final CommandParser parser;
    private final MavenExecutor mavenExecutor;
    private final AllureResultsReader resultsReader;
    private final TestGenerator testGenerator;
    private final PageObjectGenerator pageGenerator;
    private final JarvisFileWriter fileWriter;
    private final JarvisCompiler compiler;

    public JarvisExecutor() {
        this.parser = new CommandParser();
        this.mavenExecutor = new MavenExecutor();
        this.resultsReader = new AllureResultsReader();
        this.testGenerator = new TestGenerator();
        this.pageGenerator = new PageObjectGenerator();
        this.fileWriter = new JarvisFileWriter();
        this.compiler = new JarvisCompiler();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  EXECUTE — main entry point                          │
    // │  Takes raw text, returns spoken-friendly result      │
    // └─────────────────────────────────────────────────────┘

    public String execute(String userInput) {
        try {
            // Step 1 — understand what user wants
            CommandParser.JarvisCommand cmd =
                    parser.parse(userInput);

            if (cmd.action == null) {
                return "I did not understand that command. "
                        + "Try: 'run smoke tests' or "
                        + "'what failed last run'";
            }

            // Step 2 — route to correct handler
            return switch (cmd.action) {
                case "run"     -> handleRun(cmd);
                case "analyze" -> handleAnalyze(cmd);
                case "report"  -> handleReport(cmd);
                case "generate" -> handleGenerate(cmd);
                case "fix"     -> handleFix(cmd);
                default -> "Unknown action: " + cmd.action;
            };

        } catch (Exception e) {
            log.error("Execution failed: {}", e.getMessage());
            return "Something went wrong: " + e.getMessage();
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  HANDLE RUN — executes test suite                    │
    // └─────────────────────────────────────────────────────┘

    private String handleRun(CommandParser.JarvisCommand cmd) {
        try {
            String platform = cmd.platform != null
                    ? cmd.platform : "android";

            log.info("Running tests: platform={} groups={}",
                    platform, cmd.groups);

            MavenExecutor.ExecutionResult result =
                    mavenExecutor.run(platform, cmd.groups);

            if (result.success) {
                return String.format(
                        "Tests complete on %s. %s. All passed.",
                        platform, result);
            } else {
                return String.format(
                        "Tests complete on %s. %s. "
                                + "%d test(s) failed — check Allure report.",
                        platform, result, result.failures);
            }

        } catch (Exception e) {
            return "Test execution failed: " + e.getMessage();
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  HANDLE ANALYZE — reads and summarizes results       │
    // └─────────────────────────────────────────────────────┘

    private String handleAnalyze(
            CommandParser.JarvisCommand cmd) {

        if ("failures".equals(cmd.target)) {
            List<AllureResultsReader.TestSummary> failures =
                    resultsReader.getFailures();

            if (failures.isEmpty()) {
                return "No failures found in the last run. "
                        + "All tests passed.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(failures.size())
                    .append(" test(s) failed: ");
            for (AllureResultsReader.TestSummary f : failures) {
                sb.append(f.name);
                if (f.errorMessage != null) {
                    sb.append(" because ")
                            .append(f.errorMessage);
                }
                sb.append(". ");
            }
            return sb.toString();
        }

        // Default — summary
        return resultsReader.getSummaryText();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  HANDLE REPORT — opens Allure report                 │
    // └─────────────────────────────────────────────────────┘

    private String handleReport(
            CommandParser.JarvisCommand cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/bin/bash", "-c",
                    "allure serve target/allure-results &");
            pb.directory(new java.io.File(
                    JarvisConfig.PROJECT_ROOT));
            pb.start();
            return "Opening Allure report in your browser.";
        } catch (Exception e) {
            return "Could not open report: " + e.getMessage();
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  HANDLE GENERATE — placeholder for TestGenerator     │
    // └─────────────────────────────────────────────────────┘

    // ┌─────────────────────────────────────────────────────┐
// │  HANDLE GENERATE — write, save, compile, report      │
// └─────────────────────────────────────────────────────┘

    private String handleGenerate(
            CommandParser.JarvisCommand cmd) {

        if (cmd.scenario == null || cmd.scenario.isEmpty()) {
            return "I need a description of what to generate. "
                    + "Try: 'write a test for logout'";
        }

        try {
            // "page" or "page_object" target → PageObjectGenerator
            // anything else → TestGenerator
            if ("page_object".equals(cmd.target)
                    || "page".equals(cmd.target)) {
                return generatePage(cmd);
            } else {
                return generateTest(cmd);
            }

        } catch (Exception e) {
            log.error("Generation failed: {}", e.getMessage());
            return "Code generation failed: " + e.getMessage();
        }
    }

    private String generateTest(CommandParser.JarvisCommand cmd)
            throws Exception {

        String className = cmd.className != null
                ? cmd.className
                : "GeneratedTest_" + System.currentTimeMillis();

        log.info("Generating test class: {} for scenario: {}",
                className, cmd.scenario);

        // Step 1 — Claude writes the code
        String code = testGenerator.generate(
                cmd.scenario, className);

        // Step 2 — save it to the real file location
        String path = fileWriter.saveTestClass(className, code);

        // Step 3 — verify it actually compiles before claiming success
        boolean compiles = compiler.verify();

        if (!compiles) {
            return String.format(
                    "Generated %s but it does NOT compile. "
                            + "Saved to %s for manual review — check the "
                            + "console output above for the compile error.",
                    className, path);
        }

        return String.format(
                "Generated and verified %s. Saved to %s. "
                        + "Compiles cleanly — ready to run.",
                className, path);
    }

    private String generatePage(CommandParser.JarvisCommand cmd)
            throws Exception {

        String className = cmd.className != null
                ? cmd.className
                : "GeneratedPage_" + System.currentTimeMillis();

        // NOTE: page generation needs the live Inspector XML,
        // which this text-only flow does not currently capture.
        // For now this requires the XML to be supplied separately
        // — full wiring to a live driver session is a Day 6+ item.
        return "Page object generation requires live page source "
                + "XML from an active Appium session — this flow is "
                + "not yet connected to a running driver. "
                + "Use PageObjectGenerator.generate() directly with "
                + "captured XML for now.";
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  HANDLE FIX — placeholder for SelfHealingDriver      │
    // └─────────────────────────────────────────────────────┘

    // ┌─────────────────────────────────────────────────────┐
// │  HANDLE FIX — self-healing requires an active driver │
// └─────────────────────────────────────────────────────┘

    private String handleFix(
            CommandParser.JarvisCommand cmd) {
        // Same constraint as page generation — SelfHealingDriver
        // needs a live AppiumDriver session with a real broken
        // locator to heal. This text-only Jarvis flow has neither.
        // Real usage: SelfHealingDriver is called FROM inside
        // BasePage.findWithHealing() during an actual test run,
        // not triggered standalone from a chat command.
        return "Self-healing runs automatically during test "
                + "execution when a locator fails — it is not "
                + "triggered standalone. Run a test that uses "
                + "findWithHealing() to see it in action.";
    }
}