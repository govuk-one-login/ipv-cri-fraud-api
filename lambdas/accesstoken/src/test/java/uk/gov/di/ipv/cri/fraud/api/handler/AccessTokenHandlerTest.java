package uk.gov.di.ipv.cri.fraud.api.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import uk.gov.di.ipv.cri.fraud.library.error.ErrorResponse;
import uk.gov.di.ipv.cri.fraud.library.persistence.DataStore;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.AccessTokenItem;
import uk.gov.di.ipv.cri.fraud.library.service.AccessTokenService;
import uk.gov.di.ipv.cri.fraud.library.service.ConfigurationService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AccessTokenHandlerTest {

    @Mock private DataStore<AccessTokenItem> mockDataStore;

    @Mock private ConfigurationService mockConfigurationService;

    @Test
    void handlerShouldProvideAnAccessToken() throws JsonProcessingException {
        AccessTokenHandler accessTokenHandler =
                new AccessTokenHandler(
                        new AccessTokenService(mockDataStore, mockConfigurationService));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setMultiValueHeaders(makeHeadersMap());
        event.setBody("grant_type=client_credentials&scope=fraud-check");
        event.setHttpMethod("POST");
        APIGatewayProxyResponseEvent response = accessTokenHandler.handleRequest(event, null);
        assertEquals(HttpStatusCode.OK, response.getStatusCode());
        Map map = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertEquals("fraud-check", map.get("scope"));
        assertEquals("Bearer", map.get("token_type"));
        assertNotNull(map.get("access_token"));
    }

    @Test
    void handlerShouldReturnA400ResponseOnValidationError() throws JsonProcessingException {
        AccessTokenHandler accessTokenHandler =
                new AccessTokenHandler(
                        new AccessTokenService(mockDataStore, mockConfigurationService));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setMultiValueHeaders(makeHeadersMap());
        event.setBody(
                "grant_type=client_credentials&grant_type=client_credentials&scope=fraud-check");
        event.setHttpMethod("POST");
        APIGatewayProxyResponseEvent response = accessTokenHandler.handleRequest(event, null);
        assertEquals(HttpStatusCode.BAD_REQUEST, response.getStatusCode());
        Map map = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertEquals(ErrorResponse.INVALID_TOKEN_REQUEST.getMessage(), map.get("message"));
    }

    @Test
    void handlerShouldReturnA500ResponseOnServerError() throws JsonProcessingException {

        doThrow(new RuntimeException()).when(mockDataStore).create(any(AccessTokenItem.class));
        AccessTokenHandler accessTokenHandler =
                new AccessTokenHandler(
                        new AccessTokenService(mockDataStore, mockConfigurationService));
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setMultiValueHeaders(makeHeadersMap());
        event.setBody("grant_type=client_credentials&scope=fraud-check");
        event.setHttpMethod("POST");
        APIGatewayProxyResponseEvent response = accessTokenHandler.handleRequest(event, null);
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map map = new ObjectMapper().readValue(response.getBody(), Map.class);
        assertEquals(ErrorResponse.SERVER_ERROR.getMessage(), map.get("message"));
    }

    private Map<String, List<String>> makeHeadersMap() {
        return Map.of(
                "Authorization",
                List.of(" Basic YWxhZGRpbjpvcGVuc2VzYW1l"),
                "Content-Type",
                List.of("application/x-www-form-urlencoded"));
    }
}
