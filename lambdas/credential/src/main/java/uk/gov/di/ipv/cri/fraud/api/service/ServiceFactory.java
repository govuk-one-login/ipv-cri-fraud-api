package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.fraud.api.gateway.HmacGenerator;
import uk.gov.di.ipv.cri.fraud.api.gateway.IdentityVerificationRequestMapper;
import uk.gov.di.ipv.cri.fraud.api.gateway.IdentityVerificationResponseMapper;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.ContraindicationMappingItem;
import uk.gov.di.ipv.cri.fraud.library.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.ipv.cri.fraud.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.library.validation.InputValidationExecutor;

import javax.validation.Validation;
import javax.validation.Validator;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class ServiceFactory {
    private final IdentityVerificationService identityVerificationService;
    private final ContraindicationMapper contraindicationMapper;
    private final SSLContextFactory sslContextFactory;
    private final ConfigurationService configurationService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final HttpClient httpClient;

    @ExcludeFromGeneratedCoverageReport
    public ServiceFactory(ObjectMapper objectMapper)
            throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        this.objectMapper = objectMapper;
        this.configurationService =
                new ConfigurationService(
                        ParamManager.getSecretsProvider(),
                        ParamManager.getSsmProvider(),
                        System.getenv("ENVIRONMENT"));
        this.sslContextFactory =
                new SSLContextFactory(
                        this.configurationService.getEncodedKeyStore(),
                        this.configurationService.getKeyStorePassword());
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.contraindicationMapper =
                new ContraindicationMapper(
                        new DataStore<>(
                                this.configurationService.getContraindicationMappingTableName(),
                                ContraindicationMappingItem.class,
                                DataStore.getClient()),
                        this.configurationService.getThirdPartyId());
        this.httpClient = createHttpClient();
        this.identityVerificationService = createIdentityVerificationService();
    }

    ServiceFactory(
            ObjectMapper objectMapper,
            ConfigurationService configurationService,
            SSLContextFactory sslContextFactory,
            ContraindicationMapper contraindicationMapper,
            Validator validator,
            HttpClient httpClient)
            throws NoSuchAlgorithmException, InvalidKeyException {
        this.objectMapper = objectMapper;
        this.configurationService = configurationService;
        this.sslContextFactory = sslContextFactory;
        this.validator = validator;
        this.contraindicationMapper = contraindicationMapper;
        this.httpClient = httpClient;
        this.identityVerificationService = createIdentityVerificationService();
    }

    public IdentityVerificationService getIdentityVerificationService() {
        return this.identityVerificationService;
    }

    private IdentityVerificationService createIdentityVerificationService()
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

        return new IdentityVerificationService(
                thirdPartyGateway,
                new InputValidationExecutor(this.validator),
                this.contraindicationMapper);
    }

    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .sslContext(this.sslContextFactory.getSSLContext())
                .build();
    }
}
