package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.service.ConfigurationService;
import gov.di_ipv_fraud.utilities.Driver;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Set;
import java.util.logging.Logger;

public class DLUnrecoverableErrorPageObject extends UniversalSteps {

    private final ConfigurationService configurationService;
    private static final Logger LOGGER =
            Logger.getLogger(DLUnrecoverableErrorPageObject.class.getName());

    @FindBy(xpath = "//*[@id=\"main-content\"]/p/a")
    public WebElement visitCredentialIssuers;

    @FindBy(xpath = "//*[@id=\"main-content\"]/p[5]/a/input")
    public WebElement drivingLicenceCRIDev;

    @FindBy(xpath = "//*[@id=\"main-content\"]/form[2]/div/button")
    public WebElement goToDLCRIButton;

    @FindBy(xpath = "//*[@id=\"header\"]")
    public WebElement errorTitle;

    public void DrivingLicenceCRIDev() {
        visitCredentialIssuers.click();
        drivingLicenceCRIDev.click();
    }

    public DLUnrecoverableErrorPageObject() {
        this.configurationService = new ConfigurationService(System.getenv("ENVIRONMENT"));
        PageFactory.initElements(Driver.get(), this);
    }

    public void navigateToDrivingLicenceCRI() {
        goToDLCRIButton.click();
    }

    public void deletecookie() {

        Set<Cookie> cookies = Driver.get().manage().getCookies();
        System.out.println("Size of Cookies:" + cookies.size());

        for (Cookie cookie : cookies) {
            System.out.println(cookie.getName() + ":" + cookie.getValue());
        }
        Driver.get().manage().deleteCookieNamed("service_session");
        // Driver.get().manage().deleteAllCookies();
        Driver.get().navigate().refresh();
    }

    public void errorPageURLValidation() {
        String actualTitle = Driver.get().getTitle();
        String expTitle = "Sorry, there is a problem – – GOV.UK";
        if (expTitle.equals(actualTitle)) {
            LOGGER.info("Pass : Error page is displayed");
        } else {
            LOGGER.info("Fail : Who was your UK driving licence issued by? is displayed");
        }
        System.out.println("title:" + actualTitle);
    }

    public void validateErrorPageHeading() {
        String expectedHeading = "Sorry, there is a problem";
        String actualHeading = errorTitle.getText();
        if (expectedHeading.equals(actualHeading)) {
            LOGGER.info("Pass : Sorry, there is a problem page is displayed");
        } else {
            LOGGER.info("Fail : Who was your UK driving licence issued by? is displayed");
        }
    }
}
