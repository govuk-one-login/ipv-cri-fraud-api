package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.exception.SqsException;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_PASS;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private ThirdPartyFraudGateway mockThirdPartyGateway;
    @Mock private PersonIdentityValidator personIdentityValidator;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private AuditService mockAuditService;
    @Mock private SessionItem sessionItem;
    @Mock private Map<String, String> requestHeaders;
    @Mock private ConfigurationService mockConfigurationService;

    @Mock private EventProbe mockEventProbe;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setup() {
        // Tests rely on real IdentityScoreCalculator not mock
        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("zero-score-ucode"));

        this.identityVerificationService =
                new IdentityVerificationService(
                        mockThirdPartyGateway,
                        personIdentityValidator,
                        mockContraindicationMapper,
                        new IdentityScoreCalculator(mockConfigurationService),
                        mockAuditService,
                        mockConfigurationService,
                        mockEventProbe);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvidedDecisionScoreLessThanThreshold()
            throws IOException, InterruptedException, SqsException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore("35");

        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);

        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);

        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, false);
        verify(mockThirdPartyGateway, never()).performFraudCheck(testPersonIdentity, true);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws IOException, InterruptedException, SqsException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore("60");

        FraudCheckResult testPEPCheckResult = new FraudCheckResult();
        testPEPCheckResult.setExecutedSuccessfully(true);

        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        String[] thirdPartyPEPCodes = new String[] {"sample-p-code"};
        String[] mappedPEPCodes = new String[] {"mapped-p-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);
        testPEPCheckResult.setThirdPartyFraudCodes(thirdPartyPEPCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockConfigurationService.getPepEnabled()).thenReturn(Boolean.TRUE);

        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, true))
                .thenReturn(testPEPCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyPEPCodes))
                .thenReturn(mappedPEPCodes);

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

        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, false);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, true);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyPEPCodes);
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided()
            throws SqsException, JsonProcessingException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();
        List<String> validationErrors = List.of("validation error");
        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(new ValidationResult<>(false, validationErrors));

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_FAIL);
        inOrder.verify(mockEventProbe, never()).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);
        inOrder.verify(mockEventProbe, never()).counterMetric(PEP_CHECK_REQUEST_SUCCEEDED);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 0);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 1);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 2);

        assertNotNull(result);
        assertTrue(result.getContraIndicators().isEmpty());
        assertFalse(result.isSuccess());
        assertEquals(validationErrors.get(0), result.getValidationErrors().get(0));
    }

    @Test
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFails()
            throws IOException, InterruptedException, SqsException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();
        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false)).thenReturn(null);

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
    void verifyScenarioOutcomesAreCorrectWhenNoRequestTechFailOrReturnErrorResponse(
            int noFileFoundThreshold,
            int expectedDecisionScore,
            int expectedIdentityFraudScore,
            boolean zeroScoreUCodePresent)
            throws IOException, InterruptedException, SqsException {

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore(String.valueOf(expectedDecisionScore));
        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);

        FraudCheckResult testPEPCheckResult = new FraudCheckResult();
        testPEPCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyPEPCodes = new String[] {"sample-p-code"};
        String[] mappedPEPCodes = new String[] {"mapped-p-code"};
        testPEPCheckResult.setThirdPartyFraudCodes(thirdPartyPEPCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(noFileFoundThreshold);

        if (zeroScoreUCodePresent) {
            // Make the sample thirdPartyFraudCodes uCode a zero score one
            thirdPartyFraudCodes = new String[] {"zero-score-ucode"};
            testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);
        }

        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
                .thenReturn(testFraudCheckResult);

        // Pep performed scenarios - (score above threshold and no zeroScoreUCode found)
        if ((expectedDecisionScore > noFileFoundThreshold) && !zeroScoreUCodePresent) {
            // Pep is checked to be enabled
            when(mockConfigurationService.getPepEnabled()).thenReturn(Boolean.TRUE);

            when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, true))
                    .thenReturn(testPEPCheckResult);
            when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyPEPCodes))
                    .thenReturn(mappedPEPCodes);
        }

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        // Performed always for step 1 fraud check
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, false);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);

        assertNotNull(result);
        assertTrue(result.isSuccess());

        // Fraud ucodes
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));

        if ((expectedDecisionScore > noFileFoundThreshold) && !zeroScoreUCodePresent) {
            // Performed for step 2 pep check after fraud has succeeded
            verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, true);
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

        assertEquals(expectedIdentityFraudScore, result.getIdentityCheckScore());
    }

    @Test
    void identityVerificationServiceShouldReturnErrorWhenFraudCheckFailsDueToNetworkError()
            throws IOException, InterruptedException, SqsException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        // No VC in this scenario
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
                .thenThrow(IOException.class);

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
    void identityVerificationServiceShouldReturnErrorWhenFraudCheckNotPerformed()
            throws IOException, InterruptedException, SqsException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(false); // FraudCheck Fail - No VC

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        // No VC in this scenario
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
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
        "IOException", // Network Error during Pep Check
        "InterruptedException", // Sleep in exponential backoff was interrupted
        "PEPExecutedSuccessfullyFalse" // Pep Check is not successfully (e.g. Error Response)
    })
    void verifyScenarioOutcomesAreCorrectWhenPEPCheckFailsDueToError(String errorType)
            throws IOException, InterruptedException, SqsException {

        final int noFileFoundThreshold = 35;
        final int expectedDecisionScore = 90;
        final int expectedIdentityFraudScore =
                1; // Fraud Pass with Pep attempted but either network fail, sleep thread
        // interrupted or Error Response

        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        testFraudCheckResult.setDecisionScore(String.valueOf(expectedDecisionScore));
        String[] thirdPartyFraudCodes = new String[] {"sample-f-code"};
        String[] mappedFraudCodes = new String[] {"mapped-f-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);

        FraudCheckResult testPEPCheckResult = new FraudCheckResult();
        testPEPCheckResult.setExecutedSuccessfully(
                errorType.equals("PEPExecutedSuccessfullyFalse") ? false : true);
        String[] thirdPartyPEPCodes = new String[] {"sample-p-code"};
        String[] mappedPEPCodes = new String[] {"mapped-p-code"};
        testPEPCheckResult.setThirdPartyFraudCodes(thirdPartyPEPCodes);

        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());

        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(noFileFoundThreshold);

        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        // Pep is checked to be enabled
        when(mockConfigurationService.getPepEnabled()).thenReturn(Boolean.TRUE);

        if (errorType.equals("IOException")) {
            when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, true))
                    .thenThrow(IOException.class);
        } else if (errorType.equals("InterruptedException")) {
            when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, true))
                    .thenThrow(InterruptedException.class);
        } else {
            when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, true))
                    .thenReturn(testPEPCheckResult);
        }

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        // Performed always for step 1 fraud check
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, false);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);

        assertNotNull(result);
        assertTrue(result.isSuccess());

        // Fraud ucodes
        assertEquals(mappedFraudCodes[0], result.getContraIndicators().get(0));
        // Performed for step 2 pep check after fraud has succeeded
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, true);

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
