package uk.gov.di.ipv.cri.fraud.api.checkdetails;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.domain.checkdetails.Check;
import uk.gov.di.ipv.cri.fraud.library.domain.CheckType;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CheckTest {
    @Test
    void shouldCreateCheckFromCheckType() {

        final Check testCheck = new Check(CheckType.ACTIVITY_HISTORY_CHECK);
        testCheck.clearFraudCheckField();

        final int expectedNullCount = 0;
        int nullCount = 0;
        for (CheckType type : CheckType.values()) {
            Check check = assertDoesNotThrow(() -> new Check(type));

            assertNotNull(check);
            if (null != check.getFraudCheck()) {
                assertEquals(check.getFraudCheck(), type.toString().toLowerCase());

                if (type == CheckType.IMPERSONATION_RISK_CHECK) {
                    Check diffValuesCheck =
                            getValidDifferentValuesForCheckType(
                                    type, UUID.randomUUID().toString(), null, null);
                    boolean notEqualsSameCheckTypeDifferentValues = check.equals(diffValuesCheck);
                    assertFalse(notEqualsSameCheckTypeDifferentValues);
                }
            } else {
                check.clearFraudCheckField();

                // Activity History has null fraudCheck
                nullCount++;

                assertEquals(check.getFraudCheck(), testCheck.getFraudCheck());

                boolean equalsAH = check.equals(testCheck);
                assertTrue(equalsAH);

                Check diffValuesCheck =
                        getValidDifferentValuesForCheckType(
                                type, null, "identityCheckPolicy", LocalDate.now().toString());
                boolean notEqualsSameCheckTypeDifferentValues = check.equals(diffValuesCheck);
                assertFalse(notEqualsSameCheckTypeDifferentValues);
            }

            // Check Equals override
            boolean equalsSameCheckTypeSameValues = check.equals(new Check(type));
            assertTrue(equalsSameCheckTypeSameValues);

            boolean neverEqualNull = check.equals(null);
            assertFalse(neverEqualNull);

            boolean equalsSelf = check.equals(check);
            assertTrue(equalsSelf);

            // Check Hash code override
            boolean equalsSameHash = check.hashCode() == new Check(type).hashCode();
            assertTrue(equalsSameHash);
        }
    }

    private Check getValidDifferentValuesForCheckType(
            CheckType checkType, String txn, String identityCheckPolicy, String activityFrom) {

        Check check = new Check(checkType);

        switch (checkType) {
            case MORTALITY_CHECK, IDENTITY_THEFT_CHECK, SYNTHETIC_IDENTITY_CHECK -> {
                // No Unique handling
            }
                // IPR check has the transaction recorded in the check result
            case IMPERSONATION_RISK_CHECK -> check.setTxn(txn);
            case ACTIVITY_HISTORY_CHECK -> {
                // For activity history the fraud check field is not present
                check.clearFraudCheckField();

                check.setActivityFrom(activityFrom);
                check.setIdentityCheckPolicy(identityCheckPolicy);
            }
        }

        return check;
    }
}
