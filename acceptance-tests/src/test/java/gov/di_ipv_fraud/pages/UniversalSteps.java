package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.utilities.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.PageFactory;

import static gov.di_ipv_fraud.utilities.BrowserUtils.waitForPageToLoad;
import static org.junit.Assert.assertTrue;

public class UniversalSteps {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final int MAX_WAIT_SEC = 10;

    public UniversalSteps() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void assertPageTitle(String expTitle, boolean fuzzy) {
        waitForPageToLoad(MAX_WAIT_SEC);

        String title = Driver.get().getTitle();

        boolean match = fuzzy ? title.contains(expTitle) : title.equals(expTitle);

        LOGGER.info("Page title: " + title);
        assertTrue(match);
    }

    public void driverClose() {
        Driver.closeDriver();
    }

    public void assertURLContains(String expected) {
        waitForPageToLoad(MAX_WAIT_SEC);

        String url = Driver.get().getCurrentUrl();

        LOGGER.info("Page url: " + url);
        assertTrue(url.contains(expected));
    }
}
