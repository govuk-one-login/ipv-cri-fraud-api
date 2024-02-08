package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.utilities.PageObjectSupport;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

public class GlobalPage extends PageObjectSupport {

    static final By CONTINUE_BUTTON = By.xpath("//button[@class='govuk-button button']");

    private static Map<String, String> jsonFileResponses = new HashMap<>();

    WebDriver driver;

    public GlobalPage() {
        this.driver = getCurrentDriver();
    }

    public void populateField(By selector, String value) {
        waitForElementVisible(selector, 60);
        WebElement field = getCurrentDriver().findElement(selector);
        field.sendKeys(value);
    }
}
