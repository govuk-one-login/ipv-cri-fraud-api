package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.*;
import gov.di_ipv_fraud.utilities.BrowserUtils;
import gov.di_ipv_fraud.utilities.ConfigurationReader;
import gov.di_ipv_fraud.utilities.Driver;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class CommonSteps {

    @Given("I am on Orchestrator Stub")
    public void i_am_on_orchestrator_stub() {
        Driver.get().get(ConfigurationReader.getOrchestratorUrl());
        BrowserUtils.waitForPageToLoad(10);
    }

    @When("I click on Debug route")
    public void i_click_on_debug_route() {
        new OrchestratorStubPage().DebugRoute.click();
        BrowserUtils.waitForPageToLoad(10);
    }

    @Then("I should get five options")
    public void i_should_get_five_options() {
        Assert.assertTrue(new OrchestratorStubPage().UkDrivingLicence.isDisplayed());
    }
}
