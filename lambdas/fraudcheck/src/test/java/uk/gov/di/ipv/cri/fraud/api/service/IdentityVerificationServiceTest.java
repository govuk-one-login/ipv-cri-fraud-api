package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventContext;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.TPREFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.check.PepCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyPepGateway;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.exception.OAuthErrorResponseException;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.strategy.Strategy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IDENTITY_THEFT_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IMPERSONATION_RISK_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.MORTALITY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.SYNTHETIC_IDENTITY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.IDENTITY_CHECK_SCORE_PREFIX;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PEP_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PEP_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_PASS;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {

    @Mock private ServiceFactory mockServiceFactory;

    @Mock private ObjectMapper mockObjectMapper;
    @Mock private EventProbe mockEventProbe;
    @Mock private AuditService mockAuditService;

    @Mock private ThirdPartyAPIServiceFactory mockThirdPartyAPIServiceFactory;

    @Mock private TokenRequestService mockTokenRequestService;
    @Mock private ThirdPartyFraudGateway mockThirdPartyFraudGateway;
    @Mock private ThirdPartyPepGateway mockThirdPartyPepGateway;

    @Mock private PersonIdentityValidator personIdentityValidator;
    @Mock private ContraIndicatorMapper mockContraindicationMapper;
    @Mock private ActivityHistoryScoreCalculator mockActivityHistoryScoreCalculator;

    @Mock private FraudCheckConfigurationService mockFraudCheckConfigurationService;

    @Mock private SessionItem sessionItem;
    @Mock private Map<String, String> requestHeaders;

    private IdentityVerificationService identityVerificationService;

    private static final String TEST_ACCESS_TOKEN = "testTokenValue";

    @BeforeEach
    void setup() {
        // Tests rely on real IdentityScoreCalculator not mock
        when(mockFraudCheckConfigurationService.getZeroScoreUcodes())
                .thenReturn(List.of("zero-score-ucode"));

        when(mockServiceFactory.getObjectMapper()).thenReturn(mockObjectMapper);
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
        when(mockServiceFactory.getAuditService()).thenReturn(mockAuditService);

        when(mockThirdPartyAPIServiceFactory.getTokenRequestService())
                .thenReturn(mockTokenRequestService);
        when(mockThirdPartyAPIServiceFactory.getThirdPartyFraudGateway())
                .thenReturn(mockThirdPartyFraudGateway);
        when(mockThirdPartyAPIServiceFactory.getThirdPartyPepGateway())
                .thenReturn(mockThirdPartyPepGateway);

        this.identityVerificationService =
                new IdentityVerificationService(
                        mockServiceFactory,
                        mockThirdPartyAPIServiceFactory,
                        personIdentityValidator,
                        mockContraindicationMapper,
                        mockActivityHistoryScoreCalculator,
                        mockFraudCheckConfigurationService);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ipv-core-stub",
                "ipv-core-stub-aws-build",
                "ipv-core-stub-aws-prod",
                "ipv-core-stub-aws-build_3rdparty",
                "ipv-core-stub-aws-prod_3rdparty",
                "ipv-core-stub-pre-prod-aws-build",
                "ipv-core",
            })
    void
            verifyIdentityShouldReturnResultWhenValidInputProvidedDecisionScoreLessThanThresholdCrosscoreV2ForClient(
                    String clientId) throws IOException, SqsException, OAuthErrorResponseException {

        when(sessionItem.getClientId()).thenReturn(clientId);

        when(mockTokenRequestService.requestToken(eq(false), any(Strategy.class)))
                .thenReturn("testTokenValue");

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore(35);

        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockFraudCheckConfigurationService.getNoFileFoundThreshold(any(Strategy.class)))
                .thenReturn(35);

        when(mockThirdPartyFraudGateway.performFraudCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class)))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        when(mockActivityHistoryScoreCalculator.calculateActivityHistoryScore(null)).thenReturn(0);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);

        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));
        assertEquals(0, result.getActivityHistoryScore());
        assertEquals(
                LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE),
                result.getActivityFrom());

        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyFraudGateway)
                .performFraudCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class));
        verify(mockThirdPartyPepGateway, never())
                .performPepCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class));
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ipv-core-stub",
                "ipv-core-stub-aws-build",
                "ipv-core-stub-aws-prod",
                "ipv-core-stub-aws-build_3rdparty",
                "ipv-core-stub-aws-prod_3rdparty",
                "ipv-core-stub-pre-prod-aws-build",
                "ipv-core",
            })
    void verifyIdentityShouldReturnResultWhenValidInputProvidedCrosscoreV2ForClient(String clientId)
            throws IOException, SqsException, OAuthErrorResponseException {

        when(sessionItem.getClientId()).thenReturn(clientId);

        when(mockTokenRequestService.requestToken(eq(false), any(Strategy.class)))
                .thenReturn("testTokenValue");

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore(60);
        testFraudCheckResult.setOldestRecordDateInMonths(366);

        PepCheckResult testPEPCheckResult = new PepCheckResult();
        testPEPCheckResult.setExecutedSuccessfully(true);

        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        String[] thirdPartyPEPCodes = new String[] {"sample-p-code"};
        String[] mappedPEPCodes = new String[] {"mapped-p-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);
        testPEPCheckResult.setThirdPartyFraudCodes(thirdPartyPEPCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockThirdPartyFraudGateway.performFraudCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class)))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        when(mockThirdPartyPepGateway.performPepCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class)))
                .thenReturn(testPEPCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyPEPCodes))
                .thenReturn(mappedPEPCodes);

        when(mockActivityHistoryScoreCalculator.calculateActivityHistoryScore(366)).thenReturn(1);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);

        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);
        inOrder.verify(mockEventProbe).counterMetric(PEP_CHECK_REQUEST_SUCCEEDED);
        inOrder.verify(mockEventProbe).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 2);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));
        assertEquals(mappedPEPCodes[0], result.getContraIndicators().get(1));
        assertEquals(1, result.getActivityHistoryScore());
        assertEquals(
                LocalDate.now()
                        .minusMonths(366)
                        .withDayOfMonth(1)
                        .format(DateTimeFormatter.ISO_DATE),
                result.getActivityFrom());

        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyFraudGateway)
                .performFraudCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class));
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
        verify(mockThirdPartyPepGateway)
                .performPepCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class));
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyPEPCodes);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "ipv-core-stub",
                "ipv-core-stub-aws-build",
                "ipv-core-stub-aws-prod",
                "ipv-core-stub-aws-build_3rdparty",
                "ipv-core-stub-aws-prod_3rdparty",
                "ipv-core-stub-pre-prod-aws-build",
                "ipv-core",
            })
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFailsCrosscoreV2ForClient(String clientId)
            throws IOException, SqsException, OAuthErrorResponseException {

        when(sessionItem.getClientId()).thenReturn(clientId);

        when(mockTokenRequestService.requestToken(eq(false), any(Strategy.class)))
                .thenReturn("testTokenValue");

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();
        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockThirdPartyFraudGateway.performFraudCheck(
                        eq(testPersonIdentity), eq(TEST_ACCESS_TOKEN), any(Strategy.class)))
                .thenReturn(null);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 0);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 1);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 2);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_FAILED);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());
    }

    @ParameterizedTest
    @CsvSource({
        // NO_FILE_FOUND_THRESHOLD, EXPECTED_DECISION_SCORE, EXPECTED_IDENTITY_FRAUD_SCORE,
        // ZERO_SCORE_UCODE_PRESENT
        "35, 35, 1, false", // Low decision score
        "35, 35, 0, true", // Low decision score and Zero score uCode present
        "35, 90, 0, true", // High decision score but Zero score uCode present
        "35, 90, 2, false", // Fraud & Pep Ok - High decision score and Zero score uCode NOT present
    })
    void verifyScenarioOutcomesAreCorrectWhenNoRequestTechFailOrReturnErrorResponseCrosscoreV2(
            int noFileFoundThreshold,
            int expectedDecisionScore,
            int expectedIdentityFraudScore,
            boolean zeroScoreUCodePresent)
            throws IOException, SqsException, OAuthErrorResponseException {

        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
                .thenReturn("testTokenValue");
        when(sessionItem.getClientId()).thenReturn("testClientId");

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore(expectedDecisionScore);
        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);

        PepCheckResult testPEPCheckResult = new PepCheckResult();
        testPEPCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyPEPCodes = new String[] {"sample-p-code"};
        String[] mappedPEPCodes = new String[] {"mapped-p-code"};
        testPEPCheckResult.setThirdPartyFraudCodes(thirdPartyPEPCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockFraudCheckConfigurationService.getNoFileFoundThreshold(Strategy.NO_CHANGE))
                .thenReturn(noFileFoundThreshold);

        if (zeroScoreUCodePresent) {
            // Make the sample thirdPartyFraudCodes uCode a zero score one
            thirdPartyFraudCodes = new String[] {"zero-score-ucode"};
            testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);
        }

        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);
        when(mockThirdPartyFraudGateway.performFraudCheck(
                        testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                .thenReturn(testFraudCheckResult);

        // Pep performed scenarios - (score above threshold and no zeroScoreUCode found)
        if ((expectedDecisionScore > noFileFoundThreshold) && !zeroScoreUCodePresent) {
            when(mockThirdPartyPepGateway.performPepCheck(
                            testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                    .thenReturn(testPEPCheckResult);
            when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyPEPCodes))
                    .thenReturn(mappedPEPCodes);
        }

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        // Performed always for step 1 fraud check
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyFraudGateway)
                .performFraudCheck(testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);

        assertNotNull(result);
        assertTrue(result.isSuccess());

        // Fraud ucodes
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));

        if ((expectedDecisionScore > noFileFoundThreshold) && !zeroScoreUCodePresent) {
            // Performed for step 2 pep check after fraud has succeeded
            verify(mockThirdPartyPepGateway)
                    .performPepCheck(testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE);
            verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyPEPCodes);

            InOrder inOrder = inOrder(mockEventProbe);

            inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
            inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);
            inOrder.verify(mockEventProbe)
                    .counterMetric(PEP_CHECK_REQUEST_SUCCEEDED); // Check Pep was done
            inOrder.verify(mockEventProbe)
                    .counterMetric(IDENTITY_CHECK_SCORE_PREFIX + expectedIdentityFraudScore);

            // Pep ucodes are set
            assertEquals(mappedPEPCodes[0], result.getContraIndicators().get(1));

            // Checks for DecisionScore > 35 and Pep success
            assertAllChecksSucceed(result.getChecksSucceeded(), result.getChecksFailed());
        } else {
            // Expected when pep is not performed due to fraud check outcome
            InOrder inOrder = inOrder(mockEventProbe);

            inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
            inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);
            inOrder.verify(mockEventProbe, never())
                    .counterMetric(PEP_CHECK_REQUEST_SUCCEEDED); // Check pep not done (never())
            inOrder.verify(mockEventProbe)
                    .counterMetric(IDENTITY_CHECK_SCORE_PREFIX + expectedIdentityFraudScore);

            // Checks for DecisionScore <= 35/zeroScoreUCodePresent and Pep Not done
            assertAllFraudChecksFailAndPepNotPresent(
                    result.getChecksSucceeded(), result.getChecksFailed());
        }

        verify(mockAuditService)
                .sendAuditEvent(
                        eq(AuditEventType.RESPONSE_RECEIVED),
                        any(AuditEventContext.class),
                        eq(new TPREFraudAuditExtension(result.getThirdPartyFraudCodes())));

        assertEquals(expectedIdentityFraudScore, result.getIdentityCheckScore());
    }

    @Test
    void
            identityVerificationServiceShouldReturnErrorWhenFraudCheckFailsDueToNetworkErrorCrosscoreV2()
                    throws IOException, SqsException, OAuthErrorResponseException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(sessionItem.getClientId()).thenReturn("test_client_id");

        // No VC in this scenario
        when(mockThirdPartyFraudGateway.performFraudCheck(
                        testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                .thenThrow(new OAuthErrorResponseException(-1, ErrorResponse.FINAL_ERROR));

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 0);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 1);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 2);

        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_FAILED);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @Test
    void identityVerificationServiceShouldReturnErrorWhenFraudCheckNotPerformedCrosscoreV2()
            throws IOException, SqsException, OAuthErrorResponseException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(false); // FraudCheck Fail - No VC

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(sessionItem.getClientId()).thenReturn("testClientId");

        // No VC in this scenario
        when(mockThirdPartyFraudGateway.performFraudCheck(
                        testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                .thenReturn(testFraudCheckResult);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 0);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 1);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 2);

        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_FAILED);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @ParameterizedTest
    @CsvSource({
        "OAuthErrorResponseException", // Network Error during Pep Check
        "PEPExecutedSuccessfullyFalse" // Pep Check is not successfully (e.g. Error Response)
    })
    void verifyScenarioOutcomesAreCorrectWhenPEPCheckFailsDueToErrorCrosscoreV2(String errorType)
            throws IOException, SqsException, OAuthErrorResponseException {

        final int noFileFoundThreshold = 35;
        final int expectedDecisionScore = 90;
        final int expectedIdentityFraudScore =
                1; // Fraud Pass with Pep attempted but either network fail, sleep thread
        // interrupted or Error Response
        when(sessionItem.getClientId()).thenReturn("testClientId");

        when(mockTokenRequestService.requestToken(false, Strategy.NO_CHANGE))
                .thenReturn(TEST_ACCESS_TOKEN);

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore(expectedDecisionScore);
        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);
        testFraudCheckResult.setOldestRecordDateInMonths(366);

        PepCheckResult testPEPCheckResult = new PepCheckResult();
        testPEPCheckResult.setExecutedSuccessfully(
                !errorType.equals("PEPExecutedSuccessfullyFalse"));
        String[] thirdPartyPEPCodes = new String[] {"sample-p-code"};
        testPEPCheckResult.setThirdPartyFraudCodes(thirdPartyPEPCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockFraudCheckConfigurationService.getNoFileFoundThreshold(Strategy.NO_CHANGE))
                .thenReturn(noFileFoundThreshold);

        when(mockThirdPartyFraudGateway.performFraudCheck(
                        testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        when(mockActivityHistoryScoreCalculator.calculateActivityHistoryScore(366)).thenReturn(1);

        if (errorType.equals("OAuthErrorResponseException")) {
            when(mockThirdPartyPepGateway.performPepCheck(
                            testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                    .thenThrow(OAuthErrorResponseException.class);
        } else {
            when(mockThirdPartyPepGateway.performPepCheck(
                            testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE))
                    .thenReturn(testPEPCheckResult);
        }

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        // Performed always for step 1 fraud check
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyFraudGateway)
                .performFraudCheck(testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getActivityHistoryScore());
        assertEquals(
                LocalDate.now()
                        .minusMonths(366)
                        .withDayOfMonth(1)
                        .format(DateTimeFormatter.ISO_DATE),
                result.getActivityFrom());

        // Fraud ucodes
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));
        // Performed for step 2 pep check after fraud has succeeded
        verify(mockThirdPartyPepGateway)
                .performPepCheck(testPersonIdentity, TEST_ACCESS_TOKEN, Strategy.NO_CHANGE);

        // Checks for DecisionScore > 35 and Pep Fail
        InOrder inOrder = inOrder(mockEventProbe);

        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);
        inOrder.verify(mockEventProbe)
                .counterMetric(PEP_CHECK_REQUEST_FAILED); // Check Pep was done
        inOrder.verify(mockEventProbe)
                .counterMetric(IDENTITY_CHECK_SCORE_PREFIX + expectedIdentityFraudScore);

        assertTrue(result.getChecksSucceeded().size() > 0);
        assertEquals(1, result.getChecksFailed().size());

        assertTrue(result.getChecksSucceeded().contains(MORTALITY_CHECK.toString()));
        assertTrue(result.getChecksSucceeded().contains(IDENTITY_THEFT_CHECK.toString()));
        assertTrue(result.getChecksSucceeded().contains(SYNTHETIC_IDENTITY_CHECK.toString()));

        assertTrue(result.getChecksFailed().contains(IMPERSONATION_RISK_CHECK.toString()));

        assertEquals(expectedIdentityFraudScore, result.getIdentityCheckScore());
    }

    private void assertAllChecksSucceed(List<String> checksSucceeded, List<String> checksFailed) {

        // Checks Expected for DecisionScore > 35 and Pep success
        assertTrue(checksSucceeded.size() > 0);
        assertEquals(0, checksFailed.size());

        assertTrue(checksSucceeded.contains(MORTALITY_CHECK.toString()));
        assertTrue(checksSucceeded.contains(IDENTITY_THEFT_CHECK.toString()));
        assertTrue(checksSucceeded.contains(SYNTHETIC_IDENTITY_CHECK.toString()));
        assertTrue(checksSucceeded.contains(IMPERSONATION_RISK_CHECK.toString()));
    }

    private void assertAllFraudChecksFailAndPepNotPresent(
            List<String> checksSucceeded, List<String> checksFailed) {

        // Checks Expected for DecisionScore <= 35/zeroScoreUCodePresent and Pep Not done
        assertEquals(0, checksSucceeded.size());
        assertTrue(checksFailed.size() > 0);

        assertTrue(checksFailed.contains(MORTALITY_CHECK.toString()));
        assertTrue(checksFailed.contains(IDENTITY_THEFT_CHECK.toString()));
        assertTrue(checksFailed.contains(SYNTHETIC_IDENTITY_CHECK.toString()));

        // Not Present
        assertFalse(checksFailed.contains(IMPERSONATION_RISK_CHECK.toString()));
    }
}
