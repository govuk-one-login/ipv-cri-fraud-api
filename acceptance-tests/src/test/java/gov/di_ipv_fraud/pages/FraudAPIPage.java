package gov.di_ipv_fraud.pages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.SignedJWT;
import gov.di_ipv_fraud.model.AuthorisationResponse;
import gov.di_ipv_fraud.model.Check;
import gov.di_ipv_fraud.service.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.NamePart;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FraudAPIPage {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String CLIENT_ID;
    private static String SESSION_REQUEST_BODY;
    private static String SESSION_ID;
    private static String STATE;
    private static String AUTHCODE;
    private static String ACCESS_TOKEN;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int LindaDuffThirdPartyRowNumber = 6;

    private static String vcHeader;
    private static String vcBody;
    private static final String KID_PREFIX = "did:web:review-f.dev.account.gov.uk#";

    private final ConfigurationService configurationService =
            new ConfigurationService(System.getenv("ENVIRONMENT"));

    public String getAuthorisationJwtFromStub(String criId, String rowNumberString)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);
        if (coreStubUrl == null) {
            throw new IllegalArgumentException("Environment variable IPV_CORE_STUB_URL is not set");
        }

        int rowNumber;
        if (rowNumberString != null) {
            rowNumber = Integer.parseInt(rowNumberString);
        } else {
            rowNumber = LindaDuffThirdPartyRowNumber;
        }
        return getClaimsForUser(coreStubUrl, criId, rowNumber);
    }

    public void userIdentityAsJwtStringForupdatedUser(
            String givenName, String familyName, String criId, String rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);

        JsonNode jsonNode = getSessionJwtAsJson(criId, rowNumber);
        JsonNode nameArray = jsonNode.get("shared_claims").get("name");
        JsonNode firstItemInNameArray = nameArray.get(0);
        JsonNode namePartsNode = firstItemInNameArray.get("nameParts");
        JsonNode firstItemInNamePartsArray = namePartsNode.get(0);
        ((ObjectNode) firstItemInNamePartsArray).put("value", givenName);
        JsonNode secondItemInNamePartsArray = namePartsNode.get(1);
        ((ObjectNode) secondItemInNamePartsArray).put("value", familyName);
        String updatedJsonString = jsonNode.toString();
        LOGGER.info("updatedJsonString = " + updatedJsonString);
        SESSION_REQUEST_BODY = createRequest(coreStubUrl, criId, updatedJsonString);
        LOGGER.info("SESSION_REQUEST_BODY = " + SESSION_REQUEST_BODY);

        // Capture client id for using later in the auth request
        Map<String, String> deserialisedSessionResponse =
                objectMapper.readValue(SESSION_REQUEST_BODY, new TypeReference<>() {});
        CLIENT_ID = deserialisedSessionResponse.get("client_id");
        LOGGER.info("CLIENT_ID = " + CLIENT_ID);
    }

    public void updateSessionRequestFieldWithValue(
            String fieldName, String fieldValue, String criId)
            throws URISyntaxException, IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);

        JsonNode jsonNode = getSessionJwtAsJson(criId, null);

        if (fieldName.equals("lastName")) {
            JsonNode nameArray = jsonNode.get("shared_claims").get("name");
            JsonNode firstItemInNameArray = nameArray.get(0);
            JsonNode namePartsNode = firstItemInNameArray.get("nameParts");

            JsonNode secondItemInNamePartsArray = namePartsNode.get(1);
            ((ObjectNode) secondItemInNamePartsArray).put("value", fieldValue);
        }

        String updatedJsonString = jsonNode.toString();
        LOGGER.info("updatedJsonString = " + updatedJsonString);
        SESSION_REQUEST_BODY = createRequest(coreStubUrl, criId, updatedJsonString);
        LOGGER.info("SESSION_REQUEST_BODY = " + SESSION_REQUEST_BODY);
    }

    public void postRequestToSessionEndpoint() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        LOGGER.info("getPrivateAPIEndpoint() ==> " + privateApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/session"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("X-Forwarded-For", "123456789")
                        .POST(HttpRequest.BodyPublishers.ofString(SESSION_REQUEST_BODY))
                        .build();
        String sessionResponse = sendHttpRequest(request).body();
        LOGGER.info("sessionResponse = " + sessionResponse);
        Map<String, String> deserialisedResponse =
                objectMapper.readValue(sessionResponse, new TypeReference<>() {});
        SESSION_ID = deserialisedResponse.get("session_id");
        STATE = deserialisedResponse.get("state");
    }

    public void getSessionId() {
        LOGGER.info("SESSION_ID = " + SESSION_ID);
        assertTrue(StringUtils.isNotBlank(SESSION_ID));
    }

    public String postRequestToFraudEndpoint() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        HttpRequest.Builder baseHttpRequest =
                HttpRequest.newBuilder()
                        .uri(URI.create(privateApiGatewayUrl + "/identity-check"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session_id", SESSION_ID)
                        .POST(HttpRequest.BodyPublishers.ofString(""));

        HttpRequest request = baseHttpRequest.build();
        String fraudCheckResponse = sendHttpRequest(request).body();
        LOGGER.info("fraudCheckResponse = " + fraudCheckResponse);

        return fraudCheckResponse;
    }

    // Use this instead of the above to assert the response from the fraud check
    public String postRequestToFraudEndpointAndAPIReturnsResponseMatching(String expectedResponse)
            throws IOException, InterruptedException {
        String fraudCheckResponse = postRequestToFraudEndpoint();

        LOGGER.info("fraudCheckResponseMessage = " + fraudCheckResponse);

        return fraudCheckResponse;
    }

    public void getAuthorisationCode() throws IOException, InterruptedException {
        String privateApiGatewayUrl = configurationService.getPrivateAPIEndpoint();
        String coreStubUrl = configurationService.getCoreStubUrl(false);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        privateApiGatewayUrl
                                                + "/authorization?redirect_uri="
                                                + coreStubUrl
                                                + "/callback&state="
                                                + STATE
                                                + "&scope=openid&response_type=code&client_id="
                                                + CLIENT_ID))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("session-id", SESSION_ID)
                        .GET()
                        .build();
        String authCallResponse = sendHttpRequest(request).body();
        LOGGER.info("authCallResponse = " + authCallResponse);
        AuthorisationResponse deserialisedResponse =
                objectMapper.readValue(authCallResponse, AuthorisationResponse.class);
        AUTHCODE = deserialisedResponse.getAuthorizationCode().getValue();
        LOGGER.info("authorizationCode = " + AUTHCODE);
    }

    public void postRequestToAccessTokenEndpoint(String criId)
            throws IOException, InterruptedException {
        String accessTokenRequestBody = getAccessTokenRequest(criId);
        LOGGER.info("Access Token Request Body = " + accessTokenRequestBody);
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        LOGGER.info("getPublicAPIEndpoint() ==> " + publicApiGatewayUrl);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/token"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(accessTokenRequestBody))
                        .build();
        String accessTokenPostCallResponse = sendHttpRequest(request).body();
        LOGGER.info("accessTokenPostCallResponse = " + accessTokenPostCallResponse);
        Map<String, String> deserialisedResponse =
                objectMapper.readValue(accessTokenPostCallResponse, new TypeReference<>() {});
        ACCESS_TOKEN = deserialisedResponse.get("access_token");
    }

    public void requestFraudCRIVC() throws IOException, InterruptedException, ParseException {
        String publicApiGatewayUrl = configurationService.getPublicAPIEndpoint();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(publicApiGatewayUrl + "/credential/issue"))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();
        String requestFraudVCResponse = sendHttpRequest(request).body();
        LOGGER.info("requestFraudVCResponse = " + requestFraudVCResponse);
        SignedJWT signedJWT = SignedJWT.parse(requestFraudVCResponse);

        vcHeader = signedJWT.getHeader().toString();
        LOGGER.info("VC Header = {}", vcHeader);

        vcBody = signedJWT.getJWTClaimsSet().toString();
        LOGGER.info("VC Body = {}", vcBody);

        JSONObject jsonHeader;
        try {
            jsonHeader = new JSONObject(vcHeader);
        } catch (Exception e) {
            LOGGER.error("Failed to parse VC Header as JSON", e);
            throw new AssertionError("Failed to parse VC Header as JSON", e);
        }
        String[] expectedFields = {"kid", "typ", "alg"};
        for (String field : expectedFields) {
            Assert.assertTrue(
                    "Field '" + field + "' is missing in the VC Header", jsonHeader.has(field));
        }
        Assert.assertEquals(
                "The 'typ' field does not have the expected value",
                "JWT",
                jsonHeader.getString("typ"));
        Assert.assertEquals(
                "The 'alg' field does not have the expected value",
                "ES256",
                jsonHeader.getString("alg"));
        String kid = jsonHeader.getString("kid");
        Assert.assertTrue(
                "The 'kid' field does not start with the expected prefix",
                kid.startsWith(KID_PREFIX));
        String kidSuffix = kid.substring(KID_PREFIX.length());
        Assert.assertFalse("The 'kid' field suffix should not be empty", kidSuffix.isEmpty());
    }

    public void ciAndIdentityFraudScoreInVC(String ci, Integer identityFraudScore)
            throws URISyntaxException, IOException, InterruptedException, ParseException {
        JsonNode jsonNode = objectMapper.readTree((vcBody));
        JsonNode evidenceArray = jsonNode.get("vc").get("evidence");
        JsonNode firstItemInEvidenceArray = evidenceArray.get(0);
        LOGGER.info("firstItemInEvidenceArray = " + firstItemInEvidenceArray);
        JsonNode ciNode = firstItemInEvidenceArray.get("ci");
        if (ciNode.isArray() && ciNode.size() > 0) {
            JsonNode firstItemInCIArray = firstItemInEvidenceArray.get("ci").get(0);
            String actualCI = firstItemInCIArray.asText();
            LOGGER.info("actualCI = " + actualCI);
            Assert.assertEquals(ci, actualCI);
        } else {
            String actualCI = ciNode.asText();
            LOGGER.info("CI when Empty = " + actualCI);
            Assert.assertEquals(ci, actualCI);
        }

        Integer actualIdentityFraudScore =
                firstItemInEvidenceArray.get("identityFraudScore").asInt();
        LOGGER.info("actualIdentityFraudScore = " + actualIdentityFraudScore);
        Assert.assertEquals(identityFraudScore, actualIdentityFraudScore);
        assertNotNull(jsonNode.get("jti").asText());
    }

    public void activityHistoryScoreInVC(Integer activityHistoryScore)
            throws IOException, InterruptedException, ParseException {
        JsonNode jsonNode = objectMapper.readTree((vcBody));
        JsonNode evidenceArray = jsonNode.get("vc").get("evidence");
        JsonNode firstItemInEvidenceArray = evidenceArray.get(0);

        Integer actualActivityHistoryScore =
                firstItemInEvidenceArray.get("activityHistoryScore").asInt();
        LOGGER.info("activityHistoryScore = " + actualActivityHistoryScore);
        Assert.assertEquals(activityHistoryScore, actualActivityHistoryScore);
    }

    public void evidenceChecksInVC(List<String> evidenceChecks, String... activityFrom)
            throws IOException, InterruptedException, ParseException {
        JsonNode jsonNode = objectMapper.readTree((vcBody));
        JsonNode evidenceArray = jsonNode.get("vc").get("evidence");
        JsonNode firstItemInEvidenceArray = evidenceArray.get(0);

        JsonNode checkDetailsNode = firstItemInEvidenceArray.get("checkDetails");
        JsonNode failedCheckDetailsNode = firstItemInEvidenceArray.get("failedCheckDetails");

        int checkFound = 0;

        if (null != checkDetailsNode && checkDetailsNode.isArray()) {
            for (final JsonNode checkNode : checkDetailsNode) {
                ObjectMapper objectMapper = new ObjectMapper();
                Check check = objectMapper.convertValue(checkNode, Check.class);
                LOGGER.info("HELLO the check is " + objectMapper.writeValueAsString(check));
                for (String evidenceCheck : evidenceChecks) {
                    LOGGER.info("HELLO the evidence check is " + evidenceCheck);

                    if (null != check.getFraudCheck()
                            && check.getFraudCheck().equals(evidenceCheck)) {
                        LOGGER.info("Check " + evidenceCheck + " found");
                        checkFound++;
                    }
                    if (evidenceCheck.equals("activity_history_check")
                            && null != check.getIdentityCheckPolicy()
                            && check.getIdentityCheckPolicy().equals("none")
                            && check.getActivityFrom() != null) {
                        if (activityFrom != null && activityFrom.length > 0) {
                            assertEquals(activityFrom[0], check.getActivityFrom());
                        }
                        LOGGER.info("Activity history check found");
                        checkFound++;
                    }
                }
            }
        }
        Assert.assertEquals(evidenceChecks.size(), checkFound);

        // Ensure none of the above checks also appear in failedCheckDetails
        // Tests need updated to also provide the expected failedCheckDetails and assert contents
        if (null != failedCheckDetailsNode && failedCheckDetailsNode.isArray()) {
            for (final JsonNode checkNode : failedCheckDetailsNode) {
                Check failedCheck = objectMapper.convertValue(checkNode, Check.class);

                // activity history check has no fraud check field
                if (evidenceChecks.contains("activity_history_check")
                        && null == failedCheck.getFraudCheck()) {
                    LOGGER.warn("Activity history check found in failed checks");
                } else {
                    LOGGER.info("{} check found in failed checks", failedCheck.getFraudCheck());
                }

                assertFalse(evidenceChecks.contains(failedCheck.getFraudCheck()));
            }
        }
    }

    public void checkVCPersonDetails(String expectedGivenName, String expectedFamilyName)
            throws IOException {
        JsonNode jsonNode = objectMapper.readTree((vcBody));
        JsonNode vcNode = jsonNode.get("vc");
        JsonNode credentialSubjectNode = vcNode.get("credentialSubject");
        JsonNode namePartsNode = credentialSubjectNode.get("name").get(0).get("nameParts");
        LOGGER.info("namePartsNode = " + namePartsNode);

        List<NamePart> nameParts =
                objectMapper.readerForListOf(NamePart.class).readValue(namePartsNode);
        LOGGER.info(
                "nameParts = "
                        + nameParts.stream()
                                .map(NamePart::getValue)
                                .reduce(
                                        "",
                                        (givenName, familyName) -> givenName + " " + familyName));

        assertEquals(expectedGivenName, nameParts.get(0).getValue());
        assertEquals(expectedFamilyName, nameParts.get(1).getValue());
    }

    private String getClaimsForUser(String baseUrl, String criId, int userDataRowNumber)
            throws URISyntaxException, IOException, InterruptedException {

        var url =
                new URI(
                        baseUrl
                                + "/backend/generateInitialClaimsSet?cri="
                                + criId
                                + "&rowNumber="
                                + userDataRowNumber);

        LOGGER.info("URL =>> " + url);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(url)
                        .GET()
                        .setHeader(
                                "Authorization",
                                getBasicAuthenticationHeader(
                                        configurationService.getCoreStubUsername(),
                                        configurationService.getCoreStubPassword()))
                        .build();
        return sendHttpRequest(request).body();
    }

    private String createRequest(String baseUrl, String criId, String jsonString)
            throws URISyntaxException, IOException, InterruptedException {

        URI uri = new URI(baseUrl + "/backend/createSessionRequest?cri=" + criId);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(uri)
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader(
                                "Authorization",
                                getBasicAuthenticationHeader(
                                        configurationService.getCoreStubUsername(),
                                        configurationService.getCoreStubPassword()))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .build();

        return sendHttpRequest(request).body();
    }

    private HttpResponse<String> sendHttpRequest(HttpRequest request)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private String getAccessTokenRequest(String criId) throws IOException, InterruptedException {
        String coreStubUrl = configurationService.getCoreStubUrl(false);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        coreStubUrl
                                                + "/backend/createTokenRequestPrivateKeyJWT?authorization_code="
                                                + AUTHCODE
                                                + "&cri="
                                                + criId))
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader(
                                "Authorization",
                                getBasicAuthenticationHeader(
                                        configurationService.getCoreStubUsername(),
                                        configurationService.getCoreStubPassword()))
                        .GET()
                        .build();
        return sendHttpRequest(request).body();
    }

    private JsonNode getSessionJwtAsJson(String criId, String rowNumber)
            throws URISyntaxException, IOException, InterruptedException {
        String jsonString = getAuthorisationJwtFromStub(criId, rowNumber);
        LOGGER.info("jsonString = " + jsonString);
        return objectMapper.readTree((jsonString));
    }
}
