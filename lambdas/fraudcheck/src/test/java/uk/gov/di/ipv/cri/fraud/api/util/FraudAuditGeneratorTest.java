package uk.gov.di.ipv.cri.fraud.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.common.library.domain.AuditEvent;
import uk.gov.di.ipv.cri.common.library.domain.AuditEventType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.TPREFraudAuditExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FraudAuditGeneratorTest {
    @Test
    void auditTest1() throws JsonProcessingException {

        PersonIdentity pi = TestDataCreator.createTestPersonIdentity();

        PersonIdentityDetailed pid =
                FraudPersonIdentityDetailedMapper.generatePersonIdentityDetailed(pi);

        AuditEventType evt1 = AuditEventType.REQUEST_SENT;
        AuditEvent<TPREFraudAuditExtension> ev1 =
                new AuditEvent<>(000001L, "PREFIX" + "_" + evt1.toString(), "TEST");

        // +Restricted Part -Ext
        ev1.setRestricted(pid);

        ObjectWriter ow =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .writer()
                        .withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(ev1);

        System.out.println(json);
        assertNotNull(json);
    }

    @Test
    void auditTest2() throws JsonProcessingException {

        TPREFraudAuditExtension tprefEXT = new TPREFraudAuditExtension(List.of("u101"));

        AuditEventType evt1 = AuditEventType.THIRD_PARTY_REQUEST_ENDED;
        AuditEvent<TPREFraudAuditExtension> ev1 =
                new AuditEvent<>(000001L, "PREFIX" + "_" + evt1.toString(), "TEST");

        // -Restricted Part +Ext
        ev1.setExtensions(tprefEXT);

        ObjectWriter ow =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .writer()
                        .withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(ev1);

        System.out.println(json);

        assertNotNull(json);
    }
}
