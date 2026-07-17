package com.framework.ai;

// ╔══════════════════════════════════════════════════════════╗
// ║  FAILURE ANALYZER                                        ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Takes a test failure (exception + context) and asks    ║
// ║  Claude to explain the likely root cause in plain        ║
// ║  English. Result is attached to the Allure report.       ║
// ║                                                          ║
// ║  Called from BaseTest.afterMethod() ONLY on failure —    ║
// ║  never on pass/skip. Adds latency + cost, so it must     ║
// ║  never run on the happy path.                            ║
// ╚══════════════════════════════════════════════════════════╝

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FailureAnalyzer {

    private static final Logger log =
            LogManager.getLogger(FailureAnalyzer.class);

    private final ClaudeAIService claude;

    public FailureAnalyzer() {
        this.claude = new ClaudeAIService();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  ANALYZE — main entry point                          │
    // │  testName: e.g. "MDA-1234"                            │
    // │  errorMessage: exception message from ITestResult    │
    // │  pageSource: optional — live XML if available         │
    // └─────────────────────────────────────────────────────┘

    public String analyze(String testName,
                          String errorMessage, String pageSource) {

        try {
            String prompt = buildPrompt(
                    testName, errorMessage, pageSource);

            String analysis = claude.ask(prompt, 512);

            log.info("Failure analysis generated for {}: {}",
                    testName, analysis);
            return analysis;

        } catch (Exception e) {
            // Analysis is a bonus feature, not critical path.
            // If Claude call fails, log it but do NOT fail
            // the test run itself — cleanup must always proceed.
            log.warn("Failure analysis unavailable: {}",
                    e.getMessage());
            return "AI analysis unavailable: "
                    + e.getMessage();
        }
    }

    // Overload — no page source available
    public String analyze(String testName,
                          String errorMessage) {
        return analyze(testName, errorMessage, null);
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  PROMPT BUILDER                                      │
    // └─────────────────────────────────────────────────────┘

    private String buildPrompt(String testName,
                               String errorMessage, String pageSource) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("A mobile automation test failed. ");
        prompt.append("Explain the likely root cause in ");
        prompt.append("2-3 plain English sentences a QA ");
        prompt.append("engineer can act on immediately.\n\n");
        prompt.append("Test: ").append(testName).append("\n");
        prompt.append("Error: ").append(errorMessage)
                .append("\n");

        if (pageSource != null && !pageSource.isEmpty()) {
            String truncated = pageSource.length() > 4000
                    ? pageSource.substring(0, 4000)
                    : pageSource;
            prompt.append("\nPage source at time of failure:\n")
                    .append(truncated);
        }

        prompt.append("\n\nDo not restate the error message. ");
        prompt.append("Give a likely cause and one concrete ");
        prompt.append("suggestion — e.g. 'increase wait "
                + "timeout' or 'locator likely changed' or ");
        prompt.append("'app still on splash screen'.");

        return prompt.toString();
    }
}