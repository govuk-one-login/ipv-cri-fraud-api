package uk.gov.di.ipv.cri.fraud.library.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class ServiceFactoryTest {

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Mock EventProbe mockEventProbe;
    @Mock ClientFactoryService mockClientFactoryService;
    @Mock ParameterStoreService mockParameterStoreService;
    @Mock SessionService mockSessionService;
    @Mock AuditService mockAuditService;
    @Mock ResultItemStorageService<FraudResultItem> mockResultItemStorageService;
    @Mock PersonIdentityService mockPersonIdentityService;
    @Mock ConfigurationService mockCommonLibConfigurationService;

    private ServiceFactory serviceFactory;

    @BeforeEach
    void setUp() {
        environmentVariables.set("AWS_REGION", "eu-west-2");
        environmentVariables.set("AWS_STACK_NAME", "TEST_STACK");

        serviceFactory =
                new ServiceFactory(
                        mockEventProbe,
                        mockClientFactoryService,
                        mockParameterStoreService,
                        mockSessionService,
                        mockAuditService,
                        mockResultItemStorageService,
                        mockPersonIdentityService,
                        mockCommonLibConfigurationService);
    }

    @Test
    void shouldReturnObjectMapper() {
        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        assertNotNull(objectMapper);

        ObjectMapper objectMapper2 = serviceFactory.getObjectMapper();

        assertEquals(objectMapper, objectMapper2);
    }

    @Test
    void shouldReturnEventProbe() {
        EventProbe eventProbe = serviceFactory.getEventProbe();
        assertNotNull(eventProbe);

        EventProbe eventProbe2 = serviceFactory.getEventProbe();
        assertEquals(eventProbe, eventProbe2);
    }

    @Test
    void shouldReturnClientFactoryService() {
        ClientFactoryService clientFactoryService = serviceFactory.getClientFactoryService();
        assertNotNull(clientFactoryService);

        ClientFactoryService clientFactoryService2 = serviceFactory.getClientFactoryService();
        assertEquals(clientFactoryService, clientFactoryService2);
    }

    @Test
    void shouldReturnParameterStoreService() {
        ParameterStoreService parameterStoreService1 = serviceFactory.getParameterStoreService();
        assertNotNull(parameterStoreService1);

        ParameterStoreService parameterStoreService2 = serviceFactory.getParameterStoreService();
        assertEquals(parameterStoreService1, parameterStoreService2);
    }

    @Test
    void shouldReturnCommonLibConfigurationService() {
        ConfigurationService commonLibConfigurationService1 =
                serviceFactory.getCommonLibConfigurationService();
        assertNotNull(commonLibConfigurationService1);

        ConfigurationService commonLibConfigurationService2 =
                serviceFactory.getCommonLibConfigurationService();
        assertEquals(commonLibConfigurationService1, commonLibConfigurationService2);
    }

    @Test
    void shouldReturnSessionService() {
        SessionService sessionService = serviceFactory.getSessionService();
        assertNotNull(sessionService);

        SessionService sessionService2 = serviceFactory.getSessionService();
        assertEquals(sessionService, sessionService2);

        assertEquals(sessionService, sessionService2);
    }

    @Test
    void shouldReturnAuditService() {

        AuditService auditService = serviceFactory.getAuditService();
        assertNotNull(auditService);

        AuditService auditService2 = serviceFactory.getAuditService();
        assertEquals(auditService, auditService2);
    }

    @Test
    void shouldReturnPersonIdentityService() {
        PersonIdentityService personIdentityService = serviceFactory.getPersonIdentityService();
        assertNotNull(personIdentityService);

        PersonIdentityService personIdentityService2 = serviceFactory.getPersonIdentityService();
        assertEquals(personIdentityService, personIdentityService2);
    }

    @Test
    void shouldReturnResultItemStorageService() {

        ResultItemStorageService<FraudResultItem> resultItemStorageService1 =
                serviceFactory.getResultItemStorageService();
        assertNotNull(resultItemStorageService1);

        ResultItemStorageService<FraudResultItem> resultItemStorageService2 =
                serviceFactory.getResultItemStorageService();
        assertEquals(resultItemStorageService1, resultItemStorageService2);
    }
}
