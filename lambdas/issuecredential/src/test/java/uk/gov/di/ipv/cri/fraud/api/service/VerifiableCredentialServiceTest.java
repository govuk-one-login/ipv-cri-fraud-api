package uk.gov.di.ipv.cri.fraud.api.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.ipv.cri.fraud.api.service.fixtures.TestFixtures;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialServiceTest implements TestFixtures {
    /*public static final String SUBJECT = "subject";
    public static final String UPRN = "72262801";
    public static final String BUILDING_NUMBER = "8";
    public static final String STREET_NAME = "GRANGE FIELDS WAY";

    public static final String ADDRESS_LOCALITY = "LEEDS";
    public static final String POSTAL_CODE = "LS10 4QL";
    public static final String COUNTRY_CODE = "GB";
    public static final LocalDate VALID_FROM = LocalDate.of(2010, 02, 26);
    public static final LocalDate VALID_UNTIL = LocalDate.of(2021, 01, 16);
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock private ConfigurationService mockConfigurationService;

    @BeforeEach
    void setUp() {
        when(mockConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn("https://address-cri.account.gov.uk.TBC");
    }

    @Test
    void shouldReturnAVerifiedCredentialWhenGivenCanonicalAddresses() throws JOSEException {
        SignedJWTFactory mockSignedClaimSetJwt = mock(SignedJWTFactory.class);
        var verifiableCredentialService =
                new VerifiableCredentialService(mockSignedClaimSetJwt, mockConfigurationService);

        when(mockConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn("address-cri-issue");
        when(mockConfigurationService.getMaxJwtTtl()).thenReturn(342L);

        var canonicalAddresses = List.of(mock(CanonicalAddress.class));

        verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                SUBJECT, canonicalAddresses);

        verify(mockSignedClaimSetJwt).createSignedJwt(any());
    }

    @Test
    void shouldCreateValidSignedJWT()
            throws InvalidKeySpecException, NoSuchAlgorithmException, JOSEException, ParseException,
                    JsonProcessingException {

        CanonicalAddress address = new CanonicalAddress();
        address.setUprn(Long.valueOf(UPRN));
        address.setBuildingNumber(BUILDING_NUMBER);
        address.setStreetName(STREET_NAME);
        address.setAddressLocality(ADDRESS_LOCALITY);
        address.setPostalCode(POSTAL_CODE);
        address.setAddressCountry(COUNTRY_CODE);
        address.setValidFrom(VALID_FROM);
        address.setValidUntil(VALID_UNTIL);
        List<CanonicalAddress> canonicalAddresses = List.of(address);

        SignedJWTFactory signedJwtFactory = new SignedJWTFactory(new ECDSASigner(getPrivateKey()));
        var verifiableCredentialService =
                new VerifiableCredentialService(signedJwtFactory, mockConfigurationService);

        SignedJWT signedJWT =
                verifiableCredentialService.generateSignedVerifiableCredentialJwt(
                        SUBJECT, canonicalAddresses);
        JWTClaimsSet generatedClaims = signedJWT.getJWTClaimsSet();
        assertTrue(signedJWT.verify(new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1))));

        JsonNode claimsSet = objectMapper.readTree(generatedClaims.toString());

        assertEquals(5, claimsSet.size());

        assertAll(
                () -> {
                    assertEquals(
                            address.getUprn().get().toString(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("uprn")
                                    .asText());
                    assertEquals(
                            address.getBuildingNumber(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("buildingNumber")
                                    .asText());
                    assertEquals(
                            address.getStreetName(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("streetName")
                                    .asText());
                    assertEquals(
                            address.getAddressLocality(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("addressLocality")
                                    .asText());
                    assertEquals(
                            address.getPostalCode(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("postalCode")
                                    .asText());
                    assertEquals(
                            address.getAddressCountry(),
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("addressCountry")
                                    .asText());
                    assertEquals(
                            "2010-02-26",
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("validFrom")
                                    .asText());
                    assertEquals(
                            "2021-01-16",
                            claimsSet
                                    .get(VC_CLAIM)
                                    .get(VC_CREDENTIAL_SUBJECT)
                                    .get(VC_ADDRESS_KEY)
                                    .get(0)
                                    .get("validUntil")
                                    .asText());
                });
        ECDSAVerifier ecVerifier = new ECDSAVerifier(ECKey.parse(TestFixtures.EC_PUBLIC_JWK_1));
        assertTrue(signedJWT.verify(ecVerifier));
    }*/
}
