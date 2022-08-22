package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.common.library.service.AuditEventFactory;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.fraud.api.gateway.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;

public class ServiceFactory {
    private final IdentityVerificationService identityVerificationService;
    private final ContraindicationMapper contraindicationMapper;
    private final SSLContextFactory sslContextFactory;
    private final ConfigurationService configurationService;
    private final PersonIdentityValidator personIdentityValidator;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AuditService auditService;

    public ServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        this.objectMapper = objectMapper;
        this.personIdentityValidator = new PersonIdentityValidator();
        this.configurationService = createConfigurationService();
        this.sslContextFactory =
                new SSLContextFactory(
                        this.configurationService.getEncodedKeyStore(),
                        this.configurationService.getKeyStorePassword());
        this.contraindicationMapper = new ContraIndicatorRemoteMapper(configurationService);
        this.httpClient = createHttpClient();
        this.auditService = createAuditService(this.objectMapper);
        this.identityVerificationService = createIdentityVerificationService(this.auditService);
    }

    @ExcludeFromGeneratedCoverageReport
    ServiceFactory(
            ObjectMapper objectMapper,
            ConfigurationService configurationService,
            SSLContextFactory sslContextFactory,
            ContraindicationMapper contraindicationMapper,
            PersonIdentityValidator personIdentityValidator,
            HttpClient httpClient,
            AuditService auditService)
            throws NoSuchAlgorithmException, InvalidKeyException {
        this.objectMapper = objectMapper;
        this.configurationService = configurationService;
        this.sslContextFactory = sslContextFactory;
        this.contraindicationMapper = contraindicationMapper;
        this.personIdentityValidator = personIdentityValidator;
        this.httpClient = httpClient;
        this.auditService = auditService;
        this.identityVerificationService = createIdentityVerificationService(this.auditService);
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

    private IdentityVerificationService createIdentityVerificationService(AuditService auditService)
            throws NoSuchAlgorithmException, InvalidKeyException {

        ThirdPartyFraudGateway thirdPartyGateway =
                new ThirdPartyFraudGateway(
                        this.httpClient,
                        new IdentityVerificationRequestMapper(
                                this.configurationService.getTenantId()),
                        new IdentityVerificationResponseMapper(),
                        this.objectMapper,
                        new HmacGenerator(this.configurationService.getHmacKey()),
                        this.configurationService.getEndpointUrl());

        final IdentityScoreCalculator identityScoreCalculator = new IdentityScoreCalculator();
        return new IdentityVerificationService(
                thirdPartyGateway,
                this.personIdentityValidator,
                this.contraindicationMapper,
                identityScoreCalculator,
                auditService,
                configurationService);
    }

    public AuditService getAuditService() {
        return auditService;
    }

    private AuditService createAuditService(ObjectMapper objectMapper) {
        var commonLibConfigurationService =
                new uk.gov.di.ipv.cri.common.library.service.ConfigurationService();
        return new AuditService(
                SqsClient.builder().build(),
                commonLibConfigurationService,
                objectMapper,
                new AuditEventFactory(commonLibConfigurationService, Clock.systemUTC()));
    }

    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .sslContext(this.sslContextFactory.getSSLContext())
                .build();
    }
}
