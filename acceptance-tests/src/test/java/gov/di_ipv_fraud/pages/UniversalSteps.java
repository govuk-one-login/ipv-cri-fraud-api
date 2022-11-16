package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.utilities.BrowserUtils;
import gov.di_ipv_fraud.utilities.Driver;
import org.openqa.selenium.support.PageFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UniversalSteps {

    public UniversalSteps() {
        PageFactory.initElements(Driver.get(), this);
    }

    public void waitForFiveSeconds() {
        BrowserUtils.waitForPageToLoad(5);
    }

    public void waitForTextToAppear(String text) {
        String header = Driver.get().getTitle();
        Driver.get().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        if (header.contains(text)) {
            assertTrue(Driver.get().getTitle().contains(text));
        } else {
            fail("Page Title Does Not Match " + text + "But was " + Driver.get().getTitle());
        }
    }

    public void driverClose() {
        Driver.closeDriver();
    }

    public void assertURLContains(String expected) {
        String url = Driver.get().getCurrentUrl();
        assertTrue(url.contains(expected));
    }
}
