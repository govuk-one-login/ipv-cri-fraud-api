package uk.gov.di.ipv.cri.fraud.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.*;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.fixtures.TestFixtures;
import uk.gov.di.ipv.cri.fraud.api.util.TestDataCreator;
import uk.gov.di.ipv.cri.fraud.library.FraudPersonIdentityDetailedMapper;
import uk.gov.di.ipv.cri.fraud.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.di.ipv.cri.fraud.api.domain.VerifiableCredentialConstants.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class VerifiableCredentialServiceTest implements TestFixtures {

    private static final Logger LOGGER = LogManager.getLogger();

    @SystemStub private EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private static final String UNIT_TEST_VC_KEYID = "UNIT_TEST_VC_KEYID";
    private static final String UNIT_TEST_VC_ISSUER = "https://review-f.account.gov.uk";

    // Returned via the ServiceFactory
    private final ObjectMapper realObjectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock private ServiceFactory mockServiceFactory;
    @Mock private ParameterStoreService mockParameterStoreService;
    @Mock private ConfigurationService mockCommonLibConfigurationService;

    private VerifiableCredentialService verifiableCredentialService;

    @BeforeEach
    void setup() throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException {
        environmentVariables.set("AWS_REGION", "eu-west-2");

        mockServiceFactoryBehaviour();

        JWSSigner jwsSigner = new ECDSASigner(getPrivateKey());

        verifiableCredentialService =
                new VerifiableCredentialService(mockServiceFactory, jwsSigner);
    }

    @ParameterizedTest
    @CsvSource({
        "3600, 1, true", // 1 hour, 1 address, IncludeKidInVc
        "1814400, 1, true", // 3 weeks, 1 address, IncludeKidInVc
        "15780000, 1, true", // 6 months, 1 address, IncludeKidInVc
        "3600, 2, true", // 1 hour, 2 addresses, IncludeKidInVc
        "1814400, 2, true", // 3 weeks, 2 addresses, IncludeKidInVc
        "15780000, 2, true", // 6 months, 2 addresses, IncludeKidInVc
        "3600, 3, true", // 1 hour, 3 addresses, IncludeKidInVc
        "1814400, 3, true", // 3 weeks, 3 addresses, IncludeKidInVc
        "15780000, 3, true", // 6 months, 3 addresses, IncludeKidInVc
        "3600, 4, true", // 1 hour, 4 addresses, IncludeKidInVc
        "1814400, 4, true", // 3 weeks, 4 addresses, IncludeKidInVc
        "15780000, 4, true", // 6 months, 4 addresses, IncludeKidInVc
        "3600, 5, true", // 1 hour, 5 addresses, IncludeKidInVc
        "1814400, 5, true", // 3 weeks, 5 addresses, IncludeKidInVc
        "15780000, 5, true", // 6 months, 5 addresses, IncludeKidInVc
        "3600, 1, false", // 1 hour, 1 address
        "1814400, 1, false", // 3 weeks, 1 address
        "15780000, 1, false", // 6 months, 1 address
        "3600, 2, false", // 1 hour, 2 addresses
        "1814400, 2, false", // 3 weeks, 2 addresses
        "15780000, 2, false", // 6 months, 2 addresses
        "3600, 3, false", // 1 hour, 3 addresses
        "1814400, 3, false", // 3 weeks, 3 addresses
        "15780000, 3, false", // 6 months, 3 addresses
        "3600, 4, false", // 1 hour, 4 addresses
        "1814400, 4, false", // 3 weeks, 4 addresses
        "15780000, 4, false", // 6 months, 4 addresses
        "3600, 5, false", // 1 hour, 5 addresses
        "1814400, 5, false", // 3 weeks, 5 addresses
        "15780000, 5, false", // 6 months, 5 addresses
    })
    void verifiableCredentialServiceTest(
            long maxExpiryTime, int addressCount, boolean includeKidInVC)
            throws JOSEException, JsonProcessingException, ParseException, MalformedURLException,
                    NoSuchAlgorithmException {
        environmentVariables.set("INCLUDE_VC_KID", includeKidInVC);

        FraudResultItem fraudResultItem =
                new FraudResultItem(UUID.randomUUID(), List.of("A01"), 1, 1, 90);

        PersonIdentityDetailed personIdentityDetailed =
                FraudPersonIdentityDetailedMapper.generatePersonIdentityDetailed(
                        TestDataCreator.createTestPersonIdentityMultipleAddresses(addressCount));

        when(mockCommonLibConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn(UNIT_TEST_VC_ISSUER);
        if (includeKidInVC) {
            when(mockCommonLibConfigurationService.getCommonParameterValue(
                            "verifiable-credential/issuer"))
                    .thenReturn(UNIT_TEST_VC_ISSUER);
            when(mockCommonLibConfigurationService.getCommonParameterValue(
                            "verifiableCredentialKmsSigningKeyId"))
                    .thenReturn(UNIT_TEST_VC_KEYID);
        }
        when(mockCommonLibConfigurationService.getMaxJwtTtl()).thenReturn(maxExpiryTime);

        final String UNIT_TEST_MAX_JWT_TTL_UNIT = "SECONDS";
        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.STACK, ParameterStoreParameters.MAX_JWT_TTL_UNIT))
                .thenReturn(UNIT_TEST_MAX_JWT_TTL_UNIT);

        final String UNIT_TEST_SUBJECT = "urn:fdc:12345678";
        SignedJWT signedJWT =
                verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        UNIT_TEST_SUBJECT, fraudResultItem, personIdentityDetailed);

        JWTClaimsSet generatedClaims = signedJWT.getJWTClaimsSet();
        assertTrue(signedJWT.verify(new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1))));

        JWSHeader generatedJWSHeader = null;
        if (includeKidInVC) {
            generatedJWSHeader = signedJWT.getHeader();
            String[] jwsHeaderParts = generatedJWSHeader.getKeyID().split(":");
            String[] issuerHash = jwsHeaderParts[2].split("#");
            String actualIssuer = issuerHash[0];

            assertEquals("did", jwsHeaderParts[0]);
            assertEquals("web", jwsHeaderParts[1]);
            assertEquals("review-f.account.gov.uk", actualIssuer);
        }

        String jsonGeneratedClaims =
                realObjectMapper
                        .writer()
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(generatedClaims);
        LOGGER.info(jsonGeneratedClaims);

        JsonNode claimsSet = realObjectMapper.readTree(generatedClaims.toString());
        assertEquals(5, claimsSet.size());

        assertAll(
                () -> {
                    assertEquals(
                            fraudResultItem.getContraIndicators().get(0),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_EVIDENCE_KEY)
                                    .get(0)
                                    .get("ci")
                                    .get(0)
                                    .asText());
                    assertEquals(
                            fraudResultItem.getIdentityFraudScore(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_EVIDENCE_KEY)
                                    .get(0)
                                    .get("identityFraudScore")
                                    .asInt());

                    assertNotEquals(0, addressCount);

                    IntStream.range(0, addressCount)
                            .forEach(
                                    a -> {
                                        Address address =
                                                personIdentityDetailed.getAddresses().get(a);
                                        JsonNode claimSetJWTAddress =
                                                claimsSet
                                                        .get(VC_CLAIM)
                                                        .get(VC_CREDENTIAL_SUBJECT)
                                                        .get(VC_ADDRESS_KEY)
                                                        .get(a);
                                        assertEquals(
                                                address.getBuildingNumber(),
                                                claimSetJWTAddress.get("buildingNumber").asText());

                                        assertEquals(
                                                address.getStreetName(),
                                                claimSetJWTAddress.get("streetName").asText());
                                        assertEquals(
                                                address.getAddressLocality(),
                                                claimSetJWTAddress.get("addressLocality").asText());
                                        assertEquals(
                                                address.getPostalCode(),
                                                claimSetJWTAddress.get("postalCode").asText());
                                        assertEquals(
                                                "GB",
                                                claimSetJWTAddress.get("addressCountry").asText());
                                    });
                });

        assertEquals(UNIT_TEST_VC_ISSUER, claimsSet.get("iss").textValue());
        assertEquals(UNIT_TEST_SUBJECT, claimsSet.get("sub").textValue());

        ECDSAVerifier ecVerifier = new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1));
        assertTrue(signedJWT.verify(ecVerifier));

        long notBeforeTime = claimsSet.get("nbf").asLong();
        final long expirationTime = claimsSet.get("exp").asLong();

        assertEquals(expirationTime, notBeforeTime + maxExpiryTime); // testsWithinExpiryTimeVC
        assertTrue(
                expirationTime < notBeforeTime + maxExpiryTime + 10L); // testsExceedsExpiryTimeVC
    }

    private void mockServiceFactoryBehaviour() {
        when(mockServiceFactory.getObjectMapper()).thenReturn(realObjectMapper);
        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);
        when(mockServiceFactory.getCommonLibConfigurationService())
                .thenReturn(mockCommonLibConfigurationService);
    }
}
