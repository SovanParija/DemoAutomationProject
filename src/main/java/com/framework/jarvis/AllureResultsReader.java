package com.framework.jarvis;

// ╔══════════════════════════════════════════════════════════╗
// ║  ALLURE RESULTS READER                                   ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Reads target/allure-results JSON files                 ║
// ║  Extracts pass/fail info for FailureAnalyzer            ║
// ╚══════════════════════════════════════════════════════════╝

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllureResultsReader {

    private static final Logger log =
            LogManager.getLogger(AllureResultsReader.class);

    private final ObjectMapper mapper = new ObjectMapper();

    // ┌─────────────────────────────────────────────────────┐
    // │  TEST RESULT SUMMARY                                 │
    // └─────────────────────────────────────────────────────┘
    public static class TestSummary {
        public String name;
        public String status; // passed | failed | skipped
        public String errorMessage;
        public long duration;

        @Override
        public String toString() {
            return name + " [" + status + "]"
                    + (errorMessage != null
                    ? " - " + errorMessage : "");
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  READ ALL — parses every result JSON in folder      │
    // └─────────────────────────────────────────────────────┘

    public List<TestSummary> readAllResults() {
        List<TestSummary> results = new ArrayList<>();

        File resultsDir = new File(
                JarvisConfig.PROJECT_ROOT + "/"
                        + JarvisConfig.ALLURE_RESULTS_PATH);

        if (!resultsDir.exists()) {
            log.warn("Allure results directory not found: {}",
                    resultsDir.getPath());
            return results;
        }

        File[] resultFiles = resultsDir.listFiles(
                (dir, name) -> name.endsWith("-result.json"));

        if (resultFiles == null) return results;

        for (File file : resultFiles) {
            try {
                JsonNode node = mapper.readTree(file);
                TestSummary summary = new TestSummary();
                summary.name = node.has("name")
                        ? node.get("name").asText() : "Unknown";
                summary.status = node.has("status")
                        ? node.get("status").asText() : "unknown";
                summary.duration =
                        (node.has("stop") && node.has("start"))
                                ? node.get("stop").asLong()
                                - node.get("start").asLong() : 0;

                // Extract error message if failed
                if (node.has("statusDetails")
                        && node.get("statusDetails")
                        .has("message")) {
                    summary.errorMessage = node
                            .get("statusDetails")
                            .get("message").asText();
                }

                results.add(summary);
            } catch (Exception e) {
                log.warn("Could not parse result file: {}",
                        file.getName());
            }
        }

        return results;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GET FAILURES ONLY                                   │
    // └─────────────────────────────────────────────────────┘

    public List<TestSummary> getFailures() {
        return readAllResults().stream()
                .filter(r -> "failed".equals(r.status)
                        || "broken".equals(r.status))
                .collect(java.util.stream.Collectors.toList());
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GET SUMMARY TEXT                                    │
    // └─────────────────────────────────────────────────────┘

    public String getSummaryText() {
        List<TestSummary> all = readAllResults();
        long passed = all.stream()
                .filter(r -> "passed".equals(r.status))
                .count();
        long failed = all.stream()
                .filter(r -> "failed".equals(r.status)
                        || "broken".equals(r.status))
                .count();
        long skipped = all.stream()
                .filter(r -> "skipped".equals(r.status))
                .count();

        return String.format(
                "%d tests total: %d passed, %d failed, %d skipped",
                all.size(), passed, failed, skipped);
    }
}