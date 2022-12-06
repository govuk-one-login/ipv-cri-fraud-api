package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.utilities.Driver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class OrchestratorStubPage {

    public OrchestratorStubPage() {
        PageFactory.initElements(Driver.get(), this);
    }

    @FindBy(xpath = "//input[@value='Debug route']")
    public WebElement DebugRoute;

    @FindBy(xpath = "//*[@id='cri-link-ukDrivingLicence']")
    public WebElement UkDrivingLicence;
}
