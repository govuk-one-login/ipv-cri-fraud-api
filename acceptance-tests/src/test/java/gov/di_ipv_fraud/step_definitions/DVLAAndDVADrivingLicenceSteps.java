package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.*;
import gov.di_ipv_fraud.utilities.DVADrivingLicenceSubject;
import gov.di_ipv_fraud.utilities.DrivingLicenceSubject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

import java.io.IOException;

public class DVLAAndDVADrivingLicenceSteps extends DrivingLicencePageObject {

    @When("User enters data as a {}")
    public void user_enters_and(DrivingLicenceSubject drivingLicenceSubject) {
        userEntersData(drivingLicenceSubject);
    }

    @When("User enters DVA data as a {}")
    public void user_enters_dva_data_and(DVADrivingLicenceSubject dvaDrivingLicenceSubject) {
        new DVAEnterYourDetailsExactlyPage().userEntersDVAData(dvaDrivingLicenceSubject);
    }

    @When("User clicks on continue")
    public void user_clicks_on_continue() {
        Continue.click();
    }

    @Then("Proper error message for Could not find your details is displayed")
    public void properErrorMessageForCouldNotFindDVLADetailsIsDisplayed() {
        couldNotFindDetailsErrorDisplayed();
    }

    @Then(
            "Check you have entered your date of birth correctly error should be displayed in the Error summary")
    public void errorMessageForInvalidDOBIsDisplayed() {
        invalidDOBErrorDisplayed();
    }

    @Then(
            "Check you have entered your date of birth correctly Field error message should be displayed")
    public void fieldErrorMessageForInvalidDateOfBirthIsDisplayed() {
        invalidDateOfBirthFieldErrorDisplayed();
    }

    @Then("Proper error message for no or invalid Date of Birth should be displayed")
    public void properErrorMessageForNoDOBIsDisplayed() {
        noDOBErrorDisplayed();
    }

    @Then("Field error message for no or invalid Date of Birth should be displayed")
    public void fieldErrorMessageForNoDOBIsDisplayed() {
        noDateOfBirthFieldErrorDisplayed();
    }

    @Then("Your date of birth must be in the past error should be displayed in the Error summary")
    public void errorMessageForFutureDOBIsDisplayed() {
        futureDOBErrorDisplayed();
    }

    @Then("Your date of birth must be in the past Field error message should be displayed")
    public void fieldErrorMessageForFutureDOBIsDisplayed() {
        futureDOBFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid Issue date should be displayed in the Error summary")
    public void properErrorMessageForInvalidIssueDateIsDisplayed() {
        invalidIssueDateErrorDisplayed();
    }

    @Then("Field error message for invalid Issue date should be displayed")
    public void fieldErrorMessageForInvalidIssueDateIsDisplayed() {
        invalidIssueDateFieldErrorDisplayed();
    }

    @Then(
            "The issue date must be in the past error message should be displayed in the Error summary")
    public void errorMessageForFutureIssueDateIsDisplayed() {
        futureIssueDateErrorDisplayed();
    }

    @Then("The issue date must be in the past Field error message should be displayed")
    public void fieldErrorMessageForFutureIssueDateIsDisplayed() {
        futureIssueDateFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid Valid to date should be displayed in the Error summary")
    public void properErrorMessageForInvalidValidToDateIsDisplayed() {
        invalidValidToDateErrorDisplayed();
    }

    @Then("Field error message for invalid Valid to date should be displayed")
    public void fieldErrorMessageForInvalidValidToDateIsDisplayed() {
        invalidValidToDateFieldErrorDisplayed();
    }

    @Then(
            "Proper error message for expired driving licence should be displayed in the Error summary")
    public void properErrorMessageForExpiredDrivingLicenceIsDisplayed() {
        expiredDrivingLicenceErrorDisplayed();
    }

    @Then("Field error message for expired driving licence should be displayed")
    public void fieldErrorMessageForExpiredDrivingLicenceIsDisplayed() {
        expiredDrivingLicenceFieldErrorDisplayed();
    }

    @Then(
            "Your licence number should be 16 characters long error message should be displayed in the Error summary")
    public void shortDrivingLicenceNumberErrorMessageIsDisplayed() {
        shortDrivingLicenceNumberErrorDisplayed();
    }

    @Then("Your licence number should be 16 characters long Field error should be displayed")
    public void shortDrivingLicenceNumberFieldErrorMessageIsDisplayed() {
        shortDrivingLicenceNumberFieldErrorDisplayed();
    }

    @Then(
            "Your licence number should not include any symbols or spaces error message should be displayed in the Error summary")
    public void errorMessageForDrivingLicenceNumWithSpecialCharIsDisplayed() {
        specialCharDrivingLicenceErrorDisplayed();
    }

    @Then(
            "Your licence number should not include any symbols or spaces Field error should be displayed")
    public void fieldErrorForDrivingLicenceNumWithSpecialCharIsDisplayed() {
        specialCharDrivingLicenceFieldErrorDisplayed();
    }

    @Then(
            "Proper error message for invalid Licence number should be displayed in the Error summary")
    public void properErrorMessageForInvalidDrivingLicenceIsDisplayed() {
        invalidDrivingLicenceErrorDisplayed();
    }

    @Then("Field error message for invalid Licence number should be displayed")
    public void fieldErrorMessageForInvalidDrivingLicenceIsDisplayed() {
        invalidDrivingLicenceFieldErrorDisplayed();
    }

    @Then(
            "Your issue number should be 2 numbers long error message should be displayed in the Error summary")
    public void shortIssueNumberErrorMessageIsDisplayed() {
        shortIssueNumberErrorDisplayed();
    }

    @Then("Your issue number should be 2 numbers long Field error should be displayed")
    public void shortIssueNumberFieldErrorMessageIsDisplayed() {
        shortIssueNumberFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid Issue number should be displayed in the Error summary")
    public void properErrorMessageForInvalidIssueNumberIsDisplayed() {
        invalidIssueNumberErrorDisplayed();
    }

    @Then("Field error message for invalid Issue number should be displayed")
    public void fieldErrorMessageForInvalidIssueNumberIsDisplayed() {
        invalidIssueNumberFieldErrorDisplayed();
    }

    @Then(
            "Your issue number should not include any symbols or spaces error message should be displayed in the Error summary")
    public void errorMessageForIssueNumberWithSpecialCharIsDisplayed() {
        specialCharIssueNumberErrorDisplayed();
    }

    @Then(
            "Your issue number should not include any symbols or spaces Field error should be displayed")
    public void fieldErrorMessageForIssueNumberWithSpecialCharIsDisplayed() {
        specialCharIssueNumberFieldErrorDisplayed();
    }

    @Then(
            "Your postcode should be between 5 and 7 characters error should be displayed in the Error summary")
    public void shortPostcodeErrorMessageIsDisplayed() {
        shortPostcodeErrorDisplayed();
    }

    @Then("Your postcode should be between 5 and 7 characters Field error should be displayed")
    public void shortPostcodeFieldErrorMessageIsDisplayed() {
        shortPostcodeFieldErrorDisplayed();
    }

    @Then(
            "Your postcode should only include numbers and letters error message should be displayed in the Error summary")
    public void errorMessageForPostcodeWithSpecialCharIsDisplayed() {
        specialCharPostcodeErrorDisplayed();
    }

    @Then("Your postcode should only include numbers and letters Field error should be displayed")
    public void fieldErrorMessageForPostcodeWithSpecialCharIsDisplayed() {
        specialCharPostcodeFieldErrorDisplayed();
    }

    @Then(
            "Your postcode should include numbers and letters error message should be displayed in the Error summary")
    public void errorForPostcodeWithAlphaOrNumericCharIsDisplayed() {
        alphaOrNumericPostcodeErrorDisplayed();
    }

    @Then("Your postcode should include numbers and letters Field error should be displayed")
    public void fieldErrorPostcodeWithAlphaOrNumericCharIsDisplayed() {
        alphaOrNumericPostcodeFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid Postcode should be displayed in the Error summary")
    public void properErrorMessageForInvalidPostcodeIsDisplayed() {
        invalidPostcodeErrorDisplayed();
    }

    @Then("Field error message for invalid Postcode should be displayed")
    public void fieldErrorMessageForInvalidPostcodeIsDisplayed() {
        invalidPostcodeFieldErrorDisplayed();
    }

    @Then("Enter a UK postcode should be displayed in the Error summary")
    public void errorMessageForInternationalPostcodeIsDisplayed() {
        internationalPostcodeErrorDisplayed();
    }

    @Then("Enter a UK postcode Field error message should be displayed")
    public void fieldErrorMessageForInternationalPostcodeIsDisplayed() {
        internationalPostcodeFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid Last name should be displayed in the Error summary")
    public void properErrorMessageForInvalidLastNameIsDisplayed() {
        invalidLastNameErrorDisplayed();
    }

    @Then("Field error message for invalid Last name should be displayed")
    public void fieldErrorMessageForInvalidLastNameIsDisplayed() {
        invalidLastNameFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid First name should be displayed in the Error summary")
    public void properErrorMessageForInvalidFirstNameIsDisplayed() {
        invalidFirstNameErrorDisplayed();
    }

    @Then("Field error message for invalid First name should be displayed")
    public void fieldErrorMessageForInvalidFirstNameIsDisplayed() {
        invalidFirstNameFieldErrorDisplayed();
    }

    @Then("Proper error message for invalid Middle names should be displayed in the Error summary")
    public void properErrorMessageForInvalidMiddleNamesIsDisplayed() {
        invalidMiddleNamesErrorDisplayed();
    }

    @Then("Field error message for invalid Middle names should be displayed")
    public void fieldErrorMessageForInvalidMiddleNamesIsDisplayed() {
        invalidMiddleNamesFieldErrorDisplayed();
    }

    @Given("User enters invalid Driving Licence DVLA details")
    public void userInputsInvalidDrivingDetails() {
        userEntersInvalidDrivingDetails();
    }

    @Given("User enters invalid Driving Licence DVA details")
    public void userInputsInvalidDVADrivingDetails() {
        new DVAEnterYourDetailsExactlyPage().userEntersInvalidDVADrivingDetails();
    }

    @When("User Re-enters data as a {}")
    public void userReInputsDataAsADrivingLicenceSubject(
            DrivingLicenceSubject drivingLicenceSubject) {
        userReEntersDataAsADrivingLicenceSubject(drivingLicenceSubject);
    }

    @When("User Re-enters DVA data as a {}")
    public void userReInputsDataAsDVAADrivingLicenceSubject(
            DVADrivingLicenceSubject dvaDrivingLicenceSubject) {
        new DVAEnterYourDetailsExactlyPage()
                .userReEntersDataAsDVADrivingLicenceSubject(dvaDrivingLicenceSubject);
    }

    @Given("User click on ‘prove your identity another way' Link")
    public void userClickOnProveYourIdentityAnotherWayLink() {
        proveanotherway.click();
    }

    @When("User click on I do not have a UK driving licence radio button")
    public void selectIDoNotHaveAUKDrivingLicenceRadioButton() {
        clickOnIDoNotHaveAUKDrivingLicenceRadioButton();
    }

    @Then(
            "I should be on `Enter your details exactly as they appear on your UK driving licence` page")
    public void
            i_should_be_on_enter_your_details_exactly_as_they_appear_on_your_uk_driving_licence_page() {
        Assert.assertTrue(new EnterYourDetailsExactlyDVLAPage().drivingLicenceNumber.isDisplayed());
    }

    @Then(
            "I should be on DVA `Enter your details exactly as they appear on your UK driving licence` page")
    public void
            i_should_be_on_DVA_enter_your_details_exactly_as_they_appear_on_your_uk_driving_licence_page() {
        Assert.assertTrue(new DVAEnterYourDetailsExactlyPage().dvaLicenceNumber.isDisplayed());
    }

    @Then("I should be on `Who was your UK driving licence issued by` page")
    public void i_should_be_on_who_was_your_uk_driving_licence_issued_by_page() {
        Assert.assertTrue(new EnterYourDetailsExactlyDVLAPage().DVLALabel.isDisplayed());
    }

    @And("^JSON payload should contain ci (.*), validity score (.*) and strength score (.*)$")
    public void contraIndicatorInVerifiableCredential(
            String ci, String validityScore, String strengthScore) throws IOException {
        new FraudPageObject().ciInVC(ci);
        scoreIs(validityScore, strengthScore);
    }

    @And("^JSON payload should contain validity score (.*) and strength score (.*)$")
    public void scoresInVerifiableCredential(String validityScore, String strengthScore)
            throws IOException {
        scoreIs(validityScore, strengthScore);
    }

    @Given("User click on ‘Back' Link")
    public void userClickOnBackLink() {
        back.click();
    }

    @Then(
            "Check you have entered your date of birth correctly DVA error should be displayed in the Error summary")
    public void errorMessageForInvalidDVADOBIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().invalidDVADOBErrorDisplayed();
    }

    @Then(
            "Check you have entered your date of birth correctly DVA Field error message should be displayed")
    public void fieldErrorMessageForInvalidDVADOBIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().invalidDVADOBFieldErrorDisplayed();
    }

    @Then(
            "Your date of birth must be in the past DVA error should be displayed in the Error summary")
    public void errorMessageForFutureDVADOBIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().futureDVADOBErrorDisplayed();
    }

    @Then("Your date of birth must be in the past DVA Field error message should be displayed")
    public void fieldErrorForFutureDVADOBIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().futureDVADOBFieldErrorDisplayed();
    }

    @Then("Proper DVA error message for no or invalid Date of Birth should be displayed")
    public void properErrorMessageForNoDVADOBIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().noDVADOBErrorDisplayed();
    }

    @Then("Field DVA error message for no or invalid Date of Birth should be displayed")
    public void fieldErrorMessageForNoDVADOBIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().noDVADOBFieldErrorDisplayed();
    }

    @Then(
            "Proper DVA error message for invalid Issue date should be displayed in the Error summary")
    public void properDVAErrorForInvalidIssueDateIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().invalidDVAIssueDateErrorDisplayed();
    }

    @Then("Field DVA error message for invalid Issue date should be displayed")
    public void fieldDVAErrorForInvalidIssueDateIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().invalidDVAIssueDateFieldErrorDisplayed();
    }

    @Then(
            "The issue date must be in the past DVA error message should be displayed in the Error summary")
    public void errorMessageForFutureDVAIssueDateIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().futureIssueDateDVAErrorDisplayed();
    }

    @Then("The issue date must be in the past DVA Field error message should be displayed")
    public void fieldErrorMessageForFutureDVAIssueDateIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().futureIssueDateDVAFieldErrorDisplayed();
    }

    @Then(
            "Your licence number should be 8 characters long error message should be displayed in the Error summary")
    public void shortDVADrivingLicenceNumberErrorMessageIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().shortDVADrivingLicenceNumErrorDisplayed();
    }

    @Then("Your licence number should be 8 characters long Field error should be displayed")
    public void shortDVADrivingLicenceNumberFieldErrorIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().shortDVADrivingLicenceNumFieldErrorDisplayed();
    }

    @Then(
            "Your licence number should not include any symbols or spaces DVA error should be displayed in the Error summary")
    public void errorForDVADrivingLicenceNumWithSpecialCharIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().specialCharDrivingLicenceDVAErrorDisplayed();
    }

    @Then(
            "Your licence number should not include any symbols or spaces DVA Field error should be displayed")
    public void fieldErrorForDVADrivingLicenceNumWithSpecialCharIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().specialCharDrivingLicenceDVAFieldErrorDisplayed();
    }

    @Then(
            "Proper DVA error message for invalid Licence number should be displayed in the Error summary")
    public void properDVAErrorForInvalidDrivingLicenceIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().invalidDrivingLicenceDVAErrorDisplayed();
    }

    @Then("Field DVA error message for invalid Licence number should be displayed")
    public void fieldDVAErrorForInvalidDrivingLicenceIsDisplayed() {
        new DVAEnterYourDetailsExactlyPage().invalidDrivingLicenceFieldDVAErrorDisplayed();
    }

    @And("^JSON response should contain documentNumber (.*) same as given Driving Licence$")
    public void errorInJsonResponse(String documentNumber) throws IOException {
        new FraudPageObject().documentNumberInVC(documentNumber);
    }
}
