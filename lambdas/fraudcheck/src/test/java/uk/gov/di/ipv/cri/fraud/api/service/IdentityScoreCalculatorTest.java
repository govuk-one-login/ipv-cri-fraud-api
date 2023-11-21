package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.domain.check.FraudCheckResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityScoreCalculatorTest {
    @Mock private ConfigurationService mockConfigurationService;

    private IdentityScoreCalculator identityScoreCalculator;

    @Test
    void testSuccessInFraudAndSuccessInPepIsScoreOfTwo() {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setDecisionScore("40");
        fraudCheckResult.setThirdPartyFraudCodes(new String[] {});
        fraudCheckResult.setExecutedSuccessfully(true);
        fraudCheckResult.setTransactionId("123456789");

        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("U001"));
        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);
        this.identityScoreCalculator = new IdentityScoreCalculator(mockConfigurationService);

        int identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterFraudCheck(
                        fraudCheckResult, true);
        identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterPEPCheck(identityScore, true);

        assertEquals(2, identityScore);
    }

    @Test
    void testSuccessInFraudAndSuccessInPepWithZeroScoreUcodeIsScoreOfZero() {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setDecisionScore("45");
        fraudCheckResult.setThirdPartyFraudCodes(new String[] {"U001"});
        fraudCheckResult.setExecutedSuccessfully(true);
        fraudCheckResult.setTransactionId("123456789");

        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("U001"));
        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);
        this.identityScoreCalculator = new IdentityScoreCalculator(mockConfigurationService);

        int identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterFraudCheck(
                        fraudCheckResult, true);
        identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterPEPCheck(identityScore, true);
        assertEquals(identityScore, 0);
    }

    @Test
    void testSuccessInFraudAndFailInPepIsScoreOfOne() {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setDecisionScore("45");
        fraudCheckResult.setThirdPartyFraudCodes(new String[] {});
        fraudCheckResult.setExecutedSuccessfully(true);
        fraudCheckResult.setTransactionId("123456789");

        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("U001"));
        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);
        this.identityScoreCalculator = new IdentityScoreCalculator(mockConfigurationService);

        int identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterFraudCheck(
                        fraudCheckResult, true);
        identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterPEPCheck(identityScore, false);
        assertEquals(1, identityScore);
    }

    @Test
    void testFailInFraudAndFailInPepIsScoreOfZero() {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setDecisionScore("45");
        fraudCheckResult.setThirdPartyFraudCodes(new String[] {});
        fraudCheckResult.setExecutedSuccessfully(false);
        fraudCheckResult.setTransactionId("123456789");

        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("U001"));
        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);
        this.identityScoreCalculator = new IdentityScoreCalculator(mockConfigurationService);

        int identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterFraudCheck(
                        fraudCheckResult, false);
        identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterPEPCheck(identityScore, false);
        assertEquals(0, identityScore);
    }

    @Test
    void testFailInFraudAndSuccessInPepIsScoreOfZero() {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setDecisionScore("45");
        fraudCheckResult.setThirdPartyFraudCodes(new String[] {});
        fraudCheckResult.setExecutedSuccessfully(false);
        fraudCheckResult.setTransactionId("123456789");

        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("U001"));
        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);
        this.identityScoreCalculator = new IdentityScoreCalculator(mockConfigurationService);

        int identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterFraudCheck(
                        fraudCheckResult, false);
        identityScore =
                identityScoreCalculator.calculateIdentityScoreAfterPEPCheck(identityScore, true);
        assertEquals(0, identityScore);
    }
}
