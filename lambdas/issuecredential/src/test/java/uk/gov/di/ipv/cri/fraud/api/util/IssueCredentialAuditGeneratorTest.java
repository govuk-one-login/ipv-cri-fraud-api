package uk.gov.di.ipv.cri.fraud.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.common.library.domain.AuditEvent;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.VCISSFraudAuditExtension;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class IssueCredentialAuditGeneratorTest {

    @Test
    void auditTest1() throws JsonProcessingException {

        FraudResultItem fraudResultItem1 = new FraudResultItem();

        fraudResultItem1.setIdentityFraudScore(1);
        fraudResultItem1.setContraIndicators(List.of("u101"));
        fraudResultItem1.setSessionId(UUID.randomUUID());
        fraudResultItem1.setTransactionId("01");

        VCISSFraudAuditExtension ext =
                IssueCredentialFraudAuditExtensionUtil.generateVCISSFraudAuditExtension(
                        "TestIssuer", List.of(fraudResultItem1), true);

        AuditEventType evt1 = AuditEventType.VC_ISSUED;
        AuditEvent<VCISSFraudAuditExtension> ev1 =
                new AuditEvent<>(000001L, 00000001L, "PREFIX" + "_" + evt1.toString(), "TEST");

        // -Restricted +ext
        ev1.setExtensions(ext);

        ObjectWriter ow =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .writer()
                        .withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(ev1);

        assertNotNull(json);
    }
}
