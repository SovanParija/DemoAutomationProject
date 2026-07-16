package com.framework.testscommon;

// ╔══════════════════════════════════════════════════════════╗
// ║  JIRA TICKET : MDA-1234                                 ║
// ║  TITLE       : Valid login navigates to home screen     ║
// ║  TYPE        : Happy path — smoke                       ║
// ║  PRIORITY    : Critical                                 ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.base.BaseTest;
import com.framework.pagescommon.LoginPage;
import com.framework.pagescommon.MyDemoAppHomePage;
import com.framework.utils.TestDataManager;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

@Epic("Android App")
@Feature("Login")
@Story("MDA-1234 — Valid login")
public class MDA_1234_ValidLoginTest extends BaseTest {

    // ╭─────────────────────────────────────────────────────╮
    // │  MDA-1234                                           │
    // │  Valid login navigates to home screen               │
    // ├─────────────────────────────────────────────────────┤
    // │  GIVEN : App is launched on login screen            │
    // │  WHEN  : User enters valid credentials              │
    // │  THEN  : Home screen is displayed                   │
    // ╰─────────────────────────────────────────────────────╯
    @Test(testName = "MDA-1234", description = "Valid login navigates to home screen", groups = {"smoke", "MDA-1234"})
    @Severity(SeverityLevel.CRITICAL)
    @Link(name = "MDA-1234", url = "https://yourjira.atlassian.net/browse/MDA-1234")
    public void testValidLogin() {
        Map<String, String> data = TestDataManager.getLoginData("validUser");
        step("MDA-1234 | click on cart icon");
        MyDemoAppHomePage homePage = new MyDemoAppHomePage();
        homePage.openMenu();
        homePage.clickLogin();

        step("MDA-1234 | Launch login screen");
        LoginPage loginPage = new LoginPage();

        step("MDA-1234 | Verify login screen displayed");
        Assert.assertTrue(loginPage.isPageLoaded(), "MDA-1234 | Login screen should be displayed");
        step("MDA-1234 | Enter valid credentials and login");
        homePage = loginPage.login(data.get("username"),data.get("password"));

        step("MDA-1234 | Verify home screen displayed");
        Assert.assertTrue(homePage.isPageLoaded(), "MDA-1234 | Home screen should load after login");
    }
}