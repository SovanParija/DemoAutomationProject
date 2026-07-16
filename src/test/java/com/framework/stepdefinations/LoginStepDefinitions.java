package com.framework.stepdefinations;

import com.framework.pagescommon.LoginPage;
import com.framework.pagescommon.MyDemoAppHomePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginStepDefinitions {

    private LoginPage loginPage;
    private MyDemoAppHomePage homePage;

    // Matches: "Given the app is launched on login screen"
    @Given("the app is launched on login screen")
    public void theAppIsLaunchedOnLoginScreen() {
        // App already launched by Hooks @Before
        // Navigate to login screen via menu
        homePage = new MyDemoAppHomePage();
        homePage.openMenu();
        homePage.clickLogin();
        loginPage = new LoginPage();
    }

    // Matches: "When I enter username {string}"
    @When("I enter username {string}")
    public void iEnterUsername(String username) {
        loginPage.enterUsername(username);
    }

    // Matches: "And I enter password {string}"
    @And("I enter password {string}")
    public void iEnterPassword(String password) {
        loginPage.enterPassword(password);
    }

    // Matches: "And I tap the login button"
    @And("I tap the login button")
    public void iTapTheLoginButton() {
        homePage = loginPage.clickLogin();
    }

    // Matches: "Then the home screen should be displayed"
    @Then("the home screen should be displayed")
    public void theHomeScreenShouldBeDisplayed() {
        Assert.assertTrue(
                homePage.isPageLoaded(),
                "Home screen should be displayed after login");
    }
}