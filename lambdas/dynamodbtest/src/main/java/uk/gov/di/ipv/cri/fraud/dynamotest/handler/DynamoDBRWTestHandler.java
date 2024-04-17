package uk.gov.di.ipv.cri.fraud.dynamotest.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.exception.AccessTokenExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.AuthorizationCodeExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.SessionNotFoundException;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.ListUtil;
import uk.gov.di.ipv.cri.fraud.dynamotest.domain.DBTestSessionItem;
import uk.gov.di.ipv.cri.fraud.dynamotest.result.TestResult;
import uk.gov.di.ipv.cri.fraud.dynamotest.service.DBTestSessionService;
import uk.gov.di.ipv.cri.fraud.library.service.ClientFactoryService;
import uk.gov.di.ipv.cri.fraud.library.util.SleepHelper;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DynamoDBRWTestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // We need this first and static for it to be created as soon as possible during function init
    private static final long FUNCTION_INIT_START_TIME_MILLISECONDS = System.currentTimeMillis();

    private static final Logger LOGGER = LogManager.getLogger();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEST_TABLE_NAME = System.getenv("DynamoDBTestTableName");

    private static final long SESSION_EXPIRY_TTL = TimeUnit.HOURS.toMillis(1);

    private ConfigurationService commonLibConfigurationService = new ConfigurationService();
    private DBTestSessionService dbTestSessionService = createSessionService();
    private SleepHelper sleepHelper = new SleepHelper(10000L);

    private static final String ROW_FORMAT =
            "\"%s\" : { \"Id\" : \"%s\", \"Iterations\" : \"%s\", \"Delay\" : \"%s\", \"Passed\" : \"%s\", \"Failed\" : \"%s\" }";

    private long functionInitMetricLatchedValue = 0;
    private boolean functionInitMetricCaptured = false;

    public DynamoDBRWTestHandler() {
        // Runtime/SnapStart function init duration
        functionInitMetricLatchedValue =
                System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {

        try {
            LOGGER.info(
                    "Initiating lambda {} version {}",
                    context.getFunctionName(),
                    context.getFunctionVersion());

            if (!functionInitMetricCaptured) {
                LOGGER.info("Lambda function init duration {}ms", functionInitMetricLatchedValue);
                functionInitMetricCaptured = true;
            }

            // Lambda Lifetime
            long runTimeDuration =
                    System.currentTimeMillis() - FUNCTION_INIT_START_TIME_MILLISECONDS;
            Duration duration = Duration.of(runTimeDuration, ChronoUnit.MILLIS);
            String formattedDuration =
                    String.format(
                            "%d:%02d:%02d",
                            duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
            LOGGER.info(
                    "Lambda {}, Lifetime duration {}, {}ms",
                    context.getFunctionName(),
                    formattedDuration,
                    runTimeDuration);

            LOGGER.info("Table : {}", TEST_TABLE_NAME);

            Map<String, String> input =
                    objectMapper.readValue(apiGatewayProxyRequestEvent.getBody(), Map.class);

            /*
            Template event type - apigateway-aws-proxy
            Set json as the body: "{\"example\" : \"value\" }", fully escaped
            { \"id\": \"1\", \"iterations\": \"5000\", \"delayMs\": \"0\", \"threads\": \"4\"}
            */

            int id = Integer.parseInt(input.get("id"));
            int iterations = Integer.parseInt(input.get("iterations"));
            int delayMs = Integer.parseInt(input.get("delayMs"));
            int threads = Integer.parseInt(input.get("threads"));

            LOGGER.info(
                    "Test Config : id {}, iterations {}, delayMs {}, threads {}",
                    id,
                    iterations,
                    delayMs,
                    threads);

            TestResult[] testResults = doTestRun(id, iterations, delayMs, threads);

            StringBuilder json = new StringBuilder();
            json.append("{");
            for (TestResult testResult : testResults) {
                json.append(
                        String.format(
                                ROW_FORMAT,
                                testResult.testName(),
                                testResult.id(),
                                testResult.iterations(),
                                testResult.delayMS(),
                                testResult.passes(),
                                testResult.fails()));
                json.append(",");
            }
            // Remove last ","
            json.setLength(json.length() - 1);
            json.append("}");

            String message = json.toString();
            LOGGER.info(message);

        } catch (Exception e) {

            LOGGER.error(e);

            throw new RuntimeException(e);
        }

        return null;
    }

    private TestResult[] doTestRun(int testId, int maxIterations, int delayMs, int threads)
            throws ExecutionException, InterruptedException {

        // Result arrays
        int[] iterationsPassedSession = new int[maxIterations];
        int[] iterationsFailedSession = new int[maxIterations];

        int[] iterationsPassedCheck = new int[maxIterations];
        int[] iterationsFailedCheck = new int[maxIterations];

        int[] iterationsPassedAuth = new int[maxIterations];
        int[] iterationsFailedAuth = new int[maxIterations];

        int[] iterationsPassedToken = new int[maxIterations];
        int[] iterationsFailedToken = new int[maxIterations];

        int[] iterationsPassedIssueCred = new int[maxIterations];
        int[] iterationsFailedIssueCred = new int[maxIterations];

        // Available threads/cores is based on lambda memory
        // Run singleIteration with the following thread concurrency
        final int parallelTasks = threads > 0 ? threads : Thread.activeCount();
        ForkJoinPool customThreadPool = new ForkJoinPool(parallelTasks);
        IntStream intStream = IntStream.range(0, maxIterations);

        LOGGER.info("Launching using {} threads", threads);

        customThreadPool
                .submit(
                        () ->
                                intStream
                                        .parallel()
                                        .forEach(
                                                i ->
                                                        singleIteration(
                                                                i,
                                                                delayMs,
                                                                iterationsPassedSession,
                                                                iterationsFailedSession,
                                                                iterationsPassedCheck,
                                                                iterationsFailedCheck,
                                                                iterationsPassedAuth,
                                                                iterationsFailedAuth,
                                                                iterationsPassedToken,
                                                                iterationsFailedToken,
                                                                iterationsPassedIssueCred,
                                                                iterationsFailedIssueCred)))
                .get(); // Wait until completion

        LOGGER.info("Finished - now collating results");

        int iterationsPassedSessionTotal = Arrays.stream(iterationsPassedSession).sum();
        int iterationsFailedSessionTotal = Arrays.stream(iterationsFailedSession).sum();

        TestResult session =
                new TestResult(
                        testId,
                        "Session",
                        maxIterations,
                        delayMs,
                        iterationsPassedSessionTotal,
                        iterationsFailedSessionTotal);

        int iterationsPassedCheckTotal = Arrays.stream(iterationsPassedCheck).sum();
        int iterationsFailedCheckTotal = Arrays.stream(iterationsFailedCheck).sum();

        TestResult check =
                new TestResult(
                        testId,
                        "Check",
                        maxIterations,
                        delayMs,
                        iterationsPassedCheckTotal,
                        iterationsFailedCheckTotal);

        int iterationsPassedAuthTotal = Arrays.stream(iterationsPassedAuth).sum();
        int iterationsFailedAuthTotal = Arrays.stream(iterationsFailedAuth).sum();
        TestResult auth =
                new TestResult(
                        testId,
                        "Auth",
                        maxIterations,
                        delayMs,
                        iterationsPassedAuthTotal,
                        iterationsFailedAuthTotal);

        int iterationsPassedTokenTotal = Arrays.stream(iterationsPassedToken).sum();
        int iterationsFailedTokenTotal = Arrays.stream(iterationsFailedToken).sum();
        TestResult token =
                new TestResult(
                        testId,
                        "Token",
                        maxIterations,
                        delayMs,
                        iterationsPassedTokenTotal,
                        iterationsFailedTokenTotal);

        int iterationsPassedIssueCredTotal = Arrays.stream(iterationsPassedIssueCred).sum();
        int iterationsFailedIssueCredTotal = Arrays.stream(iterationsFailedIssueCred).sum();
        TestResult issueCred =
                new TestResult(
                        testId,
                        "IssueCred",
                        maxIterations,
                        delayMs,
                        iterationsPassedIssueCredTotal,
                        iterationsFailedIssueCredTotal);

        LOGGER.info("Results collated");

        return new TestResult[] {session, check, auth, token, issueCred};
    }

    private void singleIteration(
            int index,
            int delayMs,
            int[] iterationsPassedSession,
            int[] iterationsFailedSession,
            int[] iterationsPassedCheck,
            int[] iterationsFailedCheck,
            int[] iterationsPassedAuth,
            int[] iterationsFailedAuth,
            int[] iterationsPassedToken,
            int[] iterationsFailedToken,
            int[] iterationsPassedIssueCred,
            int[] iterationsFailedIssueCred) {

        //////////////
        // Session  //
        //////////////
        UUID sessionUUID = UUID.randomUUID();
        String sessionStringUUID = sessionUUID.toString();

        DBTestSessionItem dbTestSessionItem = new DBTestSessionItem();
        dbTestSessionItem.setSessionId(sessionUUID);
        dbTestSessionItem.setExpiryDate((System.currentTimeMillis() + SESSION_EXPIRY_TTL) / 1000);

        // Creating the session
        boolean sessionIdPassed = sessionLambda(dbTestSessionItem, delayMs);

        if (!sessionIdPassed) {
            iterationsFailedSession[index]++;
            return;
        }
        iterationsPassedSession[index]++;

        ///////////////////
        // Check         //
        ///////////////////
        UUID authcodeUUID = UUID.randomUUID();
        String authcodeStringUUID = authcodeUUID.toString();

        // Save and Retrieve Session and then Check Authcode (check to authcode)
        boolean checkLambdaPassed = checkLambda(sessionStringUUID, authcodeStringUUID, delayMs);

        if (!checkLambdaPassed) {
            iterationsFailedCheck[index]++;
            return;
        }
        iterationsPassedCheck[index]++;

        ////////////////////
        // AuthCode       //
        ////////////////////
        boolean authPassed = authCodeLambda(sessionStringUUID, delayMs);

        if (!authPassed) {
            iterationsFailedAuth[index]++;
            return;
        }
        iterationsPassedAuth[index]++;

        ////////////////////
        // Token          //
        ////////////////////
        AccessTokenResponse tokenResponse = createTokenResponse();

        // Save and Retrieve Session and then Check Authcode (check to authcode)
        boolean tokenPassed = tokenLambda(authcodeStringUUID, tokenResponse, delayMs);

        if (!tokenPassed) {
            iterationsFailedToken[index]++;
            return;
        }
        iterationsPassedToken[index]++;

        ///////////////
        // issueCred //
        ///////////////
        boolean issueCredPassed = issueCred(tokenResponse, delayMs);

        if (!issueCredPassed) {
            iterationsFailedIssueCred[index]++;
            return;
        }
        iterationsPassedIssueCred[index]++;
    }

    private boolean sessionLambda(DBTestSessionItem dbTestSessionItem, int delayMs) {

        dbTestSessionService.updateSession(dbTestSessionItem);

        // redirect to check
        if (delayMs > 0) {
            sleepHelper.busyWaitMilliseconds(delayMs);
        }

        return true;
    }

    private boolean checkLambda(String sessionStringUUID, String authcodeStringUUID, int delayMs) {

        // Retrieve Session
        DBTestSessionItem dbTestSessionItem = dbTestSessionService.getSession(sessionStringUUID);

        if (null == dbTestSessionItem) {
            return false;
        }

        // Simulated CRI Processing...
        if (delayMs > 0) {
            sleepHelper.busyWaitMilliseconds(delayMs);
        }

        // Save Session - after CRI processing
        dbTestSessionItem.setAuthorizationCode(authcodeStringUUID);
        dbTestSessionItem.setAuthorizationCodeExpiryDate(
                commonLibConfigurationService.getAuthorizationCodeExpirationEpoch());
        dbTestSessionService.updateSession(dbTestSessionItem);

        // Redirect delay to auth lambda
        if (delayMs > 0) {
            sleepHelper.busyWaitMilliseconds(delayMs);
        }

        return true;
    }

    private boolean authCodeLambda(String sessionStringUUID, int delayMs) {

        // AuthCode lambda
        DBTestSessionItem fetchedSession = dbTestSessionService.getSession(sessionStringUUID);

        if (null == fetchedSession) {
            return false;
        }

        if (null == fetchedSession.getAuthorizationCode()) {
            return false;
        }

        // Redirect delay to token
        if (delayMs > 0) {
            sleepHelper.busyWaitMilliseconds(delayMs);
        }

        return true;
    }

    private boolean tokenLambda(
            String authcodeStringUUID, AccessTokenResponse tokenResponse, int delayMs) {

        // Retrieve Session by authcode
        DBTestSessionItem dbTestSessionItem = null;

        try {
            dbTestSessionItem =
                    dbTestSessionService.getSessionByAuthorisationCode(authcodeStringUUID);
        } catch (SessionNotFoundException | AuthorizationCodeExpiredException ignored) {
            // Ignored
            // AuthorizationCodeExpiredException "authorization code expired" occurs here if
            // getAuthorizationCodeExpiryDate is returned as 0 (i.e not updated)
        }

        // Session there?
        if (null == dbTestSessionItem) {
            return false;
        }

        // Was Session updated in time?
        if (null == dbTestSessionItem.getAuthorizationCode()) {
            return false;
        }

        // Not updated
        if (dbTestSessionItem.getAuthorizationCodeExpiryDate() == 0) {
            return false;
        }

        // Save Session with the new access token
        dbTestSessionItem.setAccessToken(
                tokenResponse.getTokens().getBearerAccessToken().toAuthorizationHeader());
        dbTestSessionItem.setAccessTokenExpiryDate(
                commonLibConfigurationService.getBearerAccessTokenExpirationEpoch());
        dbTestSessionItem.setAuthorizationCode(null);

        dbTestSessionService.updateSession(dbTestSessionItem);

        // Core delay to issue cred
        if (delayMs > 0) {
            sleepHelper.busyWaitMilliseconds(delayMs);
        }

        return true;
    }

    private boolean issueCred(AccessTokenResponse tokenResponse, int delayMs) {
        // Retrieve Session by token
        var accessToken = tokenResponse.getTokens().getAccessToken();
        DBTestSessionItem dbTestSessionItem = null;

        try {
            dbTestSessionItem = dbTestSessionService.getSessionByAccessToken(accessToken);
        } catch (SessionNotFoundException | AccessTokenExpiredException ignored) {
            // Ignored
            // AccessTokenExpiredException "access code expired" occurs here if
            // getAccessTokenExpiryDate is
            // returned as 0 (i.e not updated)
        }

        // Session there?
        if (null == dbTestSessionItem) {
            return false;
        }

        // Was Session updated in time?
        if (null == dbTestSessionItem.getAccessToken()) {
            return false;
        }

        // Not updated
        if (dbTestSessionItem.getAccessTokenExpiryDate() == 0) {
            return false;
        }

        // VC generation and return
        if (delayMs > 0) {
            sleepHelper.busyWaitMilliseconds(delayMs);
        }

        return true;
    }

    private DBTestSessionService createSessionService() {

        ClientFactoryService clientFactoryService = new ClientFactoryService();

        DataStore<DBTestSessionItem> dataStore =
                new DataStore<>(
                        TEST_TABLE_NAME,
                        DBTestSessionItem.class,
                        clientFactoryService.getDynamoDbEnhancedClient());

        return new DBTestSessionService(
                dataStore, commonLibConfigurationService, Clock.systemUTC(), new ListUtil());
    }

    public AccessTokenResponse createTokenResponse() {
        AccessToken accessToken = new BearerAccessToken(SESSION_EXPIRY_TTL, Scope.parse("Scope"));
        return (new AccessTokenResponse(new Tokens(accessToken, (RefreshToken) null)))
                .toSuccessResponse();
    }
}
