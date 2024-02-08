package gov.di_ipv_fraud.utilities;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.fail;

public class PageObjectSupport {

    protected void populateField(By element, String value) {
        getCurrentDriver().findElement(element).click();
        getCurrentDriver().findElement(element).clear();
        getCurrentDriver().findElement(element).sendKeys(value);
        getCurrentDriver().findElement(element).sendKeys(Keys.TAB.toString());
    }

    public static void clickElement(By element) {
        getCurrentDriver().findElement(element).click();
    }

    public static String getText(By element) {
        return getCurrentDriver().findElement(element).getText();
    }

    protected WebElement waitForElementVisible(By by) {
        return waitForElementVisible(by, 5);
    }

    protected WebElement waitForElementVisible(By by, int seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(getCurrentDriver(), Duration.ofSeconds(seconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (NoSuchElementException | TimeoutException e) {
            fail("Element is not visible " + by.toString());
            fail(
                    "Element  "
                            + by.toString()
                            + " is not visible on the page "
                            + getCurrentDriver().getPageSource());
        }
        return getCurrentDriver().findElement(by);
    }

    protected boolean isElementPresent(By by) {
        WebDriverWait wait = new WebDriverWait(getCurrentDriver(), Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(by));
            getCurrentDriver().findElement(by);
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }

    /**
     * Waits up to 1 minute for the page to load. This method should be updated as was created in
     * 2017 and catch block logis is useless
     */
    public void waitForPageToLoad() throws InterruptedException {
        try {
            WebDriverWait wait = new WebDriverWait(getCurrentDriver(), Duration.ofSeconds(60));
            wait.until(
                    drv ->
                            ((JavascriptExecutor) getCurrentDriver())
                                    .executeScript("return document.readyState")
                                    .toString()
                                    .equals("complete"));
        } catch (TimeoutException e) {
            // Swallow this and continue
            // FIXME: Should raise an error when the page can not be loaded as it is confusing to
            // have a separate later test fail!
            // FIXME: Write to logger not standard out! (why this way?)
            new RuntimeException("Wait for page to load returned Timeout exception");
        }
    }

    public static WebDriver getCurrentDriver() {
        return Driver.get();
    }
}
