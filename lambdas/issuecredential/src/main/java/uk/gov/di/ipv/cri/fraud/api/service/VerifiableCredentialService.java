package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.ThirdPartyAddress;
import uk.gov.di.ipv.cri.fraud.api.util.EvidenceHelper;
import uk.gov.di.ipv.cri.fraud.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;

import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.FRAUD_CREDENTIAL_TYPE;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.VC_ADDRESS_KEY;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.VC_BIRTHDATE_KEY;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.VC_NAME_KEY;

public class VerifiableCredentialService {

    private final ObjectMapper objectMapper;
    private final ParameterStoreService parameterStoreService;
    private final ConfigurationService commonLibConfigurationService;
    private final SignedJWTFactory signedJwtFactory;
    private final VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder;

    public VerifiableCredentialService(ServiceFactory serviceFactory, JWSSigner jwsSigner) {
        this.objectMapper = serviceFactory.getObjectMapper();
        this.parameterStoreService = serviceFactory.getParameterStoreService();
        this.commonLibConfigurationService = serviceFactory.getCommonLibConfigurationService();

        this.signedJwtFactory = new SignedJWTFactory(jwsSigner);

        this.vcClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        commonLibConfigurationService, Clock.systemUTC());
    }

    public SignedJWT generateSignedVerifiableCredentialJwt(
            String subject,
            FraudResultItem fraudResultItem,
            PersonIdentityDetailed personIdentityDetailed)
            throws JOSEException, NoSuchAlgorithmException {
        long jwtTtl = commonLibConfigurationService.getMaxJwtTtl();

        ChronoUnit jwtTtlUnit =
                ChronoUnit.valueOf(
                        parameterStoreService.getParameterValue(
                                ParameterPrefix.STACK, ParameterStoreParameters.MAX_JWT_TTL_UNIT));

        var claimsSet =
                this.vcClaimsSetBuilder
                        .subject(subject)
                        .timeToLive(jwtTtl, jwtTtlUnit)
                        .verifiableCredentialType(FRAUD_CREDENTIAL_TYPE)
                        .verifiableCredentialSubject(
                                Map.of(
                                        VC_ADDRESS_KEY,
                                        convertAddresses(personIdentityDetailed.getAddresses()),
                                        VC_NAME_KEY,
                                        personIdentityDetailed.getNames(),
                                        VC_BIRTHDATE_KEY,
                                        convertBirthDates(personIdentityDetailed.getBirthDates())))
                        .verifiableCredentialEvidence(calculateEvidence(fraudResultItem))
                        .build();

        SignedJWT signedJwt = null;
        if (Boolean.parseBoolean(System.getenv("INCLUDE_VC_KID"))) {
            String issuer =
                    commonLibConfigurationService.getCommonParameterValue(
                            "verifiable-credential/issuer");
            String kmsSigningKeyId =
                    commonLibConfigurationService.getCommonParameterValue(
                            "verifiableCredentialKmsSigningKeyId");
            signedJwt = signedJwtFactory.createSignedJwt(claimsSet, issuer, kmsSigningKeyId);
        } else {
            signedJwt = signedJwtFactory.createSignedJwt(claimsSet);
        }
        return signedJwt;
    }

    private Object[] convertAddresses(List<Address> addresses) {
        return addresses.stream()
                .map(address -> objectMapper.convertValue(address, ThirdPartyAddress.class))
                .toArray();
    }

    private Object[] convertBirthDates(List<BirthDate> birthDates) {
        return birthDates.stream()
                .map(
                        birthDate ->
                                Map.of(
                                        "value",
                                        birthDate
                                                .getValue()
                                                .format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .toArray();
    }

    private Object[] calculateEvidence(FraudResultItem fraudResultItem) {

        Evidence evidence = EvidenceHelper.fraudCheckResultItemToEvidence(fraudResultItem);

        // DecisionScore not currently requested to be in VC
        evidence.setDecisionScore(null);

        return new Map[] {objectMapper.convertValue(evidence, Map.class)};
    }
}
