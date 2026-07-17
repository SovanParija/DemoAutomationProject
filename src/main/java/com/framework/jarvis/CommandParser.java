package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  COMMAND PARSER                                          ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Uses Claude to understand natural language commands    ║
// ║  Converts "run smoke tests" into structured JarvisCommand║
// ╚══════════════════════════════════════════════════════════╝

import com.fasterxml.jackson.databind.JsonNode;
import com.framework.ai.ClaudeAIService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandParser {

    private static final Logger log =
            LogManager.getLogger(CommandParser.class);

    private final ClaudeAIService claude;

    public CommandParser() {
        this.claude = new ClaudeAIService();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  JARVIS COMMAND — structured action                 │
    // └─────────────────────────────────────────────────────┘
    public static class JarvisCommand {
        public String action;      // run | generate | analyze | report
        public String target;      // tests | page_object | locator
        public String platform;    // android | ios | cucumber
        public String groups;      // smoke | regression | null
        public String scenario;    // description for generation
        public String className;   // suggested class name

        @Override
        public String toString() {
            return String.format(
                    "action=%s target=%s platform=%s groups=%s",
                    action, target, platform, groups);
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  PARSE — main entry point                            │
    // └─────────────────────────────────────────────────────┘

    public JarvisCommand parse(String userInput) {

        // For simple, common commands — skip Claude call
        // to save API cost. Only use AI for ambiguous input.
        JarvisCommand quickMatch = tryQuickMatch(userInput);
        if (quickMatch != null) {
            log.info("Quick matched: {}", quickMatch);
            return quickMatch;
        }

        // Fall back to Claude for complex/ambiguous commands
        String prompt = buildPrompt(userInput);
        JsonNode response = claude.askForJson(prompt);

        JarvisCommand cmd = new JarvisCommand();
        cmd.action = getOrNull(response, "action");
        cmd.target = getOrNull(response, "target");
        cmd.platform = getOrNull(response, "platform");
        cmd.groups = getOrNull(response, "groups");
        cmd.scenario = getOrNull(response, "scenario");
        cmd.className = getOrNull(response, "className");

        log.info("Claude parsed: {}", cmd);
        return cmd;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  QUICK MATCH — pattern matching for common commands  │
    // │  Saves API calls for obvious commands                │
    // └─────────────────────────────────────────────────────┘

    private JarvisCommand tryQuickMatch(String input) {
        String lower = input.toLowerCase().trim();
        JarvisCommand cmd = new JarvisCommand();

        // "run smoke tests [on android/ios]"
        if (lower.matches(".*run.*smoke.*")) {
            cmd.action = "run";
            cmd.target = "tests";
            cmd.groups = "smoke";
            cmd.platform = lower.contains("ios")
                    ? "ios" : "android";
            return cmd;
        }

        // "run all tests [on android/ios]"
        if (lower.matches(".*run.*(all|regression).*test.*")) {
            cmd.action = "run";
            cmd.target = "tests";
            cmd.groups = null;
            cmd.platform = lower.contains("ios")
                    ? "ios" : "android";
            return cmd;
        }

        // "run bdd" or "run cucumber"
        if (lower.matches(".*run.*(bdd|cucumber).*")) {
            cmd.action = "run";
            cmd.target = "tests";
            cmd.platform = "cucumber";
            return cmd;
        }

        // "what failed" / "show failures"
        if (lower.matches(".*(what|show).*fail.*")) {
            cmd.action = "analyze";
            cmd.target = "failures";
            return cmd;
        }

        // "open allure" / "show report"
        if (lower.matches(".*(open|show).*(allure|report).*")) {
            cmd.action = "report";
            cmd.target = "allure";
            return cmd;
        }

        // "how many passed" / "test summary"
        if (lower.matches(".*(how many|summary|status).*")) {
            cmd.action = "analyze";
            cmd.target = "summary";
            return cmd;
        }

        // No quick match — let Claude figure it out
        return null;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  BUILD PROMPT — for complex commands                 │
    // └─────────────────────────────────────────────────────┘

    private String buildPrompt(String userInput) {
        return """
            You are Jarvis, a test automation assistant
            for an Appium mobile testing framework.

            Parse this command: "%s"

            Return this exact JSON structure:
            {
              "action": "run|generate|analyze|report|fix",
              "target": "tests|page_object|locator|failures|summary|allure",
              "platform": "android|ios|cucumber|null",
              "groups": "smoke|regression|null",
              "scenario": "description if generating something, else null",
              "className": "suggested Java class name if generating, else null"
            }

            Rules:
            - action=run: user wants to execute existing tests
            - action=generate: user wants new code written
            - action=analyze: user wants info about past results
            - action=report: user wants to view Allure report
            - action=fix: user wants a broken locator repaired
            """.formatted(userInput);
    }

    private String getOrNull(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            String val = node.get(field).asText();
            return "null".equals(val) ? null : val;
        }
        return null;
    }
}