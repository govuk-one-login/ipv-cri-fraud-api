package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.KMSSigner;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.fraud.api.domain.ThirdPartyAddress;
import uk.gov.di.ipv.cri.fraud.api.domain.audit.Evidence;
import uk.gov.di.ipv.cri.fraud.api.persistence.item.FraudResultItem;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.FRAUD_CREDENTIAL_TYPE;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.VC_ADDRESS_KEY;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.VC_BIRTHDATE_KEY;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.VC_NAME_KEY;

public class VerifiableCredentialService {

    private final SignedJWTFactory signedJwtFactory;
    private final ConfigurationService configurationService;
    private final ObjectMapper objectMapper;
    private final VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder;

    public VerifiableCredentialService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.signedJwtFactory =
                new SignedJWTFactory(
                        new KMSSigner(
                                configurationService.getCommonParameterValue(
                                        "verifiableCredentialKmsSigningKeyId")));
        this.objectMapper =
                new ObjectMapper()
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule());
        this.vcClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        this.configurationService, Clock.systemUTC());
    }

    public VerifiableCredentialService(
            SignedJWTFactory signedClaimSetJwt,
            ConfigurationService configurationService,
            ObjectMapper objectMapper,
            VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder) {
        this.signedJwtFactory = signedClaimSetJwt;
        this.configurationService = configurationService;
        this.objectMapper = objectMapper;
        this.vcClaimsSetBuilder = vcClaimsSetBuilder;
    }

    public SignedJWT generateSignedVerifiableCredentialJwt(
            String subject,
            FraudResultItem fraudResultItem,
            PersonIdentityDetailed personIdentityDetailed)
            throws JOSEException {
        long jwtTtl = this.configurationService.getMaxJwtTtl();
        ChronoUnit jwtTtlUnit =
                ChronoUnit.valueOf(this.configurationService.getParameterValue("JwtTtlUnit"));
        var now = Instant.now();

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

        return signedJwtFactory.createSignedJwt(claimsSet);
    }

    public String getVerifiableCredentialIssuer() {
        return configurationService.getVerifiableCredentialIssuer();
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

        Evidence evidence = new Evidence();
        evidence.setType("IdentityCheck");
        evidence.setTxn(fraudResultItem.getTransactionId());

        evidence.setIdentityFraudScore(fraudResultItem.getIdentityFraudScore());
        evidence.setCi(fraudResultItem.getContraIndicators());

        return new Map[] {objectMapper.convertValue(evidence, Map.class)};
    }
}
