package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.fraud.library.metrics.Definitions.*;

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

        int identityScore = identityScoreCalculator.calculateIdentityScore(fraudCheckResult, true);
        assertEquals(identityScore, 2);
    }

    @Test
    void testSuccessInFraudAndSuccessInPepWithDecisionScoreBelow35IsScoreOfOne() {
        FraudCheckResult fraudCheckResult = new FraudCheckResult();
        fraudCheckResult.setDecisionScore("30");
        fraudCheckResult.setThirdPartyFraudCodes(new String[] {});
        fraudCheckResult.setExecutedSuccessfully(true);
        fraudCheckResult.setTransactionId("123456789");

        when(mockConfigurationService.getZeroScoreUcodes()).thenReturn(List.of("U001"));
        when(mockConfigurationService.getNoFileFoundThreshold()).thenReturn(35);
        this.identityScoreCalculator = new IdentityScoreCalculator(mockConfigurationService);

        int identityScore = identityScoreCalculator.calculateIdentityScore(fraudCheckResult, true);
        assertEquals(identityScore, 1);
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

        int identityScore = identityScoreCalculator.calculateIdentityScore(fraudCheckResult, true);
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

        int identityScore = identityScoreCalculator.calculateIdentityScore(fraudCheckResult, false);
        assertEquals(identityScore, 1);
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

        int identityScore = identityScoreCalculator.calculateIdentityScore(fraudCheckResult, false);
        assertEquals(identityScore, 0);
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
        int identityScore = identityScoreCalculator.calculateIdentityScore(fraudCheckResult, true);

        assertEquals(identityScore, 0);
    }
}
