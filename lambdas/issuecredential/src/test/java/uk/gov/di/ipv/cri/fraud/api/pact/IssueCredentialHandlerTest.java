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
import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.di.ipv.cri.fraud.api.handler.IssueCredentialHandler;
import uk.gov.di.ipv.cri.fraud.api.pact.utils.Injector;
import uk.gov.di.ipv.cri.fraud.api.pact.utils.MockHttpServer;
import uk.gov.di.ipv.cri.fraud.api.service.VerifiableCredentialService;
import uk.gov.di.ipv.cri.fraud.library.config.ParameterStoreParameters;
import uk.gov.di.ipv.cri.fraud.library.persistence.item.FraudResultItem;
import uk.gov.di.ipv.cri.fraud.library.service.ParameterStoreService;
import uk.gov.di.ipv.cri.fraud.library.service.ResultItemStorageService;
import uk.gov.di.ipv.cri.fraud.library.service.ServiceFactory;
import uk.gov.di.ipv.cri.fraud.library.service.parameterstore.ParameterPrefix;

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
import static uk.gov.di.ipv.cri.fraud.library.domain.CheckType.ACTIVITY_HISTORY_CHECK;
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
class IssueCredentialHandlerTest {

    private static final int PORT = 5030;

    @Mock private ServiceFactory mockServiceFactory;
    @Mock private EventProbe mockEventProbe;
    @Mock private ConfigurationService mockCommonLibConfigurationService;
    private SessionService sessionService;
    @Mock private AuditService mockAuditService;
    @Mock private ResultItemStorageService<FraudResultItem> mockFraudResultItemStorageService;
    @Mock private ParameterStoreService mockParameterStoreService;

    @Mock private DataStore<SessionItem> mockSessionItemDataStore;
    @Mock private DataStore<PersonIdentityItem> mockPersonIdentityDataStore;

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModules(new JavaTimeModule());

    // Off by default to prevent logging all secrets
    private static final boolean ENABLE_FULL_DEBUG = false;

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

        if (ENABLE_FULL_DEBUG) {
            // AutoConfig SL4j with Log4J
            BasicConfigurator.configure();
            Configurator.setAllLevels("", Level.DEBUG);
        }
    }

    @BeforeEach
    void pactSetup(PactVerificationContext context)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {

        mockServiceFactoryBehaviour();

        KeyFactory kf = KeyFactory.getInstance("EC");
        EncodedKeySpec privateKeySpec =
                new PKCS8EncodedKeySpec(
                        Base64.getDecoder()
                                .decode(
                                        "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBYNBSda5ttN9Wu4Do4"
                                                + "gLV1xaks+DB5n6ity2MvBlzDUw=="));
        JWSSigner signer = new ECDSASigner((ECPrivateKey) kf.generatePrivate(privateKeySpec));

        Injector tokenHandlerInjector =
                new Injector(
                        new IssueCredentialHandler(
                                mockServiceFactory,
                                new VerifiableCredentialService(mockServiceFactory, signer)),
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

        // Controls the TTLS
        mockHappyPathVcParameters();

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

        when(mockPersonIdentityDataStore.getItem(sessionId.toString()))
                .thenReturn(personIdentityItem);

        // SESSION HANDBACK
        performAuthorizationCodeSet(sessionService, sessionId);
        // SESSION HANDBACK

        // ACCESS TOKEN GENERATION AND SETTING
        SessionItem session = performAccessTokenSet(sessionService, sessionId);
        // ACCESS TOKEN GENERATION AND SETTING

        when(mockSessionItemDataStore.getItemByIndex(
                        SessionItem.ACCESS_TOKEN_INDEX, "Bearer dummyAccessToken"))
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

        FraudResultItem resultItem = new FraudResultItem();
        resultItem.setTransactionId("dummyTxn"); // Crosscore Id
        resultItem.setPepTransactionId("dummyTxnFailed");
        resultItem.setActivityHistoryScore(1);
        resultItem.setIdentityFraudScore(1);
        resultItem.setDecisionScore(30);
        resultItem.setActivityFrom("2013-12-01");
        resultItem.setCheckDetails(
                List.of(
                        MORTALITY_CHECK.toString(),
                        IDENTITY_THEFT_CHECK.toString(),
                        SYNTHETIC_IDENTITY_CHECK.toString(),
                        ACTIVITY_HISTORY_CHECK.toString()));
        resultItem.setFailedCheckDetails(List.of(IMPERSONATION_RISK_CHECK.toString()));
        resultItem.setContraIndicators(List.of());

        when(mockFraudResultItemStorageService.getResultItem(sessionUUID)).thenReturn(resultItem);
    }

    @State("VC evidence identityFraudScore is 2")
    void dummyFraudScoreIsInvalid() throws ParseException {
        UUID sessionUUID =
                sessionService
                        .getSessionByAccessToken(
                                AccessToken.parse(
                                        "Bearer dummyAccessToken", AccessTokenType.BEARER))
                        .getSessionId();

        FraudResultItem resultItem = new FraudResultItem();
        resultItem.setTransactionId("dummyTxn"); // Crosscore Id
        resultItem.setPepTransactionId("dummyTxn");
        resultItem.setActivityHistoryScore(1);
        resultItem.setIdentityFraudScore(2);
        resultItem.setDecisionScore(30);
        resultItem.setActivityFrom("2013-12-01");
        resultItem.setContraIndicators(List.of());
        resultItem.setCheckDetails(
                List.of(
                        MORTALITY_CHECK.toString(),
                        IDENTITY_THEFT_CHECK.toString(),
                        SYNTHETIC_IDENTITY_CHECK.toString(),
                        IMPERSONATION_RISK_CHECK.toString(),
                        ACTIVITY_HISTORY_CHECK.toString()));

        when(mockFraudResultItemStorageService.getResultItem(sessionUUID)).thenReturn(resultItem);
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

        FraudResultItem resultItem = new FraudResultItem();
        resultItem.setContraIndicators(List.of("CI1"));
        resultItem.setTransactionId("dummyTxn"); // Crosscore Id
        resultItem.setPepTransactionId("dummyTxn");
        resultItem.setActivityHistoryScore(1);
        resultItem.setIdentityFraudScore(2);
        resultItem.setDecisionScore(30);
        resultItem.setActivityFrom("2013-12-01");
        resultItem.setCheckDetails(
                List.of(
                        MORTALITY_CHECK.toString(),
                        IDENTITY_THEFT_CHECK.toString(),
                        SYNTHETIC_IDENTITY_CHECK.toString(),
                        IMPERSONATION_RISK_CHECK.toString(),
                        ACTIVITY_HISTORY_CHECK.toString()));

        when(mockFraudResultItemStorageService.getResultItem(sessionUUID)).thenReturn(resultItem);
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

        verify(mockSessionItemDataStore).create(sessionItemArgumentCaptor.capture());

        SessionItem savedSessionitem = sessionItemArgumentCaptor.getValue();

        when(mockSessionItemDataStore.getItem(sessionId.toString())).thenReturn(savedSessionitem);
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

        doNothing().when(mockSessionItemDataStore).create(any(SessionItem.class));

        return sessionService.saveSession(sessionRequest);
    }

    private void mockServiceFactoryBehaviour() {

        when(mockServiceFactory.getObjectMapper()).thenReturn(objectMapper);
        when(mockServiceFactory.getEventProbe()).thenReturn(mockEventProbe);
        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);
        when(mockServiceFactory.getParameterStoreService()).thenReturn(mockParameterStoreService);
        when(mockServiceFactory.getCommonLibConfigurationService())
                .thenReturn(mockCommonLibConfigurationService);
        sessionService =
                new SessionService(
                        mockSessionItemDataStore,
                        mockCommonLibConfigurationService,
                        Clock.systemUTC(),
                        new ListUtil());
        when(mockServiceFactory.getSessionService()).thenReturn(sessionService);
        when(mockServiceFactory.getAuditService()).thenReturn(mockAuditService);
        when(mockServiceFactory.getPersonIdentityService())
                .thenReturn(
                        new PersonIdentityService(
                                new PersonIdentityMapper(),
                                mockCommonLibConfigurationService,
                                mockPersonIdentityDataStore));
        when(mockServiceFactory.getResultItemStorageService())
                .thenReturn(mockFraudResultItemStorageService);
    }

    private void mockHappyPathVcParameters() {
        // Mock mockCommonLibConfigurationService and TTL's
        long todayPlusADay =
                LocalDate.now().plusDays(2).toEpochSecond(LocalTime.now(), ZoneOffset.UTC);
        when(mockCommonLibConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn("dummyFraudComponentId");
        when(mockCommonLibConfigurationService.getSessionExpirationEpoch())
                .thenReturn(todayPlusADay);
        when(mockCommonLibConfigurationService.getAuthorizationCodeExpirationEpoch())
                .thenReturn(todayPlusADay);
        when(mockCommonLibConfigurationService.getMaxJwtTtl()).thenReturn(1000L);

        when(mockParameterStoreService.getParameterValue(
                        ParameterPrefix.STACK, ParameterStoreParameters.MAX_JWT_TTL_UNIT))
                .thenReturn("HOURS");
        when(mockCommonLibConfigurationService.getVerifiableCredentialIssuer())
                .thenReturn("dummyFraudComponentId");
        when(mockCommonLibConfigurationService.getParameterValueByAbsoluteName(
                        "/release-flags/vc-expiry-removed"))
                .thenReturn("true");
        when(mockCommonLibConfigurationService.getParameterValue(
                        "release-flags/vc-contains-unique-id"))
                .thenReturn("true");
    }
}
