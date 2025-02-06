package com.CSYE6225.webapp.Test;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class HealthCheckTest {

    @BeforeAll
    public static void setup() {
        // Get base URL from system property (use "basurl" instead of "base.url")
        String baseUrl = System.getProperty("basurl", "http://localhost:5000");
        RestAssured.baseURI = baseUrl;
        System.out.println("Base URL set to: " + baseUrl);
    }

    @Test
    public void testHealthCheckSuccess() {
        given()
                .when()
                .get("/healthz")
                .then()
                .statusCode(200)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testInvalidPostMethod() {
        given()
                .when()
                .post("/healthz") // HealthCheck only supports GET
                .then()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testInvalidPutMethod() {
        given()
                .when()
                .put("/healthz") // HealthCheck only supports GET
                .then()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testInvalidDeleteMethod() {
        given()
                .when()
                .delete("/healthz") // HealthCheck only supports GET
                .then()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testInvalidHeadMethod() {
        given()
                .when()
                .head("/healthz") // HealthCheck only supports GET
                .then()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

    @Test
    public void testInvalidOptionMethod() {
        given()
                .when()
                .options("/healthz") // HealthCheck only supports GET
                .then()
                .statusCode(405)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

//    Test for query Parameters

    @Test
    public void testInvalidQueryParamMethod(){
        given()
                .queryParam("key","value")
                .when()
                .get("/healthz")
                .then()
                .statusCode(400)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");

    }

//    // Path parameters
//    @Test
//    public void testInvalidPathParamMethod(){
//        given()
//                .pathParam("key","value")
//                .when()
//                .get("/healthz")
//                .then()
//                .statusCode(400)
//                .header("Cache-Control", "no-cache, no-store, must-revalidate")
//                .header("Pragma", "no-cache")
//                .header("X-Content-Type-Options", "nosniff");
//
//    }

    // Body in request
    @Test
    public void testInvalidBodyRequestMethod(){

        given()
                .body("""
              {
                  /"name": "Test Title"
              }
            """
                )
                .when()
                .get("/healthz")
                .then()
                .statusCode(400)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");


    }

    // Invalid URL added

    @Test
    public void testInvalidUrlMethod(){

        given()
                .when()
                .get("/wrongURL")
                .then()
                .statusCode(404)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff");
    }

//    @Test
//    public void testDatabaseDown() {
//        // stop DB to initiate this specific test
//        // cmd < Run as admin > : net stop MySQL80 ; net start MySQL80
//        given()
//                .when()
//                .get("/healthz")
//                .then()
//                .statusCode(503)
//                .header("Cache-Control", "no-cache, no-store, must-revalidate")
//                .header("Pragma", "no-cache")
//                .header("X-Content-Type-Options", "nosniff");
//    }




}



