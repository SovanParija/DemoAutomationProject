package com.framework.pagescommon;

// ╔══════════════════════════════════════════════════════════╗
// ║  MY DEMO APP — LOGIN PAGE                               ║
// ╠══════════════════════════════════════════════════════════╣
// ║  PURPOSE : Page object for Sauce Labs Demo App          ║
// ║            login screen                                 ║
// ║                                                         ║
// ║  LOCATORS: Found using Appium Inspector                 ║
// ║            on real Pixel device                         ║
// ║                                                         ║
// ║  RETURNS : clickLogin()  → MyDemoAppHomePage            ║
// ║            everything else → LoginPage (stay on screen) ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.base.BasePage;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;

import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

public class LoginPage extends BasePage {

    // ┌─────────────────────────────────────────────────────┐
    // │  LOCATORS                                           │
    // │  Found via Appium Inspector on real Pixel device    │
    // │  id = full resource-id from Inspector               │
    // │  accessibility = accessibility id from Inspector    │
    // └─────────────────────────────────────────────────────┘

    // Username input field
    // resource-id found in Appium Inspector
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/nameET")
    @iOSXCUITFindBy(iOSNsPredicate= "type=='XCUIElementTypeTextField'")
    private WebElement usernameField;

    // Password input field
    // ⚑ Using passwordET not passwordRL
    // passwordRL is the container — passwordET is the input
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/passwordET")
    @iOSXCUITFindBy(iOSNsPredicate= "type == 'XCUIElementTypeSecureTextField'")
    private WebElement passwordField;

    // Login button — accessibility id from Inspector
    @AndroidFindBy(accessibility = "Tap to login with given credentials")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'Login' AND type == 'XCUIElementTypeButton'")
    private WebElement loginButton;

    // Forgot password link
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/forgotLoginInfoTV")
    private WebElement forgotPasswordLink;

    // Error message — shown when login fails
    // e.g. "Username and password do not match"
    @AndroidFindBy(id = "com.saucelabs.mydemoapp.android:id/errorTV")
    private WebElement errorMessage;

    // Login screen title — used in isPageLoaded()
    @AndroidFindBy(id ="com.saucelabs.mydemoapp.android:id/loginTV")
    @iOSXCUITFindBy(iOSNsPredicate = "name == 'Login' AND type == 'XCUIElementTypeStaticText'")
    private WebElement loginScreenTitle;

    // ┌─────────────────────────────────────────────────────┐
    // │  CONSTRUCTOR                                        │
    // │  super() → BasePage → PageFactory.initElements()   │
    // │  connects all @AndroidFindBy fields above           │
    // └─────────────────────────────────────────────────────┘
    public LoginPage() {
        super();
        log.info("Login page initialised");
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  PAGE LOADED CHECK                                  │
    // │  Returns true when login screen is ready            │
    // │  Called in test:                                    │
    // │  Assert.assertTrue(loginPage.isPageLoaded())        │
    // └─────────────────────────────────────────────────────┘
    @Override
    public boolean isPageLoaded() {
        // ⚑ Both elements must be visible
        // loginButton visible = page ready to interact
        // usernameField visible = input ready
        try {
            // Wait for login button to be visible
            // gives page time to fully render
            waitForVisible(loginButton);
            return isDisplayed(loginButton)
                    && isDisplayed(usernameField);
        } catch (Exception e) {
            log.warn("Login page not loaded yet: {}",
                    e.getMessage());
            return false;
        }
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  INPUT ACTIONS                                      │
    // │  return this → enables method chaining             │
    // │  loginPage.enterUsername("x").enterPassword("y")    │
    // └─────────────────────────────────────────────────────┘

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        log.info("Entering username: {}", username);
        // typeText() from BasePage:
        // waits for field → clears → types
        typeText(usernameField, username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        log.info("Entering password");
        typeText(passwordField, password);
        return this;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  NAVIGATION ACTIONS                                 │
    // └─────────────────────────────────────────────────────┘

    // ╭─────────────────────────────────────────────────────╮
    // │  clickLogin()                                       │
    // ├─────────────────────────────────────────────────────┤
    // │  WHAT    : Clicks login button                      │
    // │  EXPECTS : Successful login                         │
    // │  RETURNS : MyDemoAppHomePage                        │
    // │            Because after SUCCESS we are on HOME     │
    // │            not on login screen                      │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Tap login button — expecting success")
    public MyDemoAppHomePage clickLogin() {
        log.info("Tapping login button");
        click(loginButton);
        // ⚑ RULE: Return the page we NAVIGATE TO
        // Successful login → home screen
        return new MyDemoAppHomePage();
    }

    // ╭─────────────────────────────────────────────────────╮
    // │  clickLoginExpectingError()                         │
    // ├─────────────────────────────────────────────────────┤
    // │  WHAT    : Clicks login button                      │
    // │  EXPECTS : Login FAILS — error shown                │
    // │  RETURNS : LoginPage                                │
    // │            Because on FAILURE we stay here          │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Tap login button — expecting error")
    public LoginPage clickLoginExpectingError() {
        log.info("Tapping login — expecting failure");
        click(loginButton);
        // Wait for error message to appear
        // If it never appears → TimeoutException
        waitForVisible(errorMessage);
        return this;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  COMBINED ACTIONS                                   │
    // │  Convenience methods that chain multiple steps      │
    // └─────────────────────────────────────────────────────┘

    // ╭─────────────────────────────────────────────────────╮
    // │  login()                                            │
    // ├─────────────────────────────────────────────────────┤
    // │  Most used method in tests                          │
    // │  Chains: enterUsername → enterPassword → clickLogin │
    // │  Returns: MyDemoAppHomePage                         │
    // │                                                     │
    // │  Usage:                                             │
    // │  MyDemoAppHomePage home = loginPage.login(          │
    // │      data.get("username"),                          │
    // │      data.get("password"));                         │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Login with username: {username}")
    public MyDemoAppHomePage login(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLogin();
    }

    // ╭─────────────────────────────────────────────────────╮
    // │  loginExpectingFailure()                            │
    // ├─────────────────────────────────────────────────────┤
    // │  Used for negative tests                            │
    // │  Chains: enterUsername → enterPassword →            │
    // │          clickLoginExpectingError                   │
    // │  Returns: LoginPage — stays on login screen         │
    // ╰─────────────────────────────────────────────────────╯
    @Step("Attempt login with invalid credentials")
    public LoginPage loginExpectingFailure(
            String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLoginExpectingError();
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GETTERS                                            │
    // │  Return information FROM the screen                 │
    // │  Used in tests for assertions                       │
    // └─────────────────────────────────────────────────────┘

    // Get error message text for assertion
    // Assert.assertTrue(
    //     loginPage.getErrorMessageText()
    //         .contains("Username and password"))
    public String getErrorMessageText() {
        return getText(errorMessage);
    }

    // Check if error message is visible
    // Assert.assertTrue(loginPage.isErrorMessageDisplayed())
    public boolean isErrorMessageDisplayed() {
        return isDisplayed(errorMessage);
    }

    // Check if login button is enabled
    // TDD: button disabled when both fields empty
    public boolean isLoginButtonEnabled() {
        return isEnabled(loginButton);
    }
}