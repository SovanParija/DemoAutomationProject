package com.framework.api.tests;

// ╔══════════════════════════════════════════════════════════╗
// ║  LOGIN API TEST                                            ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Tests the reqres.in login endpoint directly via HTTP —   ║
// ║  no Appium driver, no mobile device involved.               ║
// ║                                                              ║
// ║  Mirrors the SAME login scenario already covered by         ║
// ║  MDA_1234_ValidLoginTest, but at the API layer — this is    ║
// ║  the foundation for the later "chain API with UI" test.     ║
// ╚══════════════════════════════════════════════════════════╝

import com.framework.api.base.BaseAPITest;
import com.framework.api.clients.APIClient;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

@Epic("REST API")
@Feature("Login")
public class LoginAPITest extends BaseAPITest {

    private final APIClient api = new APIClient(reqSpec);

    // ╭─────────────────────────────────────────────────────╮
    // │  Valid login returns 200 + a token                   │
    // │  reqres.in only accepts specific test credentials —  │
    // │  eve.holt@reqres.in is their documented valid user   │
    // ╰─────────────────────────────────────────────────────╯
    @Test(
            testName = "API-LOGIN-01",
            description = "Valid login returns 200 and a token",
            groups = {"smoke", "api"})
    @Story("Valid login returns a token")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidLoginReturnsToken() {

        Map<String, String> body = new HashMap<>();
        body.put("email", "eve.holt@reqres.in");
        body.put("password", "cityslicka");

        Response response = api.post("/login", body);

        Allure.step("Verify status code is 200");
        Assert.assertEquals(response.getStatusCode(), 200, "Login should succeed with valid test credentials");

        Allure.step("Verify token is present in response");
        String token = response.jsonPath().getString("token");
        Assert.assertNotNull(token, "Response should contain a token");
        Assert.assertFalse(token.isEmpty(), "Token should not be empty");
        log.info("Login succeeded, token: {}", token);
    }

    // ╭─────────────────────────────────────────────────────╮
    // │  Invalid login (missing password) returns 400        │
    // ╰─────────────────────────────────────────────────────╯
    @Test(
            testName = "API-LOGIN-02",
            description = "Login without password returns 400",
            groups = {"smoke", "api"})
    @Story("Invalid login returns an error")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginMissingPasswordReturns400() {

        Map<String, String> body = new HashMap<>();
        body.put("email", "peter@klaven");
        // deliberately no password field

        Response response = api.post("/login", body);

        Allure.step("Verify status code is 400");
        Assert.assertEquals(response.getStatusCode(), 400, "Login without password should be rejected");

        Allure.step("Verify error message is present");
        String error = response.jsonPath().getString("error");
        Assert.assertNotNull(error, "Response should contain an error message");
        log.info("Correctly rejected: {}", error);
    }

    // ╭─────────────────────────────────────────────────────╮
    // │  Response time — basic non-functional check          │
    // ╰─────────────────────────────────────────────────────╯
    @Test(
            testName = "API-LOGIN-03",
            description = "Login responds within 2 seconds",
            groups = {"regression", "api"})
    @Story("Login performance")
    @Severity(SeverityLevel.NORMAL)
    public void testLoginResponseTime() {

        Map<String, String> body = new HashMap<>();
        body.put("email", "eve.holt@reqres.in");
        body.put("password", "cityslicka");

        Response response = api.post("/login", body);

        long responseTime = response.getTime();
        log.info("Login response time: {}ms", responseTime);

        Allure.step("Verify response under 2000ms");
        Assert.assertTrue(responseTime < 2000, "Login took too long: " + responseTime + "ms");
    }
}