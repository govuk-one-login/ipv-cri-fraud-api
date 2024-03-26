package uk.gov.di.ipv.cri.fraud.api.pact;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.SelectorBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.lambda.powertools.parameters.ParamProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;
import uk.gov.di.ipv.cri.common.library.domain.SessionRequest;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.persistence.item.CanonicalAddress;
import uk.gov.di.ipv.cri.common.library.persistence.item.SessionItem;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityDateOfBirth;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityItem;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityName;
import uk.gov.di.ipv.cri.common.library.persistence.item.personidentity.PersonIdentityNamePart;
import uk.gov.di.ipv.cri.common.library.service.AuditService;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityMapper;
import uk.gov.di.ipv.cri.common.library.service.PersonIdentityService;
import uk.gov.di.ipv.cri.common.library.service.SessionService;
import uk.gov.di.ipv.cri.common.library.util.EventProbe;
import uk.gov.di.ipv.cri.common.library.util.ListUtil;
import uk.gov.di.ipv.cri.common.library.util.SignedJWTFactory;
import uk.gov.di.ipv.cri.common.library.util.VerifiableCredentialClaimsSetBuilder;
import uk.gov.di.ipv.cri.fraud.api.handler.IssueCredentialHandler;
import uk.gov.di.ipv.cri.fraud.api.pact.utils.Injector;
import uk.gov.di.ipv.cri.fraud.api.pact.utils.MockHttpServer;
import uk.gov.di.ipv.cri.fraud.api.service.FraudRetrievalService;
import uk.gov.di.ipv.cri.fraud.api.service.IssueCredentialConfigurationService;
import uk.gov.di.ipv.cri.fraud.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;

import java.io.IOException;
import java.net.URI;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IDENTITY_THEFT_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.IMPERSONATION_RISK_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.MORTALITY_CHECK;
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.SYNTHETIC_IDENTITY_CHECK;

@Tag("Pact")
@Provider("FraudVcProvider")
@PactBroker(
        url = "https://${PACT_BROKER_HOST}",
        authentication =
                @PactBrokerAuth(
                        username = "${PACT_BROKER_USERNAME}",
                        password = "${PACT_BROKER_PASSWORD}"))
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IssueCredentialHandlerTest {

    private static final int PORT = 5050;

    @Mock private ConfigurationService configurationService;
    @Mock private IssueCredentialConfigurationService icConfigurationService;
    @Mock private DataStore<SessionItem> dataStore;
    @Mock private DataStore<PersonIdentityItem> personIdentityDataStore;
    @Mock private EventProbe eventProbe;
    @Mock private AuditService auditService;
    @Mock private DataStore<FraudResultItem> fraudItemDataStore;
    @Mock private SecretsProvider secretsProvider;
    @Mock private ParamProvider paramProvider;
    private SessionService sessionService;
    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModules(new JavaTimeModule());

    @au.com.dius.pact.provider.junitsupport.loader.PactBrokerConsumerVersionSelectors
    public static SelectorBuilder consumerVersionSelectors() {
        return new SelectorBuilder()
                .tag("FraudVcProvider")
                .branch("main", "IpvCoreBack")
                .deployedOrReleased();
    }

    @BeforeAll
    static void setupServer() {
        System.setProperty("pact.verifier.publishResults", "true");
        System.setProperty("pact.content_type.override.application/jwt", "text");
    }

    @BeforeEach
    void pactSetup(PactVerificationContext context)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

        long todayPlusADay =
                LocalDate.now().plusDays(2).toEpochSecond(LocalTime.now(), ZoneOffset.UTC);

        when(configurationService.getVerifiableCredentialIssuer())
                .thenReturn("dummyFraudComponentId");
        when(configurationService.getSessionExpirationEpoch()).thenReturn(todayPlusADay);
        when(configurationService.getAuthorizationCodeExpirationEpoch()).thenReturn(todayPlusADay);
        when(configurationService.getMaxJwtTtl()).thenReturn(1000L);
        when(configurationService.getParameterValue("JwtTtlUnit")).thenReturn("HOURS");
        when(configurationService.getVerifiableCredentialIssuer())
                .thenReturn("dummyFraudComponentId");
        when(configurationService.getParameterValueByAbsoluteName(
                        "/release-flags/vc-expiry-removed"))
                .thenReturn("true");
        when(configurationService.getParameterValue("release-flags/vc-contains-unique-id"))
                .thenReturn("true");
        when(icConfigurationService.isActivityHistoryEnabled()).thenReturn(true);

        sessionService =
                new SessionService(
                        dataStore, configurationService, Clock.systemUTC(), new ListUtil());

        KeyFactory kf = KeyFactory.getInstance("EC");
        EncodedKeySpec privateKeySpec =
                new PKCS8EncodedKeySpec(
                        Base64.getDecoder()
                                .decode(
                                        "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBYNBSda5ttN9Wu4Do4"
                                                + "gLV1xaks+DB5n6ity2MvBlzDUw=="));
        JWSSigner signer = new ECDSASigner((ECPrivateKey) kf.generatePrivate(privateKeySpec));

        SignedJWTFactory signedClaimSetJwt = new SignedJWTFactory(signer);

        VerifiableCredentialClaimsSetBuilder vcClaimsSetBuilder =
                new VerifiableCredentialClaimsSetBuilder(
                        this.configurationService, Clock.systemUTC());

        PersonIdentityMapper personIdentityMapper = new PersonIdentityMapper();

        Injector tokenHandlerInjector =
                new Injector(
                        new IssueCredentialHandler(
                                new VerifiableCredentialService(
                                        signedClaimSetJwt,
                                        configurationService,
                                        objectMapper,
                                        vcClaimsSetBuilder,
                                        icConfigurationService),
                                sessionService,
                                eventProbe,
                                auditService,
                                new PersonIdentityService(
                                        personIdentityMapper,
                                        configurationService,
                                        personIdentityDataStore),
                                new FraudRetrievalService(
                                        fraudItemDataStore, icConfigurationService),
                                icConfigurationService),
                        "/credential/issue",
                        "/");
        MockHttpServer.startServer(new ArrayList<>(List.of(tokenHandlerInjector)), PORT, signer);

        context.setTarget(new HttpTestTarget("localhost", PORT));
    }

    @AfterEach
    public void tearDown() {
        MockHttpServer.stopServer();
    }

    @State("dummyApiKey is a valid api key")
    void dummyAPIKeyIsValid() {}

    @State("dummyAccessToken is a valid access token")
    void accessTokenIsValid() {
        long todayPlusADay =
                LocalDate.now().plusDays(2).toEpochSecond(LocalTime.now(), ZoneOffset.UTC);

        // INITIAL SESSION HANDOFF
        UUID sessionId = performInitialSessionRequest(sessionService, todayPlusADay);
        setSessionIntoMockDB(sessionId);
        // INITIAL SESSION HANDOFF

        // SIMULATED CRI LOGIC
        PersonIdentityNamePart firstNamePart = new PersonIdentityNamePart();
        firstNamePart.setType("GivenName");
        firstNamePart.setValue("Kenneth");
        PersonIdentityNamePart surnamePart = new PersonIdentityNamePart();
        surnamePart.setType("FamilyName");
        surnamePart.setValue("Decerqueira");
        PersonIdentityName name = new PersonIdentityName();
        name.setNameParts(List.of(firstNamePart, surnamePart));

        PersonIdentityDateOfBirth birthDate = new PersonIdentityDateOfBirth();
        birthDate.setValue(LocalDate.of(1965, 7, 8));

        CanonicalAddress address = new CanonicalAddress();
        address.setBuildingNumber("8");
        address.setBuildingName("LE FLAMBE");
        address.setStreetName("HADLEY ROAD");
        address.setAddressLocality("BATH");
        address.setPostalCode("BA2 5AA");
        address.setValidFrom(LocalDate.now().minusDays(1));

        PersonIdentityItem personIdentityItem = new PersonIdentityItem();
        personIdentityItem.setExpiryDate(
                LocalDate.of(2030, 1, 1).toEpochSecond(LocalTime.now(), ZoneOffset.UTC));
        personIdentityItem.setSessionId(sessionId);
        personIdentityItem.setAddresses(List.of(address));
        personIdentityItem.setNames(List.of(name));
        personIdentityItem.setBirthDates(List.of(birthDate));

        when(personIdentityDataStore.getItem(sessionId.toString())).thenReturn(personIdentityItem);

        // SESSION HANDBACK
        performAuthorizationCodeSet(sessionService, sessionId);
        // SESSION HANDBACK

        // ACCESS TOKEN GENERATION AND SETTING
        SessionItem session = performAccessTokenSet(sessionService, sessionId);
        // ACCESS TOKEN GENERATION AND SETTING

        when(dataStore.getItemByIndex(SessionItem.ACCESS_TOKEN_INDEX, "Bearer dummyAccessToken"))
                .thenReturn(List.of(session));
    }

    @State("test-subject is a valid subject")
    void dummyTestSubjectIsValidSubject() {}

    @State("dummyFraudComponentId is a valid issuer")
    void dummyIdIsValidIssuer() {}

    @State("VC is for Kenneth Decerqueira")
    void dummyVcIsValidName() {}

    @State("VC birthDate is 1965-07-08")
    void dummyBirthDateIsValid() {}

    @State("VC evidence identityFraudScore is 1")
    void dummyFraudScoreIsValid() throws ParseException {

        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        FraudResultItem resultItem = new FraudResultItem();
        resultItem.setTransactionId("dummyTxn"); // Crosscore Id
        resultItem.setPepTransactionId("dummyTxnFailed");
        resultItem.setActivityHistoryScore(1);
        resultItem.setIdentityFraudScore(1);
        resultItem.setDecisionScore("30");
        resultItem.setActivityFrom("2013-12-01");
        resultItem.setCheckDetails(
                List.of(
                        MORTALITY_CHECK.toString().toLowerCase(),
                        IDENTITY_THEFT_CHECK.toString(),
                        SYNTHETIC_IDENTITY_CHECK.toString().toLowerCase()));
        resultItem.setFailedCheckDetails(
                List.of(IMPERSONATION_RISK_CHECK.toString().toLowerCase()));
        resultItem.setContraIndicators(List.of());

        when(fraudItemDataStore.getItem(sessionId)).thenReturn(resultItem);
    }

    @State("VC evidence identityFraudScore is 2")
    void dummyFraudScoreIsInvalid() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        FraudResultItem resultItem = new FraudResultItem();
        resultItem.setTransactionId("dummyTxn"); // Crosscore Id
        resultItem.setPepTransactionId("dummyTxn");
        resultItem.setActivityHistoryScore(1);
        resultItem.setIdentityFraudScore(2);
        resultItem.setDecisionScore("30");
        resultItem.setActivityFrom("2013-12-01");
        resultItem.setContraIndicators(List.of());
        resultItem.setCheckDetails(
                List.of(
                        MORTALITY_CHECK.toString().toLowerCase(),
                        IDENTITY_THEFT_CHECK.toString(),
                        SYNTHETIC_IDENTITY_CHECK.toString(),
                        IMPERSONATION_RISK_CHECK.toString().toLowerCase()));

        when(fraudItemDataStore.getItem(sessionId)).thenReturn(resultItem);
    }

    @State("VC evidence activityHistoryScore is 1")
    void dummyActivityHistoryScoreIsValid() {}

    @State("VC evidence txn is dummyTxn")
    void dummyEvidenceIsValid() {}

    @State("VC evidence failed txn is dummyTxnFailed")
    void dummyTxnIsInvalid() {}

    @State("VC credentialSubject address streetName is HADLEY ROAD")
    void dummyStreetNameIsValid() {}

    @State("VC credentialSubject address buildingName is LE FLAMBE")
    void dummyBuildingNameIsValid() {}

    @State("VC credentialSubject address addressType is CURRENT")
    void DummyAddressTypeIsValid() {}

    @State("VC credentialSubject address postalCode is BA2 5AA")
    void dummyPostalCodeIsValid() {}

    @State("VC credentialSubject address buildingNumber is 8")
    void dummyBuildingNumberIsValid() {}

    @State("VC credentialSubject address addressLocality is BATH")
    void dummyLocalityIsValid() {}

    @State("VC has CI of CI1")
    void dummyVcHasCIA01() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();
        String sessionId = sessionUUID.toString();

        FraudResultItem resultItem = new FraudResultItem();
        resultItem.setContraIndicators(List.of("CI1"));
        resultItem.setTransactionId("dummyTxn"); // Crosscore Id
        resultItem.setPepTransactionId("dummyTxn");
        resultItem.setActivityHistoryScore(1);
        resultItem.setIdentityFraudScore(2);
        resultItem.setDecisionScore("30");
        resultItem.setActivityFrom("2013-12-01");
        resultItem.setCheckDetails(
                List.of(
                        MORTALITY_CHECK.toString().toLowerCase(),
                        IDENTITY_THEFT_CHECK.toString(),
                        SYNTHETIC_IDENTITY_CHECK.toString().toLowerCase(),
                        IMPERSONATION_RISK_CHECK.toString()));

        when(fraudItemDataStore.getItem(sessionId)).thenReturn(resultItem);
    }

    @State(
            "Experian conducted mortality_check, identity_theft_check, synthetic_identity_check and impersonation_risk_check")
    void dummyExperianConductedChecks() {}

    @State("VC evidence activityFrom is 2013-12-01")
    void dummyActivityFromIsValid() {}

    @State("dummyInvalidAccessToken is an invalid access token")
    void dummyAccessTokenIsInvalidAccessToken() {}

    @State("FRAUD CRI uses CORE_BACK_SIGNING_PRIVATE_KEY_JWK to validate core signatures")
    void privateJwkKeyIsValidCoreSignature() {}

    @State("dummyFraudComponentId is the FRAUD CHECK CRI component ID")
    void componentIdIsValid() {}

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testMethod(PactVerificationContext context) {
        context.verifyInteraction();
    }

    private SessionItem performAuthorizationCodeSet(SessionService sessionService, UUID sessionId) {
        SessionItem session = sessionService.getSession(sessionId.toString());
        sessionService.createAuthorizationCode(session);
        session.setAuthorizationCode("dummyAuthCode");
        sessionService.updateSession(session);
        return session;
    }

    private SessionItem performAccessTokenSet(SessionService sessionService, UUID sessionId) {
        SessionItem session = sessionService.getSession(sessionId.toString());
        session.setAccessToken("dummyAccessToken");
        session.setAccessTokenExpiryDate(
                LocalDate.now().plusDays(1).toEpochSecond(LocalTime.now(), ZoneOffset.UTC));
        sessionService.updateSession(session);
        return session;
    }

    private void setSessionIntoMockDB(UUID sessionId) {
        ArgumentCaptor<SessionItem> sessionItemArgumentCaptor =
                ArgumentCaptor.forClass(SessionItem.class);

        verify(dataStore).create(sessionItemArgumentCaptor.capture());

        SessionItem savedSessionitem = sessionItemArgumentCaptor.getValue();

        when(dataStore.getItem(sessionId.toString())).thenReturn(savedSessionitem);
    }

    private UUID performInitialSessionRequest(SessionService sessionService, long todayPlusADay) {
        SessionRequest sessionRequest = new SessionRequest();
        sessionRequest.setNotBeforeTime(new Date(todayPlusADay));
        sessionRequest.setClientId("ipv-core");
        sessionRequest.setAudience("dummyFraudComponentId");
        sessionRequest.setRedirectUri(URI.create("http://localhost:5050"));
        sessionRequest.setExpirationTime(new Date(todayPlusADay));
        sessionRequest.setIssuer("ipv-core");
        sessionRequest.setClientId("ipv-core");
        sessionRequest.setSubject("test-subject");

        doNothing().when(dataStore).create(any(SessionItem.class));

        return sessionService.saveSession(sessionRequest);
    }
}
