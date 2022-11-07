package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;

import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceFactoryTest {
    @Mock private ObjectMapper mockObjectMapper;
    @Mock private ConfigurationService mockConfigurationService;
    @Mock private SSLContextFactory mockSslContextFactory;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private PersonIdentityValidator mockPersonIdentityValidator;
    @Mock private HttpClient mockHttpClient;

    @Mock private AuditService mockAuditService;

    @Mock private EventProbe mockEventProbe;

    @Test
    void shouldCreateIdentityVerificationService()
            throws NoSuchAlgorithmException, InvalidKeyException {
        when(mockConfigurationService.getHmacKey()).thenReturn("hmac key");
        when(mockConfigurationService.getEndpointUrl()).thenReturn("https://test-endpoint");
        ServiceFactory serviceFactory =
                new ServiceFactory(
                        mockObjectMapper,
                        mockEventProbe,
                        mockConfigurationService,
                        mockSslContextFactory,
                        mockContraindicationMapper,
                        mockPersonIdentityValidator,
                        mockHttpClient,
                        mockAuditService);

        IdentityVerificationService identityVerificationService =
                serviceFactory.getIdentityVerificationService();

        assertNotNull(identityVerificationService);
        verify(mockConfigurationService).getTenantId();
        verify(mockConfigurationService).getHmacKey();
        verify(mockConfigurationService).getEndpointUrl();
    }
}
