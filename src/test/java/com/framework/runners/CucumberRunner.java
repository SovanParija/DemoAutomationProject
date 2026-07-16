package com.framework.runners;

// ╔══════════════════════════════════════════════════════════╗
// ║  CUCUMBER RUNNER                                        ║
// ╠══════════════════════════════════════════════════════════╣
// ║  Entry point for Cucumber test execution                ║
// ║  Connects feature files to step definitions             ║
// ║  Configures Allure + JSON reporting                     ║
// ║                                                         ║
// ║  Run: mvn test -Pcucumber                               ║
// ║  Tag: mvn test -Pcucumber -Dcucumber.filter.tags=@smoke ║
// ╚══════════════════════════════════════════════════════════╝

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        // Location of feature files
        features = "src/test/resources/features",

        // Location of step definitions and hooks
        glue = {
                "com.framework.stepdefinations",  // match your folder spelling
                "com.framework.runners"
        },

        // Tags to run — override with -Dcucumber.filter.tags
        tags = "@smoke",

        // Plugins for reporting
        plugin = {
                // Allure Cucumber integration
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                // Pretty console output
                "pretty",
                // JSON report for CI/CD
                "json:target/cucumber-reports/cucumber.json",
                // HTML report
                "html:target/cucumber-reports/cucumber.html"
        },

        // Show step definitions that have no matching steps
        monochrome = true,

        // Publish report to Cucumber cloud (optional)
        publish = false
)
public class CucumberRunner
        extends AbstractTestNGCucumberTests {

    // ┌─────────────────────────────────────────────────────┐
    // │  PARALLEL EXECUTION                                 │
    // │  @DataProvider(parallel=true) runs scenarios        │
    // │  in parallel threads                                │
    // │                                                     │
    // │  Set parallel=false for now                         │
    // │  Enable when multiple devices connected             │
    // └─────────────────────────────────────────────────────┘
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}