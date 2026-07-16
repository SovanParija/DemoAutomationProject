package com.framework.pagescommon;

// ╔══════════════════════════════════════════════════════════╗
// ║  MY DEMO APP — HOME PAGE                                ║
// ╠══════════════════════════════════════════════════════════╣
// ║  PURPOSE : Page object for Sauce Labs Demo App          ║
// ║            home screen — shown after successful login   ║
// ║                                                         ║
// ║  EXTENDS : BasePage — gets all common actions           ║
// ║            click(), typeText(), waitForVisible() etc    ║
// ║                                                         ║
// ║  LOCATORS: Found using Appium Inspector                 ║
// ║            on real Pixel device Android 12              ║
// ║                                                         ║
// ║  RETURNS : openMenu()  → stays on home page             ║
// ║            openCart()  → stays on home page             ║
// ╚══════════════════════════════════════════════════════════╝

// ─── Framework base ───────────────────────────────────────
// BasePage provides:
// click(), typeText(), waitForVisible()
// isDisplayed(), isEnabled(), scrollDown()
// PageFactory initialisation via AppiumFieldDecorator
import com.framework.base.BasePage;

// ─── Appium Page Factory ──────────────────────────────────
// @AndroidFindBy connects field to real element on screen
// AppiumFieldDecorator in BasePage reads this annotation
// and finds the element when test runs
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;


// ─── Allure reporting ─────────────────────────────────────
// @Step makes each method appear as a named step
// in the Allure report — makes report readable
import io.qameta.allure.Step;

// ─── Selenium ─────────────────────────────────────────────
// WebElement represents one UI element on screen
// e.g. a button, text view, icon
import org.openqa.selenium.WebElement;

public class MyDemoAppHomePage extends BasePage {

    // ┌─────────────────────────────────────────────────────┐
    // │  LOCATORS                                           │
    // │  Found via Appium Inspector on real Pixel device    │
    // │  id = full resource-id from Inspector               │
    // │                                                     │
    // │  ⚑ RULE: Use resource-id over xpath when possible   │
    // │          resource-id is faster and more stable      │
    // │  ⚑ RULE: Never use index-based xpath               │
    // │          breaks when UI order changes               │
    // └─────────────────────────────────────────────────────┘

    // Products title — "Products" text at top of screen
    // Used as PRIMARY check in isPageLoaded()
    // If this is visible → we are on home screen
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/productTV")
    @iOSXCUITFindBy(accessibility = "AppTitle Icons")
    private WebElement productsTitle;

    // Sort button — top right of product list
    // Used as SECONDARY check if productsTitle not found
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/sortIV")
        @iOSXCUITFindBy(iOSNsPredicate="label == Button AND name == Button AND type == XCUIElementTypeButton")
    private WebElement sortButton;

    // Cart icon — top right navigation
    // Used as FALLBACK check in isPageLoaded()
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/cartIV")
    @iOSXCUITFindBy(accessibility ="Cart-tab-item")
    private WebElement cartIcon;

    // Menu / hamburger icon — top left navigation
    // Tapped to open side menu with logout option
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/menuIV")
    @iOSXCUITFindBy(accessibility = "More-tab-item")
    private WebElement menuIcon;

    // First product name in the list
    // Used to verify products actually loaded
    // content-desc set by Sauce Labs dev team
    @AndroidFindBy(xpath = "(//android.widget.TextView" + "[@content-desc='store item text'])[1]")
    private WebElement firstProductName;

    @AndroidFindBy(accessibility = "Login Menu Item")
    @iOSXCUITFindBy(accessibility="Login Button")

    private WebElement login;

    // ┌─────────────────────────────────────────────────────┐
    // │  CONSTRUCTOR                                        │
    // │  super() calls BasePage constructor which:          │
    // │  1. Gets driver from DriverManager ThreadLocal      │
    // │  2. Creates WebDriverWait with timeout from config  │
    // │  3. Runs PageFactory.initElements()                 │
    // │     which connects all @AndroidFindBy fields above  │
    // │     to real elements on the screen                  │
    // └─────────────────────────────────────────────────────┘
    public MyDemoAppHomePage() {
        super();
        log.info("MyDemoApp Home page initialised");
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  PAGE LOADED CHECK                                  │
    // │                                                     │
    // │  ⚑ RULE: Every page MUST implement this            │
    // │          abstract in BasePage — compiler enforces   │
    // │                                                     │
    // │  Called in test after login:                        │
    // │  Assert.assertTrue(homePage.isPageLoaded(),         │
    // │      "Home page should load after login")           │
    // │                                                     │
    // │  Strategy: Try primary → secondary → fallback       │
    // │  At least ONE must be visible = page loaded         │
    // └─────────────────────────────────────────────────────┘
    @Override
    public boolean isPageLoaded() {
        try {
            // ── Primary check ─────────────────────────────
            // Wait up to 20 seconds for Products title
            // This is the most reliable indicator
            // that home screen fully loaded
            waitForVisible(productsTitle);
            log.info("Home page loaded — Products title visible");
            return true;

        } catch (Exception e1) {
            log.warn("Products title not found — trying cart icon");

            try {
                // ── Secondary check ───────────────────────
                // Cart icon always visible on home screen
                // Try this if products title not found
                waitForVisible(cartIcon);
                log.info("Home page loaded — cart icon visible");
                return true;

            } catch (Exception e2) {
                log.warn("Cart icon not found — trying menu icon");

                try {
                    // ── Fallback check ────────────────────
                    // Menu icon always in top left
                    // Last resort before returning false
                    waitForVisible(menuIcon);
                    log.info("Home page loaded — menu icon visible");
                    return true;

                } catch (Exception e3) {
                    // Nothing found — page not loaded
                    log.error("Home page NOT loaded — " +
                            "no elements found after login");
                    return false;
                }
            }
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  NAVIGATION ACTIONS                                 │
    // │  Each method does ONE thing                         │
    // │  @Step makes each appear in Allure report           │
    // └─────────────────────────────────────────────────────┘

    // ╭─────────────────────────────────────────────────────╮
    // │  openMenu()                                         │
    // ├─────────────────────────────────────────────────────┤
    // │  WHAT : Taps hamburger menu icon                    │
    // │  WHY  : Opens side menu with logout, settings etc   │
    // │  NOTE : Call this before tapLogout()                │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Tap menu icon to open side menu")
    public void openMenu() {
        log.info("Opening menu");
        // click() from BasePage:
        // waits for clickable then clicks
        click(menuIcon);
    }

    // ╭─────────────────────────────────────────────────────╮
    // │  openCart()                                         │
    // ├─────────────────────────────────────────────────────┤
    // │  WHAT : Taps cart icon                              │
    // │  WHY  : Navigates to cart screen                    │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Tap cart icon to open cart")
    public void openCart() {
        log.info("Opening cart");
        click(cartIcon);
    }
    // ╭─────────────────────────────────────────────────────╮
    // │  openLogin Page()                                         │
    // ├─────────────────────────────────────────────────────┤
    // │  WHAT : Taps login button                            │
    // │  WHY  : Navigates to login screen                    │
    //
    @Step("Tap on Login Button")
    public void clickLogin(){
        log.info("click login");
        click(login);
    }

    // ╭─────────────────────────────────────────────────────╮
    // │  logout()                                           │
    // ├─────────────────────────────────────────────────────┤
    // │  WHAT : Opens menu then taps logout                 │
    // │  RETURNS : LoginPage — navigates back to login      │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Logout from home page")
    public LoginPage logout() {
        log.info("Logging out");

        // ── Step 1 ── Open menu ───────────────────────────
        // Logout is inside the hamburger menu
        openMenu();

        // ── Step 2 ── Wait for menu to open ──────────────
        // Small pause for menu animation to complete
        hardWait(1000);

        // ── Step 3 ── Tap logout ──────────────────────────
        // Find and click logout menu item
        // Using xpath — logout item inside menu drawer
        click(org.openqa.selenium.By.xpath(
                "//android.widget.TextView[@text='Log Out']"));

        // ── Step 4 ── Return login page ───────────────────
        // After logout app navigates to login screen
        log.info("Logged out — returning to login page");
        return new LoginPage();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GETTERS                                            │
    // │  Return information FROM the screen                 │
    // │  Used in tests for assertions                       │
    // └─────────────────────────────────────────────────────┘

    // Quick boolean check — used in test assertions
    // Assert.assertTrue(homePage.isHomePageDisplayed())
    // ⚑ RULE: isPageLoaded() is for navigation verification
    //         isHomePageDisplayed() is for quick assertions
    public boolean isHomePageDisplayed() {
        // Check primary OR fallback element
        return isDisplayed(productsTitle)
                || isDisplayed(cartIcon);
    }

    // Get products title text
    // Used to verify correct page title shows
    // Assert.assertEquals(
    //     homePage.getProductsTitle(), "Products")
    public String getProductsTitle() {
        // getText() from BasePage
        // has iOS fallback for value attribute
        return getText(productsTitle);
    }
}