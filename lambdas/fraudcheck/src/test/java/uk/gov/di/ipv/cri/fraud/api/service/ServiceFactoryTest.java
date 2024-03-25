package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceFactoryTest {

    private static final String TEST_TOKEN_CLIENTID = "testTokenClientIdValue";
    private static final String TEST_TOKEN_ENDPOINT = "testTokenEndpointValue";
    private static final String TEST_TOKEN_CLIENT_SECRET = "testTokenClientSecretValue";
    private static final String TEST_TOKEN_USERNAME = "testTokenUsernameValue";
    private static final String TEST_TOKEN_PASSWORD = "testTokenPasswordValue";
    private static final String TEST_TOKEN_USER_DOMAIN = "testTokenUserDomainValue";
    private static final String TEST_TOKEN_TABLE_NAME = "testTokenTokenTableNameValue";

    @Mock private ObjectMapper mockObjectMapper;
    @Mock private FraudCheckConfigurationService mockFraudCheckConfigurationService;
    @Mock private CrosscoreV2Configuration mockCrosscoreV2Configuration;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private PersonIdentityValidator mockPersonIdentityValidator;

    @Mock private AuditService mockAuditService;

    @Mock private EventProbe mockEventProbe;

    @Test
    void shouldCreateIdentityVerificationService() throws HttpException {

        when(mockCrosscoreV2Configuration.getTokenEndpoint()).thenReturn(TEST_TOKEN_ENDPOINT);
        when(mockCrosscoreV2Configuration.getClientId()).thenReturn(TEST_TOKEN_CLIENTID);
        when(mockCrosscoreV2Configuration.getClientSecret()).thenReturn(TEST_TOKEN_CLIENT_SECRET);
        when(mockCrosscoreV2Configuration.getUsername()).thenReturn(TEST_TOKEN_USERNAME);
        when(mockCrosscoreV2Configuration.getPassword()).thenReturn(TEST_TOKEN_PASSWORD);
        when(mockCrosscoreV2Configuration.getUserDomain()).thenReturn(TEST_TOKEN_USER_DOMAIN);
        when(mockCrosscoreV2Configuration.getTokenTableName()).thenReturn(TEST_TOKEN_TABLE_NAME);

        when(mockFraudCheckConfigurationService.getCrosscoreV2Configuration())
                .thenReturn(mockCrosscoreV2Configuration);
        ServiceFactory serviceFactory =
                new ServiceFactory(
                        mockObjectMapper,
                        mockEventProbe,
                        mockFraudCheckConfigurationService,
                        mockContraindicationMapper,
                        mockPersonIdentityValidator,
                        mockAuditService);

        IdentityVerificationService identityVerificationService =
                serviceFactory.getIdentityVerificationService();

        assertNotNull(identityVerificationService);
    }
}
