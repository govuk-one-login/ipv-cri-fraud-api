package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.DLUnrecoverableErrorPageObject;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DLUnrecoverableErrorStepDefs extends DLUnrecoverableErrorPageObject {
    @When("I navigate to User for Driving Licence CRI dev Page")
    public void i_navigate_to_user_for_driving_licence_cri_dev_page() {
        DrivingLicenceCRIDev();
    }

    @And("I click Go to Driving Licence CRI dev button")
    public void i_click_go_to_driving_licence_cri_dev_button() {
        navigateToDrivingLicenceCRI();
    }

    @Given("I can see the relevant error page with correct title")
    public void i_can_see_the_relevant_error_page() {
        errorPageURLValidation();
    }

    @Then("^I can see the heading  Sorry, there is a error$")
    public void i_can_see_the_heading_page() {
        validateErrorPageHeading();
    }

    @Given("I delete the cookie to get the unexpected error")
    public void iDeleteTheCookieToGetTheUnexpectedError() {
        deletecookie();
    }
}
