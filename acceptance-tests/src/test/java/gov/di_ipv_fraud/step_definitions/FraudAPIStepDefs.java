package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.net.URISyntaxException;

public class FraudAPIStepDefs extends FraudAPIPage {

    @Given("user has the user identity in the form of a signed JWT string for CRI Id (.*)$")
    public void user_has_the_user_identity_in_the_form_of_a_signed_jwt_string(String criId)
            throws URISyntaxException, IOException, InterruptedException {
        userIdentityAsJwtString(criId);
    }

    @When("user sends a POST request to session end point")
    public void user_sends_a_post_request_to_session_end_point()
            throws IOException, InterruptedException {
        postRequestToSessionEndpoint();
    }

    @Then("user gets a session-id")
    public void user_gets_a_session_id() {
        getSessionId();
    }
}
