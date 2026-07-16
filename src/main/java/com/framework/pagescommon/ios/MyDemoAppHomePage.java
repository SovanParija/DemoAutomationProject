package com.framework.pagescommon.ios;

import com.framework.base.BasePage;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;


public class MyDemoAppHomePage extends BasePage {

    @iOSXCUITFindBy(id = "More-tab-item")
    private WebElement menuIcon;

    @iOSXCUITFindBy(id ="AppTitle Icons")
    private WebElement productsTitle;

    @iOSXCUITFindBy(id ="Cart-tab-item")
    private WebElement cartIcon;

    @iOSXCUITFindBy(id ="Catalog-tab-item")
    private WebElement catalogIcon;


    public MyDemoAppHomePage() {
        super();
        log.info("MyDemoApp Home page initialised");
    }

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
                    log.error("Home page NOT loaded — " + "no elements found after login");
                    return false;
                }
            }
        }
    }


}
