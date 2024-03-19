package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.gateway.*;
import uk.gov.di.ipv.cri.fraud.library.config.HttpRequestConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private final FraudCheckConfigurationService fraudCheckConfigurationService;
    private final PersonIdentityValidator personIdentityValidator;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    private final EventProbe eventProbe;

    private static final int MAX_HTTP_RETRIES = 0;

    public ServiceFactory(ObjectMapper objectMapper) throws HttpException {
        this.objectMapper = objectMapper;
        this.eventProbe = new EventProbe();
        this.personIdentityValidator = new PersonIdentityValidator();
        this.fraudCheckConfigurationService = createFraudCheckConfigurationService();
        this.contraindicationMapper =
                new ContraIndicatorRemoteMapper(fraudCheckConfigurationService);
        this.auditService = createAuditService(this.objectMapper);
        this.identityVerificationService =
                createIdentityVerificationService(fraudCheckConfigurationService);
    }

    @ExcludeFromGeneratedCoverageReport
    ServiceFactory(
            ObjectMapper objectMapper,
            EventProbe eventProbe,
            FraudCheckConfigurationService fraudCheckConfigurationService,
            ContraindicationMapper contraindicationMapper,
            PersonIdentityValidator personIdentityValidator,
            AuditService auditService)
            throws HttpException {
        this.objectMapper = objectMapper;
        this.eventProbe = eventProbe;
        this.fraudCheckConfigurationService = fraudCheckConfigurationService;
        this.contraindicationMapper = contraindicationMapper;
        this.personIdentityValidator = personIdentityValidator;
        this.auditService = auditService;
        this.identityVerificationService =
                createIdentityVerificationService(fraudCheckConfigurationService);
    }

    private FraudCheckConfigurationService createFraudCheckConfigurationService() {
        return new FraudCheckConfigurationService(
                ParamManager.getSecretsProvider(),
                ParamManager.getSsmProvider(),
                System.getenv("ENVIRONMENT"));
    }

    public FraudCheckConfigurationService getFraudCheckConfigurationService() {
        return fraudCheckConfigurationService;
    }

    public IdentityVerificationService getIdentityVerificationService() {
        return identityVerificationService;
    }

    private IdentityVerificationService createIdentityVerificationService(
            FraudCheckConfigurationService fraudConfigurationService) throws HttpException {

        final boolean useTlsKeystore = Boolean.parseBoolean(System.getenv("USE_TLS_KEYSTORE"));

        final CloseableHttpClient closeableHttpClient =
                generateHttpClient(fraudCheckConfigurationService, useTlsKeystore);

        final HttpRetryer httpRetryer =
                new HttpRetryer(closeableHttpClient, eventProbe, MAX_HTTP_RETRIES);

        final TokenRequestService tokenRequestService =
                new TokenRequestService(
                        fraudCheckConfigurationService.getCrosscoreV2Configuration(),
                        DataStore.getClient(),
                        httpRetryer,
                        HttpRequestConfig.getCustomRequestConfig(1000, 1000, 10000),
                        objectMapper,
                        eventProbe);

        final ThirdPartyFraudGateway thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        httpRetryer,
                        new IdentityVerificationRequestMapper(),
                        new IdentityVerificationResponseMapper(eventProbe, this.objectMapper),
                        this.objectMapper,
                        fraudConfigurationService,
                        eventProbe);

        final ThirdPartyPepGateway thirdPartyPepGateway =
                new ThirdPartyPepGateway(
                        httpRetryer,
                        new IdentityVerificationRequestMapper(),
                        new IdentityVerificationResponseMapper(eventProbe, this.objectMapper),
                        this.objectMapper,
                        fraudConfigurationService,
                        eventProbe);

        final IdentityScoreCalculator identityScoreCalculator =
                new IdentityScoreCalculator(fraudConfigurationService);

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
                fraudCheckConfigurationService,
                eventProbe,
                tokenRequestService);
    }

    private CloseableHttpClient generateHttpClient(
            FraudCheckConfigurationService fraudCheckConfigurationService, boolean useKeyStore)
            throws HttpException {
        try {
            SSLContextBuilder sslContextBuilder = SSLContexts.custom();

            if (useKeyStore) {
                byte[] decodedKeyStore =
                        Base64.getDecoder()
                                .decode(fraudCheckConfigurationService.getEncodedKeyStore());

                ByteArrayInputStream decodedKeystoreAsBytes =
                        new ByteArrayInputStream(decodedKeyStore);
                char[] keystorePassword =
                        fraudCheckConfigurationService.getKeyStorePassword().toCharArray();

                KeyStore keystore = KeyStore.getInstance("pkcs12");
                keystore.load(decodedKeystoreAsBytes, keystorePassword);

                sslContextBuilder.loadKeyMaterial(keystore, keystorePassword);
            }

            // Require TLSv1.2
            sslContextBuilder.setProtocol("TLSv1.2");

            return HttpClients.custom().setSSLContext(sslContextBuilder.build()).build();
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
