package uk.gov.di.ipv.cri.fraud.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.AccessTokenHeader;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.response.AccessTokenPayload;
import uk.gov.di.ipv.cri.fraud.api.service.CrosscoreV2Configuration;

import java.util.Base64;

public class AccessTokenValidator {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String UNMAPPED = "unmapped";

    private AccessTokenValidator() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static boolean isTokenValid(
            String accessToken,
            ObjectMapper objectMapper,
            CrosscoreV2Configuration crosscoreV2Configuration)
            throws JsonProcessingException {
        boolean isTokenValid = false;
        String alg = UNMAPPED;
        String sub = UNMAPPED;
        String iss = UNMAPPED;

        try {
            String[] chunks = accessToken.split("\\.");

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String header = new String(decoder.decode(chunks[0]));
            String payload = new String(decoder.decode(chunks[1]));

            AccessTokenHeader accessTokenHeader =
                    objectMapper.readValue(header, AccessTokenHeader.class);
            AccessTokenPayload accessTokenPayload =
                    objectMapper.readValue(payload, AccessTokenPayload.class);

            alg = accessTokenHeader.getAlgorithm();
            sub = accessTokenPayload.getUsername();
            iss = accessTokenPayload.getIssuer();
        } catch (JsonProcessingException e) {
            LOGGER.error("Invalid JWT returned for access token");
        }
        if (alg.equalsIgnoreCase("RS256")
                && sub.equalsIgnoreCase(crosscoreV2Configuration.getUsername())
                && iss.equalsIgnoreCase(crosscoreV2Configuration.getTokenIssuer())) {
            isTokenValid = true;
        } else {
            LOGGER.error(
                    "Token jwt contains invalid value/s. alg {}, sub {}, iss {}", alg, sub, iss);
        }

        return isTokenValid;
    }
}
