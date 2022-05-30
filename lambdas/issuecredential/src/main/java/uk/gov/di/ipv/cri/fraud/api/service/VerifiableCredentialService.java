package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.KMSSigner;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.nimbusds.jwt.JWTClaimNames.EXPIRATION_TIME;
import static com.nimbusds.jwt.JWTClaimNames.ISSUER;
import static com.nimbusds.jwt.JWTClaimNames.NOT_BEFORE;
import static com.nimbusds.jwt.JWTClaimNames.SUBJECT;

public class VerifiableCredentialService {

    private final SignedJWTFactory signedJwtFactory;
    private final ConfigurationService configurationService;

    public VerifiableCredentialService() {
        this.configurationService = new ConfigurationService();
        this.signedJwtFactory =
                new SignedJWTFactory(
                        new KMSSigner(
                                configurationService.getVerifiableCredentialKmsSigningKeyId()));
    }

    public VerifiableCredentialService(
            SignedJWTFactory signedClaimSetJwt, ConfigurationService configurationService) {
        this.signedJwtFactory = signedClaimSetJwt;
        this.configurationService = configurationService;
    }

    public SignedJWT generateSignedVerifiableCredentialJwt(String subject, List contraIndictators)
            throws JOSEException {
        var now = Instant.now();
        ObjectMapper mapper =
                new ObjectMapper()
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule());

        int size = contraIndictators.size();
        var addresses = new Object[size];
        for (int i = 0; i < size; i++) {
            // addresses[i] = mapper.convertValue(canonicalAddresses.get(i), Map.class);
        }

        var claimsSet =
                new JWTClaimsSet.Builder()
                        .claim(SUBJECT, subject)
                        .claim(ISSUER, configurationService.getVerifiableCredentialIssuer())
                        .claim(NOT_BEFORE, now.getEpochSecond())
                        .claim(
                                EXPIRATION_TIME,
                                now.plusSeconds(configurationService.getMaxJwtTtl())
                                        .getEpochSecond())
                        .claim(
                                VerifiableCredentialConstants.VC_CLAIM,
                                Map.of(
                                        VerifiableCredentialConstants.VC_TYPE,
                                        new String[] {
                                            VerifiableCredentialConstants
                                                    .VERIFIABLE_CREDENTIAL_TYPE,
                                            VerifiableCredentialConstants.FRAUD_CREDENTIAL_TYPE
                                        },
                                        VerifiableCredentialConstants.VC_CONTEXT,
                                        new String[] {
                                            VerifiableCredentialConstants.W3_BASE_CONTEXT,
                                            VerifiableCredentialConstants.DI_CONTEXT
                                        },
                                        VerifiableCredentialConstants.VC_CREDENTIAL_SUBJECT,
                                        Map.of(
                                                VerifiableCredentialConstants.VC_FRAUD_KEY,
                                                addresses)))
                        .build();

        return signedJwtFactory.createSignedJwt(claimsSet);
    }
}
