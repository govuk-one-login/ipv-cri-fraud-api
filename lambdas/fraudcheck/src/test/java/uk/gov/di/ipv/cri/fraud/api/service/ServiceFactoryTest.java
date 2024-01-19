package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private static final String TEST_CROSSCOREV2_ENDPOINTURL = "testCrosscoreV2EndpointUrlValue";
    private static final String TEST_CROSSCOREV2_TENANTID = "testCrosscoreV2TenantIdValue";

    @Mock private ObjectMapper mockObjectMapper;
    @Mock private FraudCheckConfigurationService mockFraudCheckConfigurationService;
    @Mock private CrosscoreV2Configuration mockCrosscoreV2Configuration;
    @Mock private ContraindicationMapper mockContraindicationMapper;
    @Mock private PersonIdentityValidator mockPersonIdentityValidator;
    @Mock private HttpClient mockHttpClient;

    @Mock private AuditService mockAuditService;

    @Mock private EventProbe mockEventProbe;

    @Test
    void shouldCreateIdentityVerificationService()
            throws NoSuchAlgorithmException, InvalidKeyException, HttpException, KeyStoreException,
                    CertificateException, IOException {
        when(mockFraudCheckConfigurationService.crosscoreV2Enabled()).thenReturn(Boolean.FALSE);
        when(mockFraudCheckConfigurationService.getHmacKey()).thenReturn("hmac key");
        when(mockFraudCheckConfigurationService.getEndpointUrl())
                .thenReturn("https://test-endpoint");

        // Test needs a real keystore as it is auto mapped from strings
        final char[] unitTestKeyStorePassword = UUID.randomUUID().toString().toCharArray();
        String testKeyStore = generateUnitTestKeyStore(unitTestKeyStorePassword);
        String testKeyStorePassword = new String(unitTestKeyStorePassword);

        when(mockFraudCheckConfigurationService.getEncodedKeyStore()).thenReturn(testKeyStore);
        when(mockFraudCheckConfigurationService.getKeyStorePassword())
                .thenReturn(testKeyStorePassword);

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
        verify(mockFraudCheckConfigurationService, times(1)).getTenantId();
        verify(mockFraudCheckConfigurationService, times(2)).getHmacKey();
        verify(mockFraudCheckConfigurationService, times(2)).getEndpointUrl();
    }

    @Test
    void shouldCreateIdentityVerificationServiceWhenCrosscoreV2Enabled()
            throws NoSuchAlgorithmException, InvalidKeyException, HttpException, KeyStoreException,
                    CertificateException, IOException {
        when(mockFraudCheckConfigurationService.crosscoreV2Enabled()).thenReturn(Boolean.TRUE);
        when(mockFraudCheckConfigurationService.getHmacKey()).thenReturn("hmac key");
        when(mockFraudCheckConfigurationService.getEndpointUrl())
                .thenReturn("https://test-endpoint");

        // Test needs a real keystore as it is auto mapped from strings
        final char[] unitTestKeyStorePassword = UUID.randomUUID().toString().toCharArray();
        String testKeyStore = generateUnitTestKeyStore(unitTestKeyStorePassword);
        String testKeyStorePassword = new String(unitTestKeyStorePassword);

        when(mockFraudCheckConfigurationService.getEncodedKeyStore()).thenReturn(testKeyStore);
        when(mockFraudCheckConfigurationService.getKeyStorePassword())
                .thenReturn(testKeyStorePassword);

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
        verify(mockFraudCheckConfigurationService.getCrosscoreV2Configuration(), times(1))
                .getTenantId();
        verify(mockFraudCheckConfigurationService, times(2)).getHmacKey();
        verify(mockFraudCheckConfigurationService, times(2)).getEndpointUrl();
    }

    private String generateUnitTestKeyStore(char[] unitTestKeyStorePassword)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore unitTestkeyStore = KeyStore.getInstance("pkcs12");
        unitTestkeyStore.load(null, unitTestKeyStorePassword); // New KeyStore

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        unitTestkeyStore.store(bao, unitTestKeyStorePassword);

        byte[] base64 = Base64.getEncoder().encode(bao.toByteArray());

        return new String(base64);
    }
}
