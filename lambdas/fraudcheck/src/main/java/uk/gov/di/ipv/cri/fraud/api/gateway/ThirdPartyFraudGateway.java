package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.domain.FraudCheckResult;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.IdentityVerificationResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class ThirdPartyFraudGateway {

    private static final Logger LOGGER = LogManager.getLogger();

    private final HttpClient httpClient;
    private final IdentityVerificationRequestMapper requestMapper;
    private final IdentityVerificationResponseMapper responseMapper;
    private final ObjectMapper objectMapper;
    private final HmacGenerator hmacGenerator;
    private final URI endpointUri;

    public static final String HTTP_300_REDIRECT_MESSAGE =
            "Redirection Message returned from Fraud Check Response, Status Code - ";
    public static final String HTTP_400_CLIENT_REQUEST_ERROR =
            "Client Request Error returned from Fraud Check Response, Status Code - ";
    public static final String HTTP_500_SERVER_ERROR =
            "Server Error returned from Fraud Check Response, Status Code - ";

    public static final String HTTP_UNHANDLED_ERROR =
            "Unhandled HTTP Response from Fraud Check Response, Status Code - ";

    public ThirdPartyFraudGateway(
            HttpClient httpClient,
            IdentityVerificationRequestMapper requestMapper,
            IdentityVerificationResponseMapper responseMapper,
            ObjectMapper objectMapper,
            HmacGenerator hmacGenerator,
            String endpointUrl) {
        Objects.requireNonNull(httpClient, "httpClient must not be null");
        Objects.requireNonNull(requestMapper, "requestMapper must not be null");
        Objects.requireNonNull(responseMapper, "responseMapper must not be null");
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        Objects.requireNonNull(hmacGenerator, "hmacGenerator must not be null");
        Objects.requireNonNull(endpointUrl, "endpointUri must not be null");
        if (StringUtils.isBlank(endpointUrl)) {
            throw new IllegalArgumentException("endpointUrl must be specified");
        }
        this.httpClient = httpClient;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
        this.objectMapper = objectMapper;
        this.hmacGenerator = hmacGenerator;
        this.endpointUri = URI.create(endpointUrl);
    }

    public FraudCheckResult performFraudCheck(PersonIdentity personIdentity)
            throws IOException, InterruptedException {
        LOGGER.info("Mapping person to third party verification request");
        IdentityVerificationRequest apiRequest = requestMapper.mapPersonIdentity(personIdentity);
        String requestBody = objectMapper.writeValueAsString(apiRequest);

        String requestBodyHmac = hmacGenerator.generateHmac(requestBody);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(endpointUri)
                        .setHeader("Accept", "application/json")
                        .setHeader("Content-Type", "application/json")
                        .setHeader("hmac-signature", requestBodyHmac)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

        LOGGER.info("Submitting fraud check request to third party...");
        HttpResponse<String> httpResponse =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        LOGGER.info("Third party response code {}", httpResponse.statusCode());

        int statusCode = httpResponse.statusCode();

        if (statusCode == 200) {
            String responseBody = httpResponse.body();
            IdentityVerificationResponse response =
                    objectMapper.readValue(responseBody, IdentityVerificationResponse.class);
            return responseMapper.mapIdentityVerificationResponse(response);
        } else {

            FraudCheckResult fraudCheckResult = new FraudCheckResult();
            fraudCheckResult.setExecutedSuccessfully(false);

            if (statusCode >= 300 && statusCode <= 399) {
                fraudCheckResult.setErrorMessage(HTTP_300_REDIRECT_MESSAGE + statusCode);
            } else if (statusCode >= 400 && statusCode <= 499) {
                fraudCheckResult.setErrorMessage(HTTP_400_CLIENT_REQUEST_ERROR + statusCode);
            } else if (statusCode >= 500 && statusCode <= 599) {
                fraudCheckResult.setErrorMessage(HTTP_500_SERVER_ERROR + statusCode);
            } else {
                fraudCheckResult.setErrorMessage(HTTP_UNHANDLED_ERROR + statusCode);
            }

            return fraudCheckResult;
        }
    }
}
