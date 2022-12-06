package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.FraudPageObject;
import gov.di_ipv_fraud.pages.ProveYourIdentityFullJourneyPageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Map;

public class ProveYourIdentityFullJourneyStepDefs extends ProveYourIdentityFullJourneyPageObject {

    FraudPageObject fraudPage;

    @Given("I navigate to the Orchestrator Stub")
    public void goToOrchestratorStub() {
        navigateToOrchestratorStub();
    }

    @And("^I click on Full journey route and Continue$")
    public void clickOnFullJourneyRoute() {
        clickOnFullJourneyRouteButton();
        selectContinueButton();
    }

    @And("^I enter Passport Details$")
    public void enterPassportDetails(List<Map<String, String>> passportDetails) {
        addPassportDetails(passportDetails);
    }

    @And("^I enter Date of birth as (.*)$")
    public void enterDateOfBirth(String date) {
        String[] dateParts = date.split("/");
        addDateOfBirth(dateParts[0], dateParts[1], dateParts[2]);
    }

    @And("^I enter Passport Expiry date as (.*)/(.*)/(.*) and Continue$")
    public void enterPassportExpiryDate(String day, String month, String year) {
        addPassportExpiryDate(day, month, year);
    }

    @And("^I enter (.*) in the Postcode field and find address$")
    public void enterPostcode(String postcode) {
        addPostcode(postcode);
    }

    @When("the user chooses their address (.*) from dropdown and click `Choose address`$")
    public void the_user_chooses_their_address_from_dropdown_and_click_Choose_address(
            String address) {
        selectAddressFromDropdown(address);
    }

    @When("the user enters the date (.*) they moved into their current address$")
    public void the_user_enters_the_date_they_moved_into_their_current_address(String expiryDate) {
        enterAddressExpiry(expiryDate);
    }

    @When("the user clicks `I confirm my details are correct`")
    public void the_user_clicks_I_confirm_my_details_are_correct() {
        confirmClick();
    }

    @When("the user clicks `Answer security questions`")
    public void the_user_clicks_Answe_security_questions() {
        confirmClick();
    }

    @When("kenneth answers the (.*) question correctly$")
    public void the_user_answers_the_first_question_correctly(String questionNumber)
            throws Exception {
        answerKBVQuestion();
    }

    @Then("verify the users address credentials. current address (.*)$")
    public void credentials_are_verified_against_input_address(String currentAddress)
            throws Exception {
        validateAddressVc(currentAddress);
    }

    @Then("verify the users fraud credentials")
    public void credentials_are_verified_against_input_fraud() throws Exception {
        validateFraudVc();
    }

    @Then(
            "they select `NO` for `Have you lived here for more than {int} months?` and click on `Continue`")
    public void they_select_NO_for_Have_you_lived_here_for_more_than_months_and_click_on_Continue(
            Integer int1) {
        selectNoFor3MonthsInAddress(int1);
    }

    @Then("they should be on `What is your previous home address?`")
    public void they_should_be_on_What_is_your_previous_home_address() {
        assertPreviousAddressTitle();
    }

    @And("^I click on `Continue to prove your identity this way` radio button$")
    public void clickContinueToProveYourIdentityRadioButton() {
        clickContinueToProveYourIdRadioBtn();
    }
}
