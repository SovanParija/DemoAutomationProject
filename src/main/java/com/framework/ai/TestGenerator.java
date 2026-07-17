package com.framework.ai;

// ╔══════════════════════════════════════════════════════════╗
// ║  TEST GENERATOR                                          ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Given a scenario description, asks Claude to write a   ║
// ║  complete TestNG test class following the EXACT pattern ║
// ║  of an existing reference test (MDA_1234).               ║
// ║                                                          ║
// ║  Does NOT save or compile the file — that is             ║
// ║  JarvisFileWriter and JarvisCompiler's job.               ║
// ║  This class has ONE job: produce correct Java source.    ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.jarvis.JarvisConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestGenerator {

    private static final Logger log =
            LogManager.getLogger(TestGenerator.class);

    // Reference file — every generated test mimics this
    // structure exactly, so output style never drifts
    private static final String REFERENCE_TEST_PATH =
            "src/test/java/com/framework/tests/android/"
                    + "MDA_1234_ValidLoginTest.java";

    private final ClaudeAIService claude;

    public TestGenerator() {
        this.claude = new ClaudeAIService();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GENERATE — main entry point                        │
    // │  scenario: e.g. "logout returns to login screen"    │
    // │  suggestedClassName: e.g. "MDA_1239_LogoutTest"      │
    // └─────────────────────────────────────────────────────┘

    public String generate(String scenario,
                           String suggestedClassName) {

        try {
            String referenceCode = readReferenceFile();
            String prompt = buildPrompt(
                    scenario, suggestedClassName, referenceCode);

            String code = claude.askForCode(prompt);

            log.info("Generated test class: {} ({} chars)",
                    suggestedClassName, code.length());
            return code;

        } catch (Exception e) {
            log.error("Test generation failed: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Could not generate test for: " + scenario, e);
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  READ REFERENCE — the pattern Claude must copy       │
    // └─────────────────────────────────────────────────────┘

    private String readReferenceFile() throws IOException {
        Path path = Path.of(
                JarvisConfig.PROJECT_ROOT + "/"
                        + REFERENCE_TEST_PATH);

        if (!Files.exists(path)) {
            throw new IOException(
                    "Reference test not found: " + path
                            + " — cannot generate without a pattern "
                            + "to follow");
        }

        return Files.readString(path);
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  PROMPT — forces structural consistency               │
    // └─────────────────────────────────────────────────────┘

    private String buildPrompt(String scenario,
                               String className, String referenceCode) {
        return """
            Generate a complete Java TestNG test class for
            this scenario: %s

            The class name must be exactly: %s

            Follow this EXACT structural pattern — same
            imports, same annotations, same style, same
            use of TestDataManager, same use of step():

            %s

            Rules:
            - Extend BaseTest exactly like the reference
            - Use @Epic @Feature @Story matching the reference style
            - Use TestDataManager.getLoginData() for any test data
              needed — do not hardcode credentials
            - Use step() for each logical action, matching
              the reference test's granularity
            - Assign the next logical MDA ticket number if
              the scenario needs one
            - Return ONLY the Java source code, no markdown,
              no explanation before or after
            """.formatted(scenario, className, referenceCode);
    }
}