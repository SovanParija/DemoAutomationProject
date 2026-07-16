package com.framework.base;
// AppiumDriver is the main class that controls
// your Android or iOS device

import com.framework.config.ConfigManager;
import com.framework.drivers.DriverManager;
import io.appium.java_client.AppiumDriver;

// AppiumFieldDecorator is what makes @AndroidFindBy
// and @iOSXCUITFindBy annotations work
// Without this — all your page elements are NULL
import io.appium.java_client.pagefactory.AppiumFieldDecorator;

// Logger prints messages to console during test run
// Helps you debug when something goes wrong
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// By — used to find elements dynamically
// e.g. By.id("login_button") or By.xpath("//button")
import org.openqa.selenium.By;

// JavascriptExecutor — lets us run JavaScript commands
// We use it for scrolling on mobile
import org.openqa.selenium.JavascriptExecutor;

// WebElement — represents one element on the screen
// e.g. a button, text field, label
import org.openqa.selenium.WebElement;

// PageFactory — scans the page class for @FindBy annotations
// and connects them to real screen elements
import org.openqa.selenium.support.PageFactory;

// ExpectedConditions — defines what we wait FOR
// e.g. wait until element is visible, clickable, gone
import org.openqa.selenium.support.ui.ExpectedConditions;

// WebDriverWait — the actual wait mechanism
// Keeps checking every 500ms until condition is met
// or throws TimeoutException after the timeout
import org.openqa.selenium.support.ui.WebDriverWait;

// Duration — used to express time in seconds
// Duration.ofSeconds(20) = wait up to 20 seconds
import java.time.Duration;

// List — used when findElements returns multiple elements
// e.g. a list of products on a search results page
import java.util.HashMap;
import java.util.List;

// Map — used for scroll command parameters
// Map.of("direction", "down")
import java.util.Map;

// abstract = this class cannot be created directly
// You MUST extend it: public class LoginPage extends BasePage
// This forces every page to implement isPageLoaded()
public abstract class BasePage {

    // ── FIELDS ────────────────────────────────────────────────

    // getClass() returns the actual page class name at runtime
    // So when LoginPage logs something it shows "LoginPage"
    // not "BasePage" — each page gets its own correctly named logger
    protected final Logger log = LogManager.getLogger(getClass());

    // The driver that controls your device
    // protected = LoginPage and all subclasses can use it
    // final = once set in constructor it never changes
    protected final AppiumDriver driver;

    // ConfigManager gives us platform info and timeouts
    // isAndroid(), getExplicitTimeout() etc
    protected final ConfigManager config;

    // WebDriverWait = our explicit wait object
    // We create it once here and reuse it in every wait method
    protected final WebDriverWait wait;

    // ── CONSTRUCTOR ───────────────────────────────────────────
    // Runs automatically when you write: new LoginPage()
    // Sets up everything the page needs before any action runs
    protected BasePage() {

        // Get the driver that was created in @BeforeMethod
        // DriverManager stores it in ThreadLocal
        // so each test thread gets its own driver
        this.driver = DriverManager.getDriver();

        // Get the single ConfigManager instance
        // Already loaded config.yaml into memory
        // No file reading happens here — just retrieves from memory
        this.config = ConfigManager.getInstance();

        // Create explicit wait using timeout from config.yaml
        // If config says explicit: 20 — waits up to 20 seconds
        // for each condition before throwing TimeoutException
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(config.getExplicitTimeout()));

        // THIS IS THE MOST IMPORTANT LINE IN BasePage
        // PageFactory scans THIS class (and subclasses)
        // for fields annotated with @AndroidFindBy or @iOSXCUITFindBy
        // AppiumFieldDecorator picks the RIGHT annotation
        // based on whether driver is Android or iOS
        // After this line — all your @FindBy fields point
        // to real elements on the screen, not null
        PageFactory.initElements(
                new AppiumFieldDecorator(driver,
                        Duration.ofSeconds(config.getExplicitTimeout())),
                this); // "this" = scan the current page object

        // Log which page was just initialised
        // Useful for debugging test flow
        log.debug("Page initialised: {}", getClass().getSimpleName());
    }

    // ── CLICK ─────────────────────────────────────────────────

    // Click using a WebElement — used with @AndroidFindBy fields
    // Waits for element to be clickable first
    // then clicks — never clicks a disabled or invisible element
    protected void click(WebElement element) {
        waitForClickable(element); // wait first
        element.click();           // then click
        log.debug("Clicked element");
    }

    // Click using a By locator — used for dynamic elements
    // that are not declared as @FindBy fields
    // e.g. click(By.id("dynamic_id_123"))
    protected void click(By locator) {
        waitForVisible(locator).click();
        log.debug("Clicked: {}", locator);
    }

    // ── TYPE TEXT ─────────────────────────────────────────────

    // Type text into a field — used with @AndroidFindBy fields
    // clear() removes existing text first
    // Without clear() — new text gets APPENDED to old text
    // e.g. field has "old" → sendKeys("new") → "oldnew" WRONG
    // With clear() first → "new" CORRECT
    protected void typeText(WebElement element, String text) {
        waitForVisible(element); // wait for field to appear
        element.clear();         // remove existing text
        element.sendKeys(text + "\n");   // type new text
        log.debug("Typed '{}'", text);
    }

    // Same as above but finds element by locator first
    protected void typeText(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    // ── GET TEXT ──────────────────────────────────────────────

    // Gets visible text from an element
    // Has a special iOS fallback:
    // Android → getText() always works
    // iOS → getText() sometimes returns empty string
    //        because iOS stores text in "value" attribute
    //        so we check getAttribute("value") as backup
    protected String getText(WebElement element) {
        waitForVisible(element);
        String text = element.getText(); // try getText() first

        // If getText() returned empty (iOS quirk)
        // try the "value" attribute instead
        if (text == null || text.isEmpty()) {
            text = element.getAttribute("value");
        }

        // trim() removes leading/trailing spaces
        // Return "" instead of null to avoid NullPointerException
        return text != null ? text.trim() : "";
    }

    // ── IS DISPLAYED ─────────────────────────────────────────

    // Returns true if element is visible on screen
    // Returns false if element not found or not visible
    // try-catch prevents test from crashing if element missing
    // Use this in isPageLoaded() to verify page loaded
    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            // Element not found = not displayed = return false
            // NOT a crash — just a false
            return false;
        }
    }

    // Same but finds element by locator first
    // Use when element is not a @FindBy field
    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Returns true if element is enabled (not greyed out)
    // Use to check if a button is active before clicking
    // e.g. login button disabled until both fields filled
    protected boolean isEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ── FIND ELEMENTS ─────────────────────────────────────────

    // Returns a list of all matching elements
    // Use when screen has multiple similar elements
    // e.g. list of products, list of menu items
    protected List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    // ── WAITS ─────────────────────────────────────────────────

    // Wait until element is VISIBLE on screen
    // Polls every 500ms until visible or timeout reached
    // Returns the element so you can chain:
    // waitForVisible(locator).click()
    protected WebElement waitForVisible(By locator) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Same but takes a WebElement instead of By locator
    // Use with @FindBy fields:
    // waitForVisible(loginButton)
    protected WebElement waitForVisible(WebElement element) {
        return wait.until(
                ExpectedConditions.visibilityOf(element));
    }

    // Wait until element is CLICKABLE
    // Clickable = visible AND enabled (not greyed out)
    // Always call this before clicking important buttons
    protected WebElement waitForClickable(WebElement element) {
        return wait.until(
                ExpectedConditions.elementToBeClickable(element));
    }

    // Wait until element DISAPPEARS from screen
    // Use after clicking login — wait for loading spinner to go away
    // before moving to next step
    protected void waitForInvisible(By locator) {
        wait.until(
                ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // Wait until element contains specific text
    // Useful for verifying dynamic content loaded
    // e.g. wait until error message contains "Invalid"
    protected boolean waitForText(WebElement element, String text) {
        return wait.until(
                ExpectedConditions.textToBePresentInElement(element, text));
    }

    // Hard pause — Thread.sleep() — stops everything for exact milliseconds
    // USE THIS AS LAST RESORT ONLY
    // Always prefer waitForVisible() or waitForClickable()
    // Those wait only as long as needed — hardWait always waits full time
    protected void hardWait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Restore interrupted state — Java best practice
            Thread.currentThread().interrupt();
        }
    }

    // ── SCROLL ────────────────────────────────────────────────

    // Scroll down the screen
    // Android and iOS have REVERSED direction conventions
    // Android "down" = content moves down = user sees lower content
    // iOS "up"       = finger swipes up   = same visual result
    // BasePage handles this automatically — you just call scrollDown()
    protected void scrollDown() {
        if (config.isAndroid()) {
            scroll("down"); // Android direction
        } else {
            scroll("up");   // iOS reversed direction
        }
    }

    // Scroll up the screen
    // Same reversal logic as scrollDown()
    protected void scrollUp() {
        if (config.isAndroid()) {
            scroll("up");
        } else {
            scroll("down"); // iOS reversed
        }
    }

    // Private helper — sends the actual scroll command to Appium
    // "mobile: scroll" is Appium's built-in scroll command
    // Works on both Android and iOS
    // JavascriptExecutor sends it as a JS command to the driver
    private void scroll(String direction) {

        // Create a Map to hold the scroll direction parameter
        // HashMap works with all Appium and Java versions
        // Map.of() sometimes fails with Appium's JS executor

        // "direction" is the key Appium expects
        // direction value is "up" or "down"


        // Cast driver to JavascriptExecutor
        // AppiumDriver implements JavascriptExecutor
        // "mobile: scroll" is Appium's built-in scroll command
        Map<String, Object> scrollParams = new HashMap<>();
        scrollParams.put("direction", direction);
        ((JavascriptExecutor) driver).executeScript(
                "mobile: scroll", scrollParams);
    }

    // ── ABSTRACT ──────────────────────────────────────────────

    // abstract = no body here — every subclass MUST implement this
    // Each page defines what "I am fully loaded" means
    //
    // Example in LoginPage:
    // @Override
    // public boolean isPageLoaded() {
    //     return isDisplayed(loginButton);
    // }
    //
    // Used in tests:
    // Assert.assertTrue(loginPage.isPageLoaded());
    // Verifies navigation worked before doing anything else
    public abstract boolean isPageLoaded();
}