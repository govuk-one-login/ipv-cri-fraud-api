package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.domain.IdentityVerificationResult;
import uk.gov.di.ipv.cri.fraud.api.domain.ValidationResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationServiceTest {
    @Mock private ThirdPartyFraudGateway mockThirdPartyGateway;
    @Mock private PersonIdentityValidator personIdentityValidator;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private IdentityScoreCalaculator identityScoreCalaculator;
    @Mock private AuditService mockAuditService;

    private IdentityVerificationService identityVerificationService;

    @BeforeEach
    void setup() {
        this.identityVerificationService =
                new IdentityVerificationService(
                        mockThirdPartyGateway,
                        personIdentityValidator,
                        mockContraindicationMapper,
                        identityScoreCalaculator,
                        mockAuditService);
    }

    @Test
    void verifyIdentityShouldReturnResultWhenValidInputProvided()
            throws IOException, InterruptedException {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();
        FraudCheckResult testFraudCheckResult = new FraudCheckResult();
        testFraudCheckResult.setExecutedSuccessfully(true);
        String[] thirdPartyFraudCodes = new String[] {"sample-code"};
        String[] mappedFraudCodes = new String[] {"mapped-code"};
        testFraudCheckResult.setThirdPartyFraudCodes(thirdPartyFraudCodes);
        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(ValidationResult.createValidResult());
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity))
                .thenReturn(testFraudCheckResult);
        when(mockContraindicationMapper.mapThirdPartyFraudCodes(thirdPartyFraudCodes))
                .thenReturn(mappedFraudCodes);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(testPersonIdentity);

        assertNotNull(result);
        assertEquals(mappedFraudCodes[0], result.getContraIndicators()[0]);
        verify(personIdentityValidator).validate(testPersonIdentity);
        verify(mockThirdPartyGateway).performFraudCheck(testPersonIdentity);
        verify(mockContraindicationMapper).mapThirdPartyFraudCodes(thirdPartyFraudCodes);
    }

    @Test
    void verifyIdentityShouldReturnValidationErrorWhenInvalidInputProvided() {
        PersonIdentity testPersonIdentity = TestDataCreator.createTestPersonIdentity();
        List<String> validationErrors = List.of("validation error");
        when(personIdentityValidator.validate(testPersonIdentity))
                .thenReturn(new ValidationResult<>(false, validationErrors));

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(testPersonIdentity);

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
        when(mockThirdPartyGateway.performFraudCheck(testPersonIdentity)).thenReturn(null);

        IdentityVerificationResult result =
                this.identityVerificationService.verifyIdentity(testPersonIdentity);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(
                "Error occurred when attempting to invoke the third party api", result.getError());
    }
}
