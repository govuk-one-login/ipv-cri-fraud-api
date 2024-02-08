package uk.gov.di.ipv.cri.fraud.api.util;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class TestDataCreator {

    public static PersonIdentity createTestPersonIdentity(AddressType addressType) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));
        Address address = new Address();
        address.setValidFrom(LocalDate.now().minusYears(3));
        if (addressType.equals(AddressType.PREVIOUS)) {
            address.setValidUntil(LocalDate.now().minusMonths(1));
        }

        address.setBuildingNumber("101");
        address.setStreetName("Street Name");
        address.setAddressLocality("PostTown");
        address.setPostalCode("Postcode");
        address.setAddressCountry("GB");

        personIdentity.setAddresses(List.of(address));
        return personIdentity;
    }

    public static PersonIdentity createTestPersonIdentity() {
        return createTestPersonIdentity(AddressType.CURRENT);
    }

    public static PersonIdentity createTestPersonIdentityMultipleAddresses(
            int addressChainLength,
            int additionalCurrentAddresses,
            int additionalPreviousAddresses,
            boolean addressShuffle) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));

        List<Address> addresses = new ArrayList<>();
        IntStream.range(0, addressChainLength).forEach(a -> addresses.add(createAddress(a)));

        while (additionalCurrentAddresses > 0) {

            Address additionalCurrentAddress = new Address();
            additionalCurrentAddress.setValidUntil(null);
            additionalCurrentAddress.setValidFrom(
                    LocalDate.now().minusYears(additionalCurrentAddresses));

            addresses.add(additionalCurrentAddress);

            additionalCurrentAddresses--;
        }

        while (additionalPreviousAddresses > 0) {

            Address additionalPreviousAddress = new Address();
            additionalPreviousAddress.setValidUntil(
                    LocalDate.now().minusYears(additionalPreviousAddresses));
            additionalPreviousAddress.setValidFrom(null);

            addresses.add(additionalPreviousAddress);

            additionalPreviousAddresses--;
        }

        // Randomise list order
        if (addressShuffle) {
            Collections.shuffle(addresses);
        }

        personIdentity.setAddresses(addresses);

        return personIdentity;
    }

    public static IdentityVerificationResponse createTestVerificationResponse(ResponseType type) {
        switch (type) {
            case INFO:
                return createTestVerificationInfoResponse();
            case ERROR:
            case WARN:
            case WARNING:
                return createTestVerificationErrorResponse(type);
            default:
                throw new IllegalArgumentException("Unexpected response type encountered: " + type);
        }
    }

    public static PEPResponse createTestPEPResponse(ResponseType type) {
        switch (type) {
            case INFO:
                return createTestPEPInfoResponse();
            case ERROR:
            case WARN:
            case WARNING:
                return createTestPEPErrorResponse(type);
            default:
                throw new IllegalArgumentException("Unexpected response type encountered: " + type);
        }
    }

    private static Address createAddress(int id) {

        Address address = new Address();

        final int yearsBetweenAddresses = 2;

        int startYear = id + (id + yearsBetweenAddresses);
        int endYear = id + id;

        address.setValidFrom(LocalDate.now().minusYears(startYear));

        address.setValidUntil(
                (id == 0 ? null : LocalDate.now().minusYears(endYear).minusMonths(1)));

        address.setPostalCode("Postcode" + id);
        address.setStreetName("Street Name" + id);
        address.setAddressLocality("PostTown" + id);

        return address;
    }

    private static IdentityVerificationResponse createTestVerificationInfoResponse() {
        IdentityVerificationResponse testIVR = new IdentityVerificationResponse();

        ResponseHeader header = new ResponseHeader();
        header.setRequestType("TEST_INFO_RESPONSE");
        header.setClientReferenceId("1234567890abcdefghijklmnopqrstuvwxyz");
        header.setExpRequestId("1234");
        header.setMessageTime("2022-01-01T00:00:01Z");

        OverallResponse overallResponse = new OverallResponse();
        overallResponse.setDecision("OK");
        overallResponse.setDecisionText("OK");
        overallResponse.setDecisionReasons(List.of("NoReason"));
        overallResponse.setRecommendedNextActions(List.of(""));
        overallResponse.setSpareObjects(List.of(""));
        header.setOverallResponse(overallResponse);

        header.setResponseCode("1234");
        header.setResponseType(ResponseType.INFO);
        header.setResponseMessage("Text");
        header.setTenantID("1234");

        testIVR.setResponseHeader(header);

        ClientResponsePayload payload = new ClientResponsePayload();

        List<OrchestrationDecision> orchestrationDecisions = new ArrayList<>();
        OrchestrationDecision orchestrationDecision1 = new OrchestrationDecision();
        orchestrationDecision1.setSequenceId("1");
        orchestrationDecision1.setDecisionSource("Test");
        orchestrationDecision1.setDecision("OK");
        orchestrationDecision1.setDecisionReasons(List.of("Test"));
        orchestrationDecision1.setScore(0);
        orchestrationDecision1.setDecisionText("Test");
        orchestrationDecision1.setDecisionTime("2022-01-01T00:00:02Z");
        orchestrationDecision1.setNextAction("Continue");
        orchestrationDecision1.setAppReference("UNIT_TEST");
        orchestrationDecision1.setDecisionReasons(List.of("Test"));

        orchestrationDecisions.add(orchestrationDecision1);
        payload.setOrchestrationDecisions(orchestrationDecisions);

        List<DecisionElement> decisionElements = new ArrayList<>();
        DecisionElement decisionElement1 = new DecisionElement();
        decisionElement1.setApplicantId("APPLICANT_1");
        decisionElement1.setServiceName("Authenticateplus");
        decisionElement1.setDecision("AU01");
        decisionElement1.setScore(90);
        decisionElement1.setDecisionText("OK");
        decisionElement1.setDecisionReason("TEST");
        decisionElement1.setAppReference("UNIT_TEST");

        List<Rule> rules = new ArrayList<>();
        Rule rule1 = new Rule();
        rule1.setRuleName("AUTP_IDCONFLEVEL");
        rule1.setRuleId("");
        rule1.setRuleScore(1);
        rule1.setRuleText("Conf Level 1");

        rules.add(rule1);
        decisionElement1.setRules(rules);

        decisionElements.add(decisionElement1);
        payload.setDecisionElements(decisionElements);

        testIVR.setClientResponsePayload(payload);

        return testIVR;
    }

    private static PEPResponse createTestPEPInfoResponse() {
        PEPResponse testPEP = new PEPResponse();

        ResponseHeader header = new ResponseHeader();
        header.setRequestType("TEST_INFO_RESPONSE");
        header.setClientReferenceId("1234567890abcdefghijklmnopqrstuvwxyz");
        header.setExpRequestId("1234");
        header.setMessageTime("2022-01-01T00:00:01Z");

        OverallResponse overallResponse = new OverallResponse();
        overallResponse.setDecision("OK");
        overallResponse.setDecisionText("OK");
        overallResponse.setDecisionReasons(List.of("NoReason"));
        overallResponse.setRecommendedNextActions(List.of(""));
        overallResponse.setSpareObjects(List.of(""));
        header.setOverallResponse(overallResponse);

        header.setResponseCode("1234");
        header.setResponseType(ResponseType.INFO);
        header.setResponseMessage("Text");
        header.setTenantID("1234");

        testPEP.setResponseHeader(header);

        ClientResponsePayload payload = new ClientResponsePayload();

        List<OrchestrationDecision> orchestrationDecisions = new ArrayList<>();
        OrchestrationDecision orchestrationDecision1 = new OrchestrationDecision();
        orchestrationDecision1.setSequenceId("1");
        orchestrationDecision1.setDecisionSource("Test");
        orchestrationDecision1.setDecision("OK");
        orchestrationDecision1.setDecisionReasons(List.of("Test"));
        orchestrationDecision1.setScore(0);
        orchestrationDecision1.setDecisionText("Test");
        orchestrationDecision1.setDecisionTime("2022-01-01T00:00:02Z");
        orchestrationDecision1.setNextAction("Continue");
        orchestrationDecision1.setAppReference("UNIT_TEST");
        orchestrationDecision1.setDecisionReasons(List.of("Test"));

        orchestrationDecisions.add(orchestrationDecision1);
        payload.setOrchestrationDecisions(orchestrationDecisions);

        List<DecisionElement> decisionElements = new ArrayList<>();
        DecisionElement decisionElement1 = new DecisionElement();
        decisionElement1.setApplicantId("APPLICANT_1");
        decisionElement1.setServiceName("PepSanctions01");
        decisionElement1.setDecision("AU01");
        decisionElement1.setScore(90);
        decisionElement1.setDecisionText("OK");
        decisionElement1.setDecisionReason("TEST");
        decisionElement1.setAppReference("UNIT_TEST");

        List<Rule> rules = new ArrayList<>();
        Rule rule1 = new Rule();
        rule1.setRuleName("AUTP_IDCONFLEVEL");
        rule1.setRuleId("");
        rule1.setRuleScore(1);
        rule1.setRuleText("Conf Level 1");

        rules.add(rule1);
        decisionElement1.setRules(rules);

        decisionElements.add(decisionElement1);
        payload.setDecisionElements(decisionElements);

        testPEP.setClientResponsePayload(payload);

        return testPEP;
    }

    private static IdentityVerificationResponse createTestVerificationErrorResponse(
            ResponseType type) {
        if (type == ResponseType.INFO) {
            String errorMessage = "Info is not an Error response Type.";
            throw new IllegalArgumentException(errorMessage);
        }

        IdentityVerificationResponse testErrorResponse = new IdentityVerificationResponse();

        ResponseHeader header = new ResponseHeader();
        header.setRequestType("TEST_ERROR_RESPONSE");
        header.setClientReferenceId("1234567890abcdefghijklmnopqrstuvwxyz");
        header.setExpRequestId("1234");
        header.setMessageTime("2022-01-01T00:00:01Z");

        header.setResponseType(type);
        header.setResponseCode("E101");
        header.setResponseMessage("AnErrorOccured");

        testErrorResponse.setResponseHeader(header);

        return testErrorResponse;
    }

    private static PEPResponse createTestPEPErrorResponse(ResponseType type) {
        if (type == ResponseType.INFO) {
            String errorMessage = "Info is not an Error response Type.";
            throw new IllegalArgumentException(errorMessage);
        }

        PEPResponse testErrorResponse = new PEPResponse();

        ResponseHeader header = new ResponseHeader();
        header.setRequestType("TEST_ERROR_RESPONSE");
        header.setClientReferenceId("1234567890abcdefghijklmnopqrstuvwxyz");
        header.setExpRequestId("1234");
        header.setMessageTime("2022-01-01T00:00:01Z");

        header.setResponseType(type);
        header.setResponseCode("E101");
        header.setResponseMessage("AnErrorOccured");

        testErrorResponse.setResponseHeader(header);

        return testErrorResponse;
    }

    public static IdentityVerificationResponse getResponseScenario(String scenario)
            throws Exception {

        IdentityVerificationResponse identityVerificationResponse =
                TestDataCreator.createTestVerificationResponse(ResponseType.INFO);

        switch (scenario) {
            case "HappyPathInfo" -> {
                return identityVerificationResponse;
            }
            case "HappyPathError" -> {
                identityVerificationResponse =
                        TestDataCreator.createTestVerificationResponse(ResponseType.ERROR);

                return identityVerificationResponse;
            }
            case "Null Response" -> {
                return null;
            }
            case "Null ClientResponsePayload" -> identityVerificationResponse
                    .setClientResponsePayload(null);
            case "ClientResponsePayload null OrchestrationDecisions" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .setOrchestrationDecisions(null);
            case "ClientResponsePayload null OrchestrationDecisions Element" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getOrchestrationDecisions()
                    .set(0, null);
            case "Null DecisionElements" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .setDecisionElements(null);
            case "DecisionElements Null Element" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .set(0, null);
            case "DecisionElements DecisionElement null decision" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setDecision(null);
            case "DecisionElements DecisionElement null score" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setScore(null);
            case "DecisionElements DecisionElement null decisionReason" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setDecisionReason(null);
            case "Null Response Header" -> identityVerificationResponse.setResponseHeader(null);
            case "Null Response Header ResponseType" -> identityVerificationResponse
                    .getResponseHeader()
                    .setResponseType(null);
            case "Null Response Header RequestType" -> identityVerificationResponse
                    .getResponseHeader()
                    .setRequestType(null);
            case "Null warningsErrors" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setWarningsErrors(null);
            case "Empty warningsErrors" -> identityVerificationResponse
                    .getClientResponsePayload()
                    .getDecisionElements()
                    .get(0)
                    .setWarningsErrors(new ArrayList<>());
            case "WarningsErrors null warning" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, null);
            }
            case "Warning null responseType" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                WarningsErrors warningsErrors = new WarningsErrors();
                warningsErrors.setResponseType(null);
                warningsErrors.setResponseCode("ResponseCode");
                warningsErrors.setResponseMessage("ResponseMessage");

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, warningsErrors);
            }
            case "Warning null responseCode" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                WarningsErrors warningsErrors = new WarningsErrors();
                warningsErrors.setResponseType("ResponseType");
                warningsErrors.setResponseCode(null);
                warningsErrors.setResponseMessage("ResponseMessage");

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, warningsErrors);
            }
            case "Warning null responseMessage" -> {
                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .setWarningsErrors(new ArrayList<>());

                WarningsErrors warningsErrors = new WarningsErrors();
                warningsErrors.setResponseType("ResponseType");
                warningsErrors.setResponseCode("ResponseCode");
                warningsErrors.setResponseMessage(null);

                identityVerificationResponse
                        .getClientResponsePayload()
                        .getDecisionElements()
                        .get(0)
                        .getWarningsErrors()
                        .add(0, warningsErrors);
            }
            default -> throw new Exception("Invalid Test Scenario - " + scenario);
        }

        return identityVerificationResponse;
    }
}
