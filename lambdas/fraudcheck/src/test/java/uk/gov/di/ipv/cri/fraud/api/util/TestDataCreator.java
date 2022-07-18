package uk.gov.di.ipv.cri.fraud.api.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType.CURRENT;

public class TestDataCreator {

    private static final Logger LOGGER = LogManager.getLogger();

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
        return createTestPersonIdentity(CURRENT);
    }

    public static PersonIdentity createTestPersonIdentityMultipleAddresses(int totalAddresses) {
        PersonIdentity personIdentity = new PersonIdentity();

        personIdentity.setFirstName("FirstName");
        personIdentity.setMiddleNames("MiddleName");
        personIdentity.setSurname("Surname");

        personIdentity.setDateOfBirth(LocalDate.of(1976, 12, 26));

        List<Address> addresses = new ArrayList<>();
        IntStream.range(0, totalAddresses)
                .forEach(
                        a -> {
                            addresses.add(createAddress(a));
                        });

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

        LOGGER.info(
                "createAddress "
                        + id
                        + " "
                        + address.getAddressType()
                        + " from "
                        + address.getValidFrom()
                        + " until "
                        + address.getValidUntil());

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

    private static IdentityVerificationResponse createTestVerificationErrorResponse(
            ResponseType type) {
        if (type == ResponseType.INFO) {
            String errorMessage = "Info is not an Error response Type.";
            LOGGER.error(errorMessage);
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
}
