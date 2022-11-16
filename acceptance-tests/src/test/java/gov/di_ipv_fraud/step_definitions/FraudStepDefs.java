package gov.di_ipv_fraud.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.di_ipv_fraud.pages.FraudPageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static gov.di_ipv_fraud.pages.Headers.IPV_CORE_STUB;

public class FraudStepDefs extends FraudPageObject {

    @Given("I navigate to the IPV Core Stub")
    public void navigateToStub() {
        navigateToIPVCoreStub();
    }

    @And("^I click the Fraud CRI for the (.*) environment$")
    public void navigateToFraud(String environment) {
        navigateToFraudCRI(environment);
    }

    @Then("^I search for user number (.*) in the Experian table$")
    public void userSearch(String number) {
        searchForUATUser(number);
    }

    @And("^I navigate to the verifiable issuer to check for a (.*) response from experian$")
    public void navigateToVerifiableIssuer(String validOrInvalid) {
        navigateToResponse(validOrInvalid);
    }

    @Then("^I navigate to (.*) and assert I have been directed correctly$")
    public void experianOrPrivacyPolicy(String page) {
        whoWeCheckDetailsWith(page);
    }

    @When("^I search for user name (.*) in the Experian table$")
    public void userSearchByUserName(String username) {
        userSearchByName(username);
    }

    @Then("^I click on Go to Fraud CRI link$")
    public void navigateToFraudCRILink() {
        goTofraudCRILink();
    }

    @And("JSON payload should contain user's name")
    public void userNameInJsonPayload() throws JsonProcessingException {
        userNameInJsonResponse();
    }

    @And("^JSON response should contain error details and status code as (.*)$")
    public void errorInJsonResponse(String testStatusCode) throws JsonProcessingException {
        jsonErrorResponse(testStatusCode);
    }

    @And("^I navigate to the page (.*)$")
    public void navigateToPage(String page) {
        goToPage(page);
    }

    @When("^I check Continue button is enabled and click on the Continue button$")
    public void clickOnContinueButton() {
        clickContinue();
    }

    @Then("^I navigate to Verifiable Credentials page$")
    public void navigateToVerifiableCredentialsPage() {
        goToVerifiableCredentialsPage();
    }

    @And("^I check for a (.*) response from experian")
    public void navigateToVerifiableCredentials(String validOrInvalid) {
        goToResponse(validOrInvalid);
    }

    @When("^I click on Edit User link$")
    public void navigateToEditUserLink() {
        goToEditUserLink();
    }

    @And("^I clear the postcode$")
    public void i_clear_the_postcode() {
        removePostcode();
    }

    @And("^I click on Go to Fraud CRI link after Edit$")
    public void iClickOnGoToFraudCRILinkAfterEdit() {
        goTofraudCRILinkAfterEdit();
    }

    @And("^I enter (.*) in the House name field$")
    public void enterHouseName(String housename) {
        addHouseName(housename);
    }

    @And("^JSON payload should contain user's House name as (.*) and House number as (.*)$")
    public void userHouseNameAndNumberInJsonPayload(String testHouseName, String testHouseNumber)
            throws JsonProcessingException {
        userHouseNameAndNumber(testHouseName, testHouseNumber);
    }

    @And("^JSON payload should contain ci (.*) and score (.*)$")
    public void contraIndicatorInVerifiableCredential(String ci, String score) throws IOException {
        ciInVC(ci);
        identityScoreIs(score);
    }

    @And("^I clear existing House number$")
    public void clearExistingHouseNumber() {
        clearHouseNumber();
    }

    @And("^I enter (.*) in the House number field$")
    public void enterHouseNumber(String housenumber) {
        addHouseNumber(housenumber);
    }

    @And("^I clear existing first name$")
    public void clearUserFirstName() {
        clearFirstname();
    }

    @And("^I clear existing surname$")
    public void clearUserSurname() {
        clearSurname();
    }

    @And("^I enter name (.*)$")
    public void enterUsername(String name) {
        enterName(name);
    }

    @And("^I clear existing Date of Birth$")
    public void clearExistingDoB() {
        clearDoB();
    }

    @And("^I submit user updates$")
    public void submitUserUpdates() {
        clickSubmit();
    }

    @And("^I am on (.*) page$")
    public void navigateToPageWithTitle(String title) {
        goToPageWithTitle(title);
    }

    @Then("Validate User navigation back to core for invalid users")
    public void validate_user_navigation_back_to_core_for_invalid_users() {
        waitForTextToAppear(IPV_CORE_STUB);
    }

    @When("^I click on Second address$")
    public void clickOnSecondAddress() {
        selectSecondAddressLink();
    }

    @And("^I enter Second address details$")
    public void enterSecondAddressDetails(List<Map<String, String>> secondAddressDetails) {
        addSecondAddressDetails(secondAddressDetails);
    }

    @And("^I enter valid to date as (.*)/(.*)/(.*)$")
    public void enterValidFromDate(String day, String month, String year) {
        addValidFromDate(day, month, year);
    }
}
