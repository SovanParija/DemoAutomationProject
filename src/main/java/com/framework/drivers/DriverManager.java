package com.framework.drivers;

import com.framework.config.ConfigManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Driver Manager - creates and Manages Appium Driver
 * Uses ThreadLocal so each thread gets it own driver
 * supports Android iOS and BrowserStack
 */
public class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<AppiumDriver> driverThread = new ThreadLocal<>();

    //get the ConfigManager singleton once
    private static final ConfigManager configManager = ConfigManager.getInstance();

    //Call the constructor
    private DriverManager() {

    }

    //------------------------------------------------------
    //INIT-called by Base Test @BeforeMethod
    //------------------------------------------------------

    public static void initDriver() {
        //Don't create4 a second driver if driver exists
        if (driverThread.get() != null) {
            log.warn("Driver already exists -skipping INIT");
            return;
        }

        String platform = configManager.getPlatform();
        //Decide Which Driver to create based on Platform
        AppiumDriver driver = switch (platform) {
            case "android" -> createAndroidDriver();
            case "ios" -> createIOSDriver();
            case "browserstack" -> createBrowserStackDriver();
            default ->
                    throw new IllegalArgumentException("Unknown platform: " + platform + "\nValid values: android, ios, browserstack" + "\nCheck config.yaml or -Dplatform flag");
        };

        //set implicit wait time for config.yaml
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(configManager.getImplicitTimeout()));

        //store Driver in threadlocal for this thread
        driverThread.set(driver);
        log.info("Driver ready :{}", driver.getClass().getSimpleName());
    }

    //------------------------------------------------------
    //GET- called by BasePage and Tests
    //------------------------------------------------------
    public static AppiumDriver getDriver() {
        AppiumDriver driver = driverThread.get();
        if (driver == null) {
            throw new IllegalStateException("Driver is null call initDriver() first");

        }
        return driver;
    }

    //------------------------------------------------------
    //QuitDriver-called by Base Test @AfterMethod
    //------------------------------------------------------

    public static void quitDriver() {
        AppiumDriver driver = driverThread.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("Driver Quit Successfully");

            } catch (Exception e) {
                log.error("Error quitting driver", e.getMessage());
            } finally {
                //Always remove from threadlocal-even if quit fails
                driverThread.remove();

            }

        }
    }

    public static boolean isDriverInitialized() {
        return driverThread.get() != null;

    }


    private static AppiumDriver createBrowserStackDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        Map<String, Object> bStackOptions = new HashMap<>();
        bStackOptions.put("userName", configManager.getBrowserstackUsername());
        bStackOptions.put("accessKey", configManager.getBrowserstackAccessKey());
        bStackOptions.put("projectName", "AppiumTDDFramework");
        bStackOptions.put("buildName", "CI-Build-1.0");
        bStackOptions.put("sessionName", "Test Run");
        bStackOptions.put("debug", "true");
        bStackOptions.put("networkLogs", "true");

        String sub = System.getProperty("bs.platform", "android");
        capabilities.setCapability("platformName", sub.equals("iOS") ? "iOS" : "android");
        capabilities.setCapability("automationName", sub.equals("iOS") ? "XCUITest" : "UIAutomator2");
        capabilities.setCapability("bstack:options", bStackOptions);

        try {
            URL url = new URL(configManager.getBSHub());
            log.info("Connecting to BrowserStack: {}", url);
            return sub.equals("ios") ? new IOSDriver(url, capabilities) : new AndroidDriver(url, capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad Browserstack URL", e);
        }
    }


    private static AppiumDriver createIOSDriver() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("platformName", "iOS");
        capabilities.setCapability("appium:deviceName",configManager.getIOSDevice());
        capabilities.setCapability("appium:platformVersion",configManager.getIOSPlatformVersion());
        capabilities.setCapability("appium:automationName",configManager.getIOSAuto());
        capabilities.setCapability("appium:udid",configManager.getIOSUdid());
        capabilities.setCapability("appium:bundleId", configManager.getIOSBundleId());
        capabilities.setCapability("appium:noReset", false);
        capabilities.setCapability("appium:newCommandTimeout", 300);
        capabilities.setCapability("appium:wdaLaunchTimeout", configManager.getWdaLaunchTimeout());

        try {
            URL url = new URL("http://"
                    + configManager.getAppiumHost()
                    + ":" + configManager.getAppiumPort());
            log.info("Connecting to Appium Server: {}", url);
            return new IOSDriver(url, capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad Appium URL", e);
        }
    }

    private static AndroidDriver createAndroidDriver() {

        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:deviceName", configManager.getAndroidDevice());
        caps.setCapability("appium:platformVersion", configManager.getAndroidVersion());
        caps.setCapability("appium:automationName", configManager.getAndroidAuto());
        caps.setCapability("appium:noReset", false);
        caps.setCapability("appium:autoGrantPermissions", true);
        caps.setCapability("appium:newCommandTimeout", 300);

        caps.setCapability("appium:appPackage", "com.saucelabs.mydemoapp.android");
        caps.setCapability("appium:appActivity", "com.saucelabs.mydemoapp.android.view.activities.SplashActivity");
        // Accept any activity that renders immediately after the Splash screen
        caps.setCapability("appium:appWaitActivity", "com.saucelabs.mydemoapp.android.view.activities.*");
        // Allow up to 30 seconds for the app to finish its initial load/redirects
        caps.setCapability("appium:appWaitDuration", 30000);

        // Use APK path — bypasses Android 16 security restriction
        caps.setCapability("appium:app",
                System.getProperty("user.dir")
                        + "/src/test/resources/apps/mda-2.2.0-25.apk");

        try {
            URL url = new URL("http://"
                    + configManager.getAppiumHost()
                    + ":" + configManager.getAppiumPort());
            log.info("Connecting to Appium: {}", url);
            return new AndroidDriver(url, caps);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad Appium URL", e);
        }
    }
}
