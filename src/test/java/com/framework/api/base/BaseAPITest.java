package com.framework.api.base;

// ╔══════════════════════════════════════════════════════════╗
// ║  BASE API TEST                                            ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Foundation for all REST API tests. Same role as         ║
// ║  BaseTest.java plays for mobile tests — shared setup,     ║
// ║  reused across every API test class.                      ║
// ║                                                            ║
// ║  Deliberately does NOT extend BaseTest — API tests have   ║
// ║  no AppiumDriver, no mobile lifecycle, no device concept. ║
// ║  Sharing that base would drag in irrelevant setup.        ║
// ╚══════════════════════════════════════════════════════════╝

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;

public abstract class BaseAPITest {

    protected static final Logger log =
            LogManager.getLogger(BaseAPITest.class);

    // Shared across all API test classes — built once
    protected static RequestSpecification reqSpec;

    // reqres.in — free test API with a login/token flow that
    // mirrors the mobile app's authentication story
    private static final String BASE_URI =
            "https://reqres.in";
    private static final String BASE_PATH = "/com/framework";

    @BeforeSuite(alwaysRun = true)
    public static void setup() {
        log.info("╔══════════════════════════════╗");
        log.info("║   API TEST SUITE STARTING    ║");
        log.info("╚══════════════════════════════╝");

        RestAssured.baseURI = BASE_URI;
        RestAssured.basePath = BASE_PATH;

        // RequestSpecBuilder — reusable request config so
        // every test doesn't repeat content-type, logging,
        // and base setup individually
        reqSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)   // log request URI only —
                // full body logging is noisy
                // across many tests
                .build();

        log.info("API base URI: {}{}", BASE_URI, BASE_PATH);
    }
}