package gov.di_ipv_fraud.step_definitions;

import gov.di_ipv_fraud.pages.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;

public class FraudAPIStepDefs extends FraudAPIPage {

    @Given(
            "user (.*) (.*) row number (.*) has the user identity in the form of a signed JWT string for CRI Id (.*)$")
    public void user_has_the_user_identity_in_the_form_of_a_signed_jwt_string(
            String GivenName, String FamilyName, String rowNumber, String criId)
            throws URISyntaxException, IOException, InterruptedException {
        userIdentityAsJwtStringForupdatedUser(GivenName, FamilyName, criId, rowNumber);
    }

    @And("user sends a POST request to session endpoint")
    public void user_sends_a_post_request_to_session_end_point()
            throws IOException, InterruptedException {
        postRequestToSessionEndpoint();
    }

    @And("user gets a session-id")
    public void user_gets_a_session_id() {
        getSessionId();
    }

    @When("user sends a POST request to Fraud endpoint")
    public void user_sends_a_post_request_to_fraud_end_point()
            throws IOException, InterruptedException {
        postRequestToFraudEndpoint();
    }

    @When("user sends a second POST request to Fraud endpoint")
    public void user_sends_a_second_post_request_to_fraud_end_point()
            throws IOException, InterruptedException {
        postRequestToFraudEndpoint();
    }

    @When("user sends a POST request to Fraud endpoint and the API returns the error (.*)$")
    public void user_sends_a_post_request_to_fraud_end_point(String response)
            throws IOException, InterruptedException {
        postRequestToFraudEndpointAndAPIReturnsResponseMatching(response);
    }

    @And("user gets authorisation code")
    public void user_gets_authorisation_code() throws IOException, InterruptedException {
        getAuthorisationCode();
    }

    @And("user sends a POST request to Access Token endpoint (.*)$")
    public void user_requests_access_token(String CRIId) throws IOException, InterruptedException {
        postRequestToAccessTokenEndpoint(CRIId);
    }

    @Then("user requests Fraud CRI VC")
    public void user_requests_vc() throws IOException, InterruptedException, ParseException {
        requestFraudCRIVC();
    }

    @And("VC should contain ci (.*) and identityFraudScore (.*)$")
    public void vc_should_contain_ci_and_identityFraudScore(String ci, Integer identityFraudScore)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        ciAndIdentityFraudScoreInVC(ci, identityFraudScore);
    }

    @And("VC is for person (.*) (.*)$")
    public void vc_is_for_user(String GivenName, String FamilyName)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        checkVCPersonDetails(GivenName, FamilyName);
    }

    @And("user changes (.*) in session request to (.*) for (.*)$")
    public void userChangesFieldsInTheSessionRequest(
            String fieldName, String fieldValue, String criId)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        updateSessionRequestFieldWithValue(fieldName, fieldValue, criId);
    }

    @And("VC should contain activityHistory score of (.*)$")
    public void vc_should_contain_ci_and_identityFraudScore(Integer activityHistoryScore)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        activityHistoryScoreInVC(activityHistoryScore);
    }

    @And("VC evidence checks should contain (.*)$")
    public void vc_should_contain_evidence_checks(String checksString)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        List<String> checks = List.of(checksString.split(","));
        evidenceChecksInVC(checks);
    }

    @And("VC evidence activity`history check contains activity from (.*)$")
    public void vc_should_contain_evidence_check_with_activity_from(String activityFrom)
            throws IOException, InterruptedException, ParseException, URISyntaxException {
        evidenceChecksInVC(List.of("activity_history_check"), activityFrom);
    }
}
