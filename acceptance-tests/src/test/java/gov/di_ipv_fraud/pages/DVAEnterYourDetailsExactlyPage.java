package gov.di_ipv_fraud.pages;

import gov.di_ipv_fraud.utilities.BrowserUtils;
import gov.di_ipv_fraud.utilities.DVADrivingLicenceSubject;
import gov.di_ipv_fraud.utilities.Driver;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DVAEnterYourDetailsExactlyPage {
    public DVAEnterYourDetailsExactlyPage() {
        PageFactory.initElements(Driver.get(), this);
    }

    @FindBy(id = "surname")
    public WebElement Lastname;

    @FindBy(id = "firstName")
    public WebElement Firstname;

    @FindBy(id = "dvaDateOfBirth-day")
    public WebElement DayOfBirth;

    @FindBy(id = "dvaDateOfBirth-month")
    public WebElement MonthOfBirth;

    @FindBy(id = "dvaDateOfBirth-year")
    public WebElement YearOfBirth;

    @FindBy(id = "expiryDate-day")
    public WebElement LicenceValidToDay;

    @FindBy(id = "expiryDate-month")
    public WebElement LicenceValidToMonth;

    @FindBy(id = "expiryDate-year")
    public WebElement LicenceValidToYear;

    @FindBy(id = "dateOfIssue-day")
    public WebElement LicenceIssueDay;

    @FindBy(id = "dateOfIssue-month")
    public WebElement LicenceIssueMonth;

    @FindBy(id = "dateOfIssue-year")
    public WebElement LicenceIssueYear;

    @FindBy(id = "postcode")
    public WebElement Postcode;

    @FindBy(id = "dvaLicenceNumber")
    public WebElement dvaLicenceNumber;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dvaDateOfBirth-day')]")
    public WebElement DVAInvalidDOBError;

    @FindBy(id = "dvaDateOfBirth-error")
    public WebElement DVAInvalidDOBFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dateOfIssue-day')]")
    public WebElement DVAInvalidIssueDateError;

    @FindBy(id = "dateOfIssue-error")
    public WebElement DVAInvalidIssueDateFieldError;

    @FindBy(
            xpath =
                    "//*[@class='govuk-error-summary error-summary']//*[@class='govuk-error-summary__body']//*[@class='govuk-list govuk-error-summary__list']//*[contains(@href,'#dvaLicenceNumber')]")
    public WebElement DVAInvalidDrivingLicenceError;

    @FindBy(id = "dvaLicenceNumber-error")
    public WebElement DVADrivingLicenceFieldError;

    public void userEntersDVAData(DVADrivingLicenceSubject dvaDrivingLicenceSubject) {
        dvaLicenceNumber.sendKeys(dvaDrivingLicenceSubject.getDVAlicenceNumber());
        Lastname.sendKeys(dvaDrivingLicenceSubject.getlastName());
        Firstname.sendKeys(dvaDrivingLicenceSubject.getfirstName());
        DayOfBirth.sendKeys(dvaDrivingLicenceSubject.getbirthDay());
        MonthOfBirth.sendKeys(dvaDrivingLicenceSubject.getbirthMonth());
        YearOfBirth.sendKeys(dvaDrivingLicenceSubject.getbirthYear());
        LicenceValidToDay.sendKeys(dvaDrivingLicenceSubject.getvalidtoDay());
        LicenceValidToMonth.sendKeys(dvaDrivingLicenceSubject.getvalidtoMonth());
        LicenceValidToYear.sendKeys(dvaDrivingLicenceSubject.getvalidtoYear());
        LicenceIssueDay.sendKeys(dvaDrivingLicenceSubject.getlicenceIssueDay());
        LicenceIssueMonth.sendKeys(dvaDrivingLicenceSubject.getlicenceIssueMonth());
        LicenceIssueYear.sendKeys(dvaDrivingLicenceSubject.getlicenceIssueYear());
        Postcode.sendKeys(dvaDrivingLicenceSubject.getpostcode());
    }

    public void userEntersInvalidDVADrivingDetails() {
        dvaLicenceNumber.sendKeys("11110610");
        Lastname.sendKeys("Testlastname");
        Firstname.sendKeys("Testfirstname");
        DayOfBirth.sendKeys("11");
        MonthOfBirth.sendKeys("10");
        YearOfBirth.sendKeys("1962");
        LicenceValidToDay.sendKeys("01");
        LicenceValidToMonth.sendKeys("01");
        LicenceValidToYear.sendKeys("2030");
        LicenceIssueDay.sendKeys("10");
        LicenceIssueMonth.sendKeys("12");
        LicenceIssueYear.sendKeys("1970");
        Postcode.sendKeys("BS98 1AA");
        BrowserUtils.waitFor(3);
        BrowserUtils.waitForPageToLoad(10);
    }

    public void userReEntersDataAsDVADrivingLicenceSubject(
            DVADrivingLicenceSubject dvaDrivingLicenceSubject) {
        dvaLicenceNumber.clear();
        Lastname.clear();
        Firstname.clear();
        DayOfBirth.clear();
        MonthOfBirth.clear();
        YearOfBirth.clear();
        LicenceValidToDay.clear();
        LicenceValidToMonth.clear();
        LicenceValidToYear.clear();
        LicenceIssueDay.clear();
        LicenceIssueMonth.clear();
        LicenceIssueYear.clear();
        Postcode.clear();
        dvaLicenceNumber.sendKeys(dvaDrivingLicenceSubject.getDVAlicenceNumber());
        Lastname.sendKeys(dvaDrivingLicenceSubject.getlastName());
        Firstname.sendKeys(dvaDrivingLicenceSubject.getfirstName());
        DayOfBirth.sendKeys(dvaDrivingLicenceSubject.getbirthDay());
        MonthOfBirth.sendKeys(dvaDrivingLicenceSubject.getbirthMonth());
        YearOfBirth.sendKeys(dvaDrivingLicenceSubject.getbirthYear());
        LicenceValidToDay.sendKeys(dvaDrivingLicenceSubject.getvalidtoDay());
        LicenceValidToMonth.sendKeys(dvaDrivingLicenceSubject.getvalidtoMonth());
        LicenceValidToYear.sendKeys(dvaDrivingLicenceSubject.getvalidtoYear());
        LicenceIssueDay.sendKeys(dvaDrivingLicenceSubject.getlicenceIssueDay());
        LicenceIssueMonth.sendKeys(dvaDrivingLicenceSubject.getlicenceIssueMonth());
        LicenceIssueYear.sendKeys(dvaDrivingLicenceSubject.getlicenceIssueYear());
        Postcode.sendKeys(dvaDrivingLicenceSubject.getpostcode());
    }

    public void invalidDVADOBErrorDisplayed() {
        Assert.assertEquals(
                "Check you have entered your date of birth correctly",
                DVAInvalidDOBError.getText());
    }

    public void invalidDVADOBFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Check you have entered your date of birth correctly",
                DVAInvalidDOBFieldError.getText());
    }

    public void futureDVADOBErrorDisplayed() {
        Assert.assertEquals("Your date of birth must be in the past", DVAInvalidDOBError.getText());
    }

    public void futureDVADOBFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your date of birth must be in the past",
                DVAInvalidDOBFieldError.getText());
    }

    public void noDVADOBErrorDisplayed() {
        Assert.assertEquals(
                "Enter your date of birth as it appears on your driving licence",
                DVAInvalidDOBError.getText());
    }

    public void noDVADOBFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter your date of birth as it appears on your driving licence",
                DVAInvalidDOBFieldError.getText());
    }

    public void invalidDVAIssueDateErrorDisplayed() {
        Assert.assertEquals(
                "Enter the date as it appears on your driving licence",
                DVAInvalidIssueDateError.getText());
    }

    public void invalidDVAIssueDateFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter the date as it appears on your driving licence",
                DVAInvalidIssueDateFieldError.getText());
    }

    public void futureIssueDateDVAErrorDisplayed() {
        Assert.assertEquals(
                "The issue date must be in the past", DVAInvalidIssueDateError.getText());
    }

    public void futureIssueDateDVAFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "The issue date must be in the past",
                DVAInvalidIssueDateFieldError.getText());
    }

    public void shortDVADrivingLicenceNumErrorDisplayed() {
        Assert.assertEquals(
                "Your licence number should be 8 characters long",
                DVAInvalidDrivingLicenceError.getText());
    }

    public void shortDVADrivingLicenceNumFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your licence number should be 8 characters long",
                DVADrivingLicenceFieldError.getText());
    }

    public void specialCharDrivingLicenceDVAErrorDisplayed() {
        Assert.assertEquals(
                "Your licence number should not include any symbols or spaces",
                DVAInvalidDrivingLicenceError.getText());
    }

    public void specialCharDrivingLicenceDVAFieldErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Your licence number should not include any symbols or spaces",
                DVADrivingLicenceFieldError.getText());
    }

    public void invalidDrivingLicenceDVAErrorDisplayed() {
        Assert.assertEquals(
                "Enter the number exactly as it appears on your driving licence",
                DVAInvalidDrivingLicenceError.getText());
    }

    public void invalidDrivingLicenceFieldDVAErrorDisplayed() {
        Assert.assertEquals(
                "Error:\n" + "Enter the number exactly as it appears on your driving licence",
                DVADrivingLicenceFieldError.getText());
    }
}
