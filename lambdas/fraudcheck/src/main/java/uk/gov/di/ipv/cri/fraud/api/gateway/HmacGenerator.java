package uk.gov.di.ipv.cri.fraud.api.gateway;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

public class HmacGenerator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final Mac sha256Hmac;

    public HmacGenerator(String hmacKey) throws NoSuchAlgorithmException, InvalidKeyException {
        if (StringUtils.isBlank(hmacKey)) {
            throw new IllegalArgumentException("hmacKey must be specified");
        }
        this.sha256Hmac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(hmacKey.getBytes(), HMAC_ALGORITHM);
        this.sha256Hmac.init(secretKey);
    }

    String generateHmac(String input) {
        Objects.requireNonNull(input, "The input must not be null");
        return Base64.getEncoder().encodeToString(sha256Hmac.doFinal(input.getBytes()));
    }
}
