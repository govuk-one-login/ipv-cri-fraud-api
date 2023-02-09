package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class FraudAPIStepDefs extends FraudAPIPage {

    @Given("user has the user identity in the form of a signed JWT string for CRI Id (.*)$")
    public void user_has_the_user_identity_in_the_form_of_a_signed_jwt_string(String criId)
            throws URISyntaxException, IOException, InterruptedException {
        userIdentityAsJwtString(criId);
    }

    @When("user sends a POST request to session endpoint")
    public void user_sends_a_post_request_to_session_end_point()
            throws IOException, InterruptedException {
        postRequestToSessionEndpoint();
    }

    @Then("user gets a session-id")
    public void user_gets_a_session_id() {
        getSessionId();
    }

    @And("user sends a POST request to Fraud endpoint")
    public void user_sends_a_post_request_to_fraud_end_point()
            throws IOException, InterruptedException {
        postRequestToFraudEndpoint();
    }

    @And("user gets authorisation code")
    public void user_gets_authorisation_code() throws IOException, InterruptedException {
        getAuthorisationCode();
    }

    @And("user sends a POST request to Access Token endpoint (.*)$")
    public void user_requests_access_token(String CRIId) throws IOException, InterruptedException {
        postRequestToAccessTokenEndpoint(CRIId);
    }

    @And("user requests Fraud CRI VC")
    public void user_requests_vc() throws IOException, InterruptedException, ParseException {
        requestFraudCRIVC();
    }

}
