package com.framework.base;

// ConfigManager — we use it to log which platform is running

import com.framework.config.ConfigManager;

// DriverManager — we call initDriver() and quitDriver() here
import com.framework.drivers.DriverManager;

// Allure — for attaching screenshots to the report
import io.qameta.allure.Allure;

// Logger — prints messages to console during test run
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// ITestResult — tells us if test PASSED, FAILED or SKIPPED
// TestNG passes this automatically into @AfterMethod
import org.testng.ITestResult;

// All TestNG lifecycle annotations
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

// ByteArrayInputStream — wraps screenshot bytes
// so Allure can attach it as an image
import java.io.ByteArrayInputStream;

// Method — gives us the name of the test method
// TestNG passes this automatically into @BeforeMethod
import java.lang.reflect.Method;

// abstract = this class cannot be used directly
// Every test class extends this:
// public class LoginTest extends BaseTest
public abstract class BaseTest {

    // ── FIELDS ────────────────────────────────────────────────

    // static = one logger shared across all tests
    // BaseTest.class = all log messages show "BaseTest" as source
    protected static final Logger log =
            LogManager.getLogger(BaseTest.class);

    // ConfigManager singleton — already loaded config.yaml
    // We use it to log which platform tests are running on
    protected static final ConfigManager config =
            ConfigManager.getInstance();

    // ── SUITE LEVEL ───────────────────────────────────────────
    // Runs ONCE at the very start before any test class starts
    // Use for global setup — logging, environment info etc

    @BeforeSuite(alwaysRun = true)
    // alwaysRun = true means this runs even if other
    // setup methods fail — guarantees suite always starts clean
    public void beforeSuite() {
        log.info("╔══════════════════════════════╗");
        log.info("║   TEST SUITE STARTING        ║");
        log.info("╚══════════════════════════════╝");

        // Log which platform we are testing on
        // Comes from config.yaml or -Dplatform=android
        log.info("Platform    : {}", config.getPlatform());

        // Log which environment — qa, staging, prod
        // Override with: mvn test -Denv=staging
        log.info("Environment : {}",
                System.getProperty("env", "qa"));
    }

    // Runs ONCE at the very end after all tests complete
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        log.info("TEST SUITE COMPLETE");
        try {
            java.io.File src = new java.io.File(
                    "src/test/resources/environment.properties");
            java.io.File dst = new java.io.File(
                    "target/allure-results/environment.properties");
            dst.getParentFile().mkdirs();
            org.apache.commons.io.FileUtils
                    .copyFile(src, dst);
            log.info("Environment properties copied");
        } catch (Exception e) {
            log.warn("Could not copy env properties: {}",
                    e.getMessage());
        }
    }

    // ── CLASS LEVEL ───────────────────────────────────────────
    // Runs once per test class — before first test in the class
    // Use for class-level setup if needed

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        // getClass().getSimpleName() returns "LoginTest"
        // not the full path — clean and readable
        log.info("── Setting up class: {}",
                getClass().getSimpleName());
    }

    // Runs once per test class — after last test in the class
    @AfterClass(alwaysRun = true)
    public void afterClass() {
        log.info("── Tearing down class: {}",
                getClass().getSimpleName());
    }

    // ── METHOD LEVEL ──────────────────────────────────────────
    // THIS IS THE MOST IMPORTANT PART
    // Runs before EVERY single @Test method

    @BeforeMethod(alwaysRun = true)
    // Method parameter — TestNG automatically passes in
    // the test method that is about to run
    // We use method.getName() to log which test is starting
    // THIS IS THE KEY LINE
    // Creates a fresh AppiumDriver before every test
    // Launches the app on your emulator or device
    // Platform decided by config.yaml or -Dplatform flag

    // Log which test is about to start
    // e.g. "┌─ Starting: testValidLogin"
    public void beforeMethod(Method method) {
        log.info("Starting: {}", method.getName());
        DriverManager.initDriver();
        // Give app 3 seconds to fully launch
        pause(3000);
    }


    // Runs after EVERY single @Test method
    // alwaysRun = true is CRITICAL here
    // Without it — if @BeforeMethod fails, this is skipped
    // Driver never gets quit — emulator runs out of memory
    @AfterMethod(alwaysRun = true)
    // ITestResult — TestNG passes this in automatically
    // Contains test name, pass/fail status, exception info
    public void afterMethod(ITestResult result) {

        // Get the name of the test that just ran
        // e.g. "testValidLogin" or "testInvalidPassword"
        String testName = result.getMethod().getMethodName();

        // Check what happened to the test
        // ITestResult.FAILURE = 2
        // ITestResult.SKIP    = 3
        // ITestResult.SUCCESS = 1
        switch (result.getStatus()) {

            case ITestResult.FAILURE -> {
                // Log failure with exact reason
                log.error("✗ FAILED : {}", testName);
                log.error("  Reason : {}",
                        result.getThrowable().getMessage());

                // Take screenshot immediately while app is still open
                // Attach it to Allure report for visual debugging
                attachScreenshot(testName);
            }

            case ITestResult.SKIP -> {
                // Test was skipped — log as warning
                log.warn("⊘ SKIPPED: {}", testName);
            }

            case ITestResult.SUCCESS -> {
                // Test passed — log as info
                log.info("✓ PASSED : {}", testName);
            }
        }

        // ALWAYS quit driver after every test — pass or fail
        // Closes the app and removes driver from ThreadLocal
        // Next test gets a completely fresh driver
        DriverManager.quitDriver();

        log.info("└─ Finished: {}", testName);
    }

    // ── SCREENSHOT ────────────────────────────────────────────
    // Called automatically when a test fails
    // Takes screenshot and attaches to Allure report
    // private = only BaseTest uses this — test classes don't call it

    private void attachScreenshot(String testName) {
        try {
            // Cast driver to TakesScreenshot interface
            // AppiumDriver already implements TakesScreenshot
            // OutputType.BYTES = screenshot as raw byte array
            // Bytes are better than File — no disk write needed
            byte[] screenshot =
                    ((org.openqa.selenium.TakesScreenshot)
                            DriverManager.getDriver())
                            .getScreenshotAs(
                                    org.openqa.selenium.OutputType.BYTES);

            // Allure.addAttachment() puts the image inside
            // the Allure report for this specific test
            // You see it when you run: allure serve target/allure-results
            Allure.addAttachment(
                    testName + "_FAILED",  // name shown in report
                    "image/png",           // MIME type
                    new ByteArrayInputStream(screenshot), // image data
                    ".png");               // file extension

            log.info("Screenshot attached to Allure report");

        } catch (Exception e) {
            // Never crash the cleanup because screenshot failed
            // Log warning and continue — driver still needs to quit
            log.warn("Could not take screenshot: {}",
                    e.getMessage());
        }
    }

    // ── HELPERS ───────────────────────────────────────────────
    // Available to all test classes that extend BaseTest

    // Log a step — appears in both console AND Allure report
    // Use in your tests to mark each action clearly:
    // step("Enter valid username");
    // step("Tap login button");
    // step("Verify home screen loaded");
    protected void step(String message) {
        log.info("  → {}", message);
        Allure.step(message); // adds step to Allure timeline
    }

    // Hard pause — stops everything for exact milliseconds
    // Use sparingly — only when no wait condition is possible
    // e.g. pause(2000) waits exactly 2 seconds
    protected void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Restore interrupted state — Java best practice
            Thread.currentThread().interrupt();
        }
    }
}