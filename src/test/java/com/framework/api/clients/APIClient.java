package com.framework.api.clients;

// ╔══════════════════════════════════════════════════════════╗
// ║  API CLIENT                                                ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Thin wrapper around RestAssured's given()/when()/then()  ║
// ║  chain. Same reasoning as BasePage for mobile — hide      ║
// ║  repetitive boilerplate behind simple named methods.       ║
// ║                                                              ║
// ║  Returns raw Response — assertions stay in the TEST class, ║
// ║  not here. This class only sends requests and returns      ║
// ║  results; it does not know what a "correct" response is.    ║
// ╚══════════════════════════════════════════════════════════╝

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

public class APIClient {

    private static final Logger log =
            LogManager.getLogger(APIClient.class);

    private final RequestSpecification reqSpec;

    public APIClient(RequestSpecification reqSpec) {
        this.reqSpec = reqSpec;
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  GET                                                 │
    // └─────────────────────────────────────────────────────┘

    public Response get(String path) {
        log.info("GET {}", path);
        return given(reqSpec)
                .when()
                .get(path);
    }

    // Overload — with query params, e.g. ?page=2
    public Response get(String path,
                        java.util.Map<String, Object> queryParams) {
        log.info("GET {} params={}", path, queryParams);
        return given(reqSpec)
                .queryParams(queryParams)
                .when()
                .get(path);
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  POST                                                 │
    // └─────────────────────────────────────────────────────┘

    public Response post(String path, Object body) {
        log.info("POST {} body={}", path, body);
        return given(reqSpec)
                .body(body)
                .when()
                .post(path);
    }

    // Overload — with an auth token attached as Bearer header
    public Response post(String path, Object body,
                         String token) {
        log.info("POST {} (authenticated)", path);
        return given(reqSpec)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post(path);
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  PUT                                                  │
    // └─────────────────────────────────────────────────────┘

    public Response put(String path, Object body) {
        log.info("PUT {} body={}", path, body);
        return given(reqSpec)
                .body(body)
                .when()
                .put(path);
    }

    // ┌─────────────────────────────────────────────────────┐
    // │  DELETE                                               │
    // └─────────────────────────────────────────────────────┘

    public Response delete(String path) {
        log.info("DELETE {}", path);
        return given(reqSpec)
                .when()
                .delete(path);
    }
}