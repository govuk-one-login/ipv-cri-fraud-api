package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.PassportDetailsPageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PassportDetailsStepDefs extends PassportDetailsPageObject {

    @Given("A user is on Prove Your Identity page")
    public void navigateToProveIdentityPage() {
        proveIdentityPage();
    }

    @And(
            "A user enters {string}, {string}, {string}, {string}, {string}, {string}, {string},{string} and {string}")
    public void userEntersDetails(
            String passportNumber,
            String surname,
            String name,
            String birthDay,
            String birthMonth,
            String birthYear,
            String expiryDay,
            String expiryMonth,
            String expiryYear) {
        entersPassportDetails(
                passportNumber,
                surname,
                name,
                birthDay,
                birthMonth,
                birthYear,
                expiryDay,
                expiryMonth,
                expiryYear);
    }

    @Then("user sees DCS check is complete message")
    public void dcsCheckComplete() {
        dcsCompletionHeader();
    }

    @And(
            "A user enters the passport details {string}, {string}, {string}, {string}, {string}, {string}, {string},{string} and {string}")
    public void a_user_enters_the_passport_details_and(
            String passportNumber,
            String surname,
            String givenName,
            String birthDay,
            String birthMonth,
            String birthYear,
            String expiryDay,
            String expiryMonth,
            String expiryYear) {
        userEntersThePassportDetails(
                passportNumber,
                surname,
                givenName,
                birthDay,
                birthMonth,
                birthYear,
                expiryDay,
                expiryMonth,
                expiryYear);
    }

    @Then("I navigate to User for Passport CRI dev Page")
    public void i_navigate_to_user_for_passport_cri_dev_page() {
        navigateToPassportCRIDev();
    }

    @When("^I enter number (.*) and click Go to Passport CRI Dev button$")
    public void i_enter_number_and_click_go_to_passport_cri_dev_button(String number) {
        goToPassportCRIDevPage(number);
    }

    @And("^I validate the link Response from Passport CRI dev$")
    public void i_validate_the_link_response_from_passport_cri_dev() {
        responseFromPassportCRIDevlink();
    }

    @And("^I navigate to the verifiable issuer to check for valid response from Passport CRI dev$")
    public void navigateToVerifiableIssuer() {
        passportCRIDev();
    }
}
