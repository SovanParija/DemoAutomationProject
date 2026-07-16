package com.framework.runners;

// ╔══════════════════════════════════════════════════════════╗
// ║  CUCUMBER HOOKS                                         ║
// ╠══════════════════════════════════════════════════════════╣
// ║  @Before — runs before every Cucumber scenario          ║
// ║  @After  — runs after every Cucumber scenario           ║
// ║                                                         ║
// ║  Same role as BaseTest @BeforeMethod/@AfterMethod       ║
// ║  but for Cucumber instead of TestNG                     ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.drivers.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;

public class Hooks {

    private static final Logger log =
            LogManager.getLogger(Hooks.class);

    // ┌─────────────────────────────────────────────────────┐
    // │  @Before — runs before every scenario               │
    // └─────────────────────────────────────────────────────┘

    // order=1 means this runs first if multiple @Before
    @Before(order = 1)
    public void setUp(Scenario scenario) {
        log.info("Starting scenario: {}",
                scenario.getName());

        // Create fresh AppiumDriver
        // Same as BaseTest @BeforeMethod
        DriverManager.initDriver();

        // Wait for app to fully launch
        try { Thread.sleep(5000); }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Driver ready for: {}",
                scenario.getName());
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  @After — runs after every scenario                 │
    // └─────────────────────────────────────────────────────┘

    // alwaysRun equivalent in Cucumber — @After always runs
    @After(order = 1)
    public void tearDown(Scenario scenario) {
        log.info("Finishing scenario: {} — {}",
                scenario.getName(),
                scenario.getStatus());

        // Take screenshot if scenario FAILED
        // Attach to Allure report
        if (scenario.isFailed()) {
            log.error("Scenario FAILED: {}",
                    scenario.getName());
            attachScreenshot(scenario);
        }

        // ALWAYS quit driver — same as alwaysRun=true
        DriverManager.quitDriver();
        log.info("Driver closed");
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  @AfterStep — runs after every step                 │
    // └─────────────────────────────────────────────────────┘

    // Take screenshot after every FAILED step
    // Helps debug which exact step failed
    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            attachScreenshot(scenario);
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  SCREENSHOT HELPER                                  │
    // └─────────────────────────────────────────────────────┘

    private void attachScreenshot(Scenario scenario) {
        try {
            byte[] screenshot =
                    ((org.openqa.selenium.TakesScreenshot)
                            DriverManager.getDriver())
                            .getScreenshotAs(
                                    org.openqa.selenium.OutputType.BYTES);

            // Attach to Cucumber report
            scenario.attach(
                    screenshot,
                    "image/png",
                    scenario.getName() + "_failed");

            // Also attach to Allure report
            Allure.addAttachment(
                    scenario.getName() + "_FAILED",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png");

            log.info("Screenshot attached");
        } catch (Exception e) {
            log.warn("Screenshot failed: {}",
                    e.getMessage());
        }
    }
}