package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.lambda.powertools.parameters.ParamManager;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.Address;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.BirthDate;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentityDetailed;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.KMSSigner;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.fraud.api.domain.Evidence;
import uk.gov.di.ipv.cri.fraud.api.domain.ThirdPartyAddress;
import uk.gov.di.ipv.cri.fraud.api.util.EvidenceHelper;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

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
    private final ConfigurationService commonConfigurationService;
    private final uk.gov.di.ipv.cri.fraud.api.service.ConfigurationService configurationService;
    private final ObjectMapper objectMapper;
    private final VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder;

    public VerifiableCredentialService(ConfigurationService commonConfigurationService) {
        this.configurationService =
                new uk.gov.di.ipv.cri.fraud.api.service.ConfigurationService(
                        ParamManager.getSecretsProvider(),
                        ParamManager.getSsmProvider(),
                        System.getenv("ENVIRONMENT"));
        this.commonConfigurationService = commonConfigurationService;
        this.signedJwtFactory =
                new SignedJWTFactory(
                        new KMSSigner(
                                commonConfigurationService.getCommonParameterValue(
                                        "verifiableCredentialKmsSigningKeyId")));
        this.objectMapper =
                new ObjectMapper()
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule());
        this.vcClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        this.commonConfigurationService, Clock.systemUTC());
    }

    public VerifiableCredentialService(
            SignedJWTFactory signedClaimSetJwt,
            ConfigurationService commonConfigurationService,
            ObjectMapper objectMapper,
            VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder,
            uk.gov.di.ipv.cri.fraud.api.service.ConfigurationService configurationService) {
        this.signedJwtFactory = signedClaimSetJwt;
        this.commonConfigurationService = commonConfigurationService;
        this.objectMapper = objectMapper;
        this.vcClaimsSetBuilder = vcClaimsSetBuilder;
        this.configurationService = configurationService;
    }

    public SignedJWT generateSignedVerifiableCredentialJwt(
            String subject,
            FraudResultItem fraudResultItem,
            PersonIdentityDetailed personIdentityDetailed)
            throws JOSEException {
        long jwtTtl = this.commonConfigurationService.getMaxJwtTtl();
        ChronoUnit jwtTtlUnit =
                ChronoUnit.valueOf(this.commonConfigurationService.getParameterValue("JwtTtlUnit"));
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
        return commonConfigurationService.getVerifiableCredentialIssuer();
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

        Evidence evidence =
                EvidenceHelper.fraudCheckResultItemToEvidence(
                        fraudResultItem, configurationService.isActivityHistoryEnabled());

        // DecisionScore not currently requested to be in VC
        evidence.setDecisionScore(null);

        return new Map[] {objectMapper.convertValue(evidence, Map.class)};
    }
}
