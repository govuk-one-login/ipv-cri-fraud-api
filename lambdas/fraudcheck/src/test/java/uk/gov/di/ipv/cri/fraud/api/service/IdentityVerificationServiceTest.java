package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_REQUEST_FAILED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.FRAUD_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.IDENTITY_CHECK_SCORE_PREFIX;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PEP_CHECK_REQUEST_SUCCEEDED;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_FAIL;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.PERSON_DETAILS_VALIDATION_PASS;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private ThirdPartyFraudGateway mockThirdPartyGateway;
    @Mock private PersonIdentityValidator personIdentityValidator;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private IdentityScoreCalculator identityScoreCalculator;
    @Mock private AuditService mockAuditService;
    @Mock private SessionItem sessionItem;
    @Mock private Map<String, String> requestHeaders;
    @Mock private ConfigurationService mockConfigurationService;

    @Mock private EventProbe mockEventProbe;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setup() {
        this.identityVerificationService =
                new IdentityVerificationService(
                        mockThirdPartyGateway,
                        personIdentityValidator,
                        mockContraindicationMapper,
                        identityScoreCalculator,
                        mockAuditService,
                        mockConfigurationService,
                        mockEventProbe);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvidedDecisionScoreLessThanThreshold()
            throws IOException, InterruptedException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();

        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);

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

        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);

        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        when(identityScoreCalculator.calculateIdentityScore(testFraudCheckResult, false))
                .thenReturn(1);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);

        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_SUCCEEDED);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(mappedFraudCodes[0], result.getContraIndicators()[0]);
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, false);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
        verify(identityScoreCalculator).calculateIdentityScore(testFraudCheckResult, false);
        verify(identityScoreCalculator).calculateIdentityScore(testFraudCheckResult, false);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws IOException, InterruptedException {
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

        when(identityScoreCalculator.calculateIdentityScore(testFraudCheckResult, false))
                .thenReturn(1);
        when(identityScoreCalculator.calculateIdentityScore(
                        testFraudCheckResult, testPEPCheckResult.isExecutedSuccessfully()))
                .thenReturn(2);

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
        assertEquals(mappedPEPCodes[0], result.getContraIndicators()[0]);
        assertEquals(mappedFraudCodes[0], result.getContraIndicators()[1]);
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, false);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity, true);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyPEPCodes);
        verify(identityScoreCalculator).calculateIdentityScore(testFraudCheckResult, false);
        verify(identityScoreCalculator)
                .calculateIdentityScore(
                        testFraudCheckResult, testPEPCheckResult.isExecutedSuccessfully());
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided() {
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
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 1);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 2);

        assertNotNull(result);
        assertNull(result.getContraIndicators());
        assertFalse(result.isSuccess());
        assertEquals(validationErrors.get(0), result.getValidationErrors().get(0));
    }

    @Test
    void verifyIdentityShouldReturnErrorWhenThirdPartyCallFails()
            throws IOException, InterruptedException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();
        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity, false)).thenReturn(null);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(
                        testPersonIdentity, sessionItem, requestHeaders);

        InOrder inOrder = inOrder(mockEventProbe);
        inOrder.verify(mockEventProbe).counterMetric(PERSON_DETAILS_VALIDATION_PASS);
        inOrder.verify(mockEventProbe, never()).counterMetric(IDENTITY_CHECK_SCORE_PREFIX + 1);
        inOrder.verify(mockEventProbe).counterMetric(FRAUD_CHECK_REQUEST_FAILED);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());
    }
}
