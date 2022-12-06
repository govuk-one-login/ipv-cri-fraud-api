package gov.di_ipv_fraud.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import gov.di_ipv_fraud.pages.DrivingLicencePageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DrivingLicenceStepDefs extends DrivingLicencePageObject {
    @And("^I click the Driving Licence CRI for the (.*) environment$")
    public void navigateToDrivingLicence(String environment) {
        navigateToDrivingLicenceCRI(environment);
    }

    @Then("^I search for Driving Licence user number (.*) in the Experian table$")
    public void i_search_for_DL_user_number(String number) {
        searchForUATUser(number);
    }

    @Then("I check the page title who was your UK driving license issued by?")
    public void i_check_the_page_title_who_was_your_uk_driving_license_issued_by() {
        validateDLPageTitle();
    }

    @And("I assert the URL is valid")
    public void i_assert_the_url_is_valid() {
        drivingLicencePageURLValidation();
    }

    @Given("I check the page title {string}")
    public void i_check_the_page_titled() {
        validateDLPageTitle();
    }

    @Given("I can see a radio button titled “DVLA”")
    public void i_can_see_a_radio_button_titled_dvla() {
        titleDVLAWithRadioBtn();
    }

    @Then("I can see a radio button titled “DVA”")
    public void i_can_see_a_radio_button_titled_dva() {
        titleDVAWithRadioBtn();
    }

    @And("I can see a radio button titled “I do not have a UK driving licence”")
    public void i_can_see_a_radio_button_titled_i_do_not_have_a_uk_driving_licence() {
        noDrivingLicenceBtn();
    }

    @Then("I can see CTA {string}")
    public void i_can_see_cta(String string) {
        ContinueButton();
    }

    @Given("I click on DVLA radio button and Continue")
    public void i_click_on_DVLA_radio_button_and_Continue() {
        clickOnDVLARadioButton();
    }

    @Then(
            "^I should on the page DVLA Enter your details exactly as they appear on your UK driving licence$")
    public void i_should_be_on_the_DVLA_page() {
        pageTitleDVLAValidation();
    }

    @Then(
            "^I should be on the page DVA Enter your details exactly as they appear on your UK driving licence$")
    public void i_should_be_on_the_DVA_page() {
        pageTitleDVAValidation();
    }

    @Given("I click on DVA radio button and Continue")
    public void i_select_dva_radio_button_and_Continue() {
        clickOnDVARadioButton();
    }

    @Given("I click I do not have UK Driving License and continue")
    public void i_select_i_do_not_have_uk_driving_license() {
        noDrivingLicenceOption();
    }

    @When("I am directed to the IPV Core routing page")
    public void i_am_directed_to_the_ipv_core_routing_page() {
        ipvCoreRoutingPage();
    }

    @Given("I have not selected anything and Continue")
    public void i_have_not_selected_anything() {
        noSelectContinue();
    }

    @When("I can see an error box highlighted red")
    public void i_can_see_an_error_box_highlighted_red() {
        errormessage();
    }

    @And("An error heading copy “You must choose an option to continue”")
    public void an_error_heading_copy_you_must_choose_an_option_to_continue() {
        errorTitle();
    }

    @Then("I can select a link which directs to the problem field")
    public void i_can_select_a_link_which_directs_to_the_problem_field() {
        errorLink();
    }

    @And("The field error copy “You must choose an option to continue”")
    public void the_field_error_copy_you_must_choose_an_option_to_continue() {
        validateErrorText();
    }

    @And("I validate the URL having access denied")
    public void iValidateTheURLHavingAccessDenied() {
        ipvCoreRoutingPageURL();
    }

    @Then("^I navigate to the Driving Licence verifiable issuer to check for a (.*) response$")
    public void i_navigate_to_driving_licence_verifiable_issuer_for_valid_response(
            String validOrInvalid) {
        navigateToDrivingLicenceResponse(validOrInvalid);
    }

    @And("^JSON response should contain error description (.*) and status code as (.*)$")
    public void errorInJsonResponse(String testErrorDescription, String testStatusCode)
            throws JsonProcessingException {
        jsonErrorResponse(testErrorDescription, testStatusCode);
    }
}
