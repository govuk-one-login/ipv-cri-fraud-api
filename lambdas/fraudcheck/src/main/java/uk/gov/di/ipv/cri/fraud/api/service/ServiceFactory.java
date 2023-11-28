package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.gateway.*;

import javax.net.ssl.SSLContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Clock;
import java.util.Base64;

public class ServiceFactory {
    private final IdentityVerificationService identityVerificationService;
    private final ContraindicationMapper contraindicationMapper;
    private final ConfigurationService configurationService;
    private final PersonIdentityValidator personIdentityValidator;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    private final EventProbe eventProbe;

    private static final int MAX_HTTP_RETRIES = 0;

    public ServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, HttpException {
        this.objectMapper = objectMapper;
        this.eventProbe = new EventProbe();
        this.personIdentityValidator = new PersonIdentityValidator();
        this.configurationService = createConfigurationService();
        this.contraindicationMapper = new ContraIndicatorRemoteMapper(configurationService);
        this.auditService = createAuditService(this.objectMapper);
        this.identityVerificationService = createIdentityVerificationService(configurationService);
    }

    @ExcludeFromGeneratedCoverageReport
    ServiceFactory(
            ObjectMapper objectMapper,
            EventProbe eventProbe,
            ConfigurationService configurationService,
            ContraindicationMapper contraindicationMapper,
            PersonIdentityValidator personIdentityValidator,
            AuditService auditService)
            throws NoSuchAlgorithmException, InvalidKeyException, HttpException {
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
        this.configurationService = configurationService;
        this.contraindicationMapper = contraindicationMapper;
        this.personIdentityValidator = personIdentityValidator;
        this.auditService = auditService;
        this.identityVerificationService = createIdentityVerificationService(configurationService);
    }

    private ConfigurationService createConfigurationService() {
        return new ConfigurationService(
                ParamManager.getSecretsProvider(),
                ParamManager.getSsmProvider(),
                System.getenv("ENVIRONMENT"));
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public IdentityVerificationService getIdentityVerificationService() {
        return this.identityVerificationService;
    }

    private IdentityVerificationService createIdentityVerificationService(
            ConfigurationService configurationService)
            throws NoSuchAlgorithmException, InvalidKeyException, HttpException {

        final CloseableHttpClient closeableHttpClient = generateHttpClient(configurationService);

        final HttpRetryer httpRetryer =
                new HttpRetryer(closeableHttpClient, eventProbe, MAX_HTTP_RETRIES);

        final ThirdPartyFraudGateway thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        httpRetryer,
                        new IdentityVerificationRequestMapper(
                                this.configurationService.getTenantId()),
                        new IdentityVerificationResponseMapper(eventProbe),
                        this.objectMapper,
                        new HmacGenerator(configurationService.getHmacKey()),
                        configurationService.getEndpointUrl(),
                        eventProbe);

        final ThirdPartyPepGateway thirdPartyPepGateway =
                new ThirdPartyPepGateway(
                        httpRetryer,
                        new IdentityVerificationRequestMapper(
                                this.configurationService.getTenantId()),
                        new IdentityVerificationResponseMapper(eventProbe),
                        this.objectMapper,
                        new HmacGenerator(configurationService.getHmacKey()),
                        configurationService.getEndpointUrl(),
                        eventProbe);

        final IdentityScoreCalculator identityScoreCalculator =
                new IdentityScoreCalculator(configurationService);

        final ActivityHistoryScoreCalculator activityHistoryScoreCalculator =
                new ActivityHistoryScoreCalculator();

        return new IdentityVerificationService(
                thirdPartyFraudGateway,
                thirdPartyPepGateway,
                personIdentityValidator,
                contraindicationMapper,
                identityScoreCalculator,
                activityHistoryScoreCalculator,
                auditService,
                configurationService,
                eventProbe);
    }

    private CloseableHttpClient generateHttpClient(ConfigurationService configurationService)
            throws HttpException {
        try {
            byte[] decodedKeyStore =
                    Base64.getDecoder().decode(configurationService.getEncodedKeyStore());

            ByteArrayInputStream decodedKeystoreAsBytes = new ByteArrayInputStream(decodedKeyStore);
            char[] keystorePassword = configurationService.getKeyStorePassword().toCharArray();

            KeyStore keystore = KeyStore.getInstance("pkcs12");
            keystore.load(decodedKeystoreAsBytes, keystorePassword);

            SSLContext sslContext =
                    SSLContexts.custom()
                            .loadKeyMaterial(keystore, keystorePassword)
                            .setProtocol("TLSv1.2")
                            .build();

            return HttpClients.custom().setSSLContext(sslContext).build();
        } catch (NoSuchAlgorithmException
                | KeyManagementException
                | KeyStoreException
                | UnrecoverableKeyException
                | IOException
                | CertificateException e) {
            throw new HttpException(e.getMessage());
        }
    }

    public AuditService getAuditService() {
        return auditService;
    }

    private AuditService createAuditService(ObjectMapper objectMapper) {
        var commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService();

        SqsClient sqsClient =
                SqsClient.builder()
                        .defaultsMode(DefaultsMode.STANDARD)
                        .httpClientBuilder(UrlConnectionHttpClient.builder())
                        .build();

        return new AuditService(
                sqsClient,
                commonLibConfigurationService,
                objectMapper,
                new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));
    }
}
