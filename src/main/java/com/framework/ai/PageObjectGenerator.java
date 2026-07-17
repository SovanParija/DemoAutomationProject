package com.framework.ai;

// ╔══════════════════════════════════════════════════════════╗
// ║  PAGE OBJECT GENERATOR                                   ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Given raw Appium Inspector page source XML, asks        ║
// ║  Claude to generate a complete page object class         ║
// ║  following LoginPage's exact structural pattern —        ║
// ║  including both @AndroidFindBy and @iOSXCUITFindBy       ║
// ║  where the XML makes both platforms identifiable.        ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.jarvis.JarvisConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PageObjectGenerator {

    private static final Logger log =
            LogManager.getLogger(PageObjectGenerator.class);

    // Reference file — LoginPage already has both
    // @AndroidFindBy AND @iOSXCUITFindBy on shared fields,
    // which is the exact cross-platform pattern to replicate
    private static final String REFERENCE_PAGE_PATH =
            "src/main/java/com/framework/pages/common/"
                    + "LoginPage.java";

    private final ClaudeAIService claude;

    public PageObjectGenerator() {
        this.claude = new ClaudeAIService();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GENERATE — main entry point                        │
    // │  pageSourceXml: raw output from Appium Inspector     │
    // │  className: e.g. "CartPage"                          │
    // └─────────────────────────────────────────────────────┘

    public String generate(String pageSourceXml,
                           String className) {

        try {
            String referenceCode = readReferenceFile();

            // Truncate — page source can be huge, most
            // useful elements are near the top of the tree
            String truncated = pageSourceXml.length() > 10000
                    ? pageSourceXml.substring(0, 10000)
                    : pageSourceXml;

            String prompt = buildPrompt(
                    truncated, className, referenceCode);

            String code = claude.askForCode(prompt);

            log.info("Generated page object: {} ({} chars)",
                    className, code.length());
            return code;

        } catch (Exception e) {
            log.error("Page object generation failed: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Could not generate page object: "
                            + className, e);
        }
    }

    private String readReferenceFile() throws IOException {
        Path path = Path.of(
                JarvisConfig.PROJECT_ROOT + "/"
                        + REFERENCE_PAGE_PATH);

        if (!Files.exists(path)) {
            throw new IOException(
                    "Reference page not found: " + path);
        }

        return Files.readString(path);
    }

    private String buildPrompt(String pageSourceXml,
                               String className, String referenceCode) {
        return """
            Generate a complete Java Appium page object class
            named exactly: %s

            Given this Appium Inspector page source XML:
            %s

            Follow this EXACT structural pattern from an
            existing page object in the same framework:

            %s

            Rules:
            - Extend BasePage exactly like the reference
            - Use @AndroidFindBy for Android elements
              (resource-id preferred, accessibility second,
              xpath only as last resort)
            - Only add @iOSXCUITFindBy if the XML clearly
              shows iOS element attributes — do not guess
            - Implement isPageLoaded() using the same
              try-catch waitForVisible() pattern as reference
            - Only include elements that are clearly
              interactive (buttons, fields, clickable text) —
              skip purely decorative elements
            - Return ONLY the Java source code, no markdown,
              no explanation before or after
            """.formatted(className, pageSourceXml,
                referenceCode);
    }
}