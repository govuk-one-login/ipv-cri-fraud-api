package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpException;
import org.apache.http.impl.client.CloseableHttpClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.fraud.api.gateway.IdentityVerificationRequestMapper;
import uk.gov.di.ipv.cri.fraud.api.gateway.IdentityVerificationResponseMapper;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyFraudGateway;
import uk.gov.di.ipv.cri.fraud.api.gateway.ThirdPartyPepGateway;
import uk.gov.di.ipv.cri.fraud.library.config.HttpRequestConfig;
import uk.gov.di.ipv.cri.fraud.library.service.HttpRetryer;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;

public class ThirdPartyAPIServiceFactory {

    private static final int MAX_HTTP_RETRIES = 0;

    private final TokenRequestService tokenRequestService;
    private final ThirdPartyFraudGateway thirdPartyFraudGateway;
    private final ThirdPartyPepGateway thirdPartyPepGateway;

    public ThirdPartyAPIServiceFactory(
            ServiceFactory serviceFactory,
            FraudCheckConfigurationService fraudCheckConfigurationService)
            throws HttpException {

        ObjectMapper objectMapper = serviceFactory.getObjectMapper();
        EventProbe eventProbe = serviceFactory.getEventProbe();

        ThirdPartyCloseableHttpClientFactory thirdPartyCloseableHttpClientFactory =
                new ThirdPartyCloseableHttpClientFactory();

        final CloseableHttpClient closeableHttpClient =
                thirdPartyCloseableHttpClientFactory.generateTLSHttpClient();

        final HttpRetryer httpRetryer =
                new HttpRetryer(closeableHttpClient, eventProbe, MAX_HTTP_RETRIES);

        DynamoDbEnhancedClient dynamoDbEnhancedClient =
                serviceFactory.getClientFactoryService().getDynamoDbEnhancedClient();

        tokenRequestService =
                new TokenRequestService(
                        fraudCheckConfigurationService.getCrosscoreV2Configuration(),
                        dynamoDbEnhancedClient,
                        httpRetryer,
                        HttpRequestConfig.getCustomRequestConfig(1000, 1000, 10000),
                        objectMapper,
                        eventProbe);

        thirdPartyFraudGateway =
                new ThirdPartyFraudGateway(
                        httpRetryer,
                        new IdentityVerificationRequestMapper(),
                        new IdentityVerificationResponseMapper(eventProbe, objectMapper),
                        objectMapper,
                        fraudCheckConfigurationService,
                        eventProbe);

        thirdPartyPepGateway =
                new ThirdPartyPepGateway(
                        httpRetryer,
                        new IdentityVerificationRequestMapper(),
                        new IdentityVerificationResponseMapper(eventProbe, objectMapper),
                        objectMapper,
                        fraudCheckConfigurationService,
                        eventProbe);
    }

    public TokenRequestService getTokenRequestService() {
        return tokenRequestService;
    }

    public ThirdPartyFraudGateway getThirdPartyFraudGateway() {
        return thirdPartyFraudGateway;
    }

    public ThirdPartyPepGateway getThirdPartyPepGateway() {
        return thirdPartyPepGateway;
    }
}
