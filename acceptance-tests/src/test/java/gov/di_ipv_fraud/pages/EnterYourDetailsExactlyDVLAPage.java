package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.utilities.Driver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EnterYourDetailsExactlyDVLAPage {
    public EnterYourDetailsExactlyDVLAPage() {
        PageFactory.initElements(Driver.get(), this);
    }

    @FindBy(id = "drivingLicenceNumber")
    public WebElement drivingLicenceNumber;

    @FindBy(id = "surname")
    public WebElement Lastname;

    @FindBy(id = "firstName")
    public WebElement Firstname;

    @FindBy(id = "middleNames")
    public WebElement Middlenames;

    @FindBy(id = "dateOfBirth-day")
    public WebElement DayOfBirth;

    @FindBy(id = "dateOfBirth-month")
    public WebElement MonthOfBirth;

    @FindBy(id = "dateOfBirth-year")
    public WebElement YearOfBirth;

    @FindBy(id = "expiryDate-day")
    public WebElement LicenceValidToDay;

    @FindBy(id = "expiryDate-month")
    public WebElement LicenceValidToMonth;

    @FindBy(id = "expiryDate-year")
    public WebElement LicenceValidToYear;

    @FindBy(id = "licenceIssuerRadio-DVLA-label")
    public WebElement DVLALabel;

    @FindBy(id = "issueDate-day")
    public WebElement LicenceIssueDay;

    @FindBy(id = "issueDate-month")
    public WebElement LicenceIssueMonth;

    @FindBy(id = "issueDate-year")
    public WebElement LicenceIssueYear;

    @FindBy(id = "issueNumber")
    public WebElement IssueNumber;

    @FindBy(id = "postcode")
    public WebElement Postcode;
}
