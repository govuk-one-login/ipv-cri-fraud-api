package gov.di_ipv_fraud.pages;

import org.openqa.selenium.By;

public class ProveYourIdentityGovUkPage extends GlobalPage {
    private static final By USER_INFO = By.cssSelector(".govuk-heading-l");

    public void waitForPageToLoad() {
        waitForElementVisible(USER_INFO, 30);
    }
}
