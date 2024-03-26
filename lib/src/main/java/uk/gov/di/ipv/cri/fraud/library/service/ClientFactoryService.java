package uk.gov.di.ipv.cri.fraud.library.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.lambda.powertools.parameters.ParamManager;
import software.amazon.lambda.powertools.parameters.SSMProvider;
import software.amazon.lambda.powertools.parameters.SecretsProvider;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

// See https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/http-configuration.html
// If an explicit client choice is not made the SDK default will be used *if it is the only one* in
// the
// classpath
// If there is more than one of the same HTTP client type a conflict will occur for these clients.
// To prevent this, the exact http clients are now being specified for each client.
// DataStore (Dynamo) from CRI-lib his has this already done in CRI lib.
public class ClientFactoryService {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Region awsRegion;

    // https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/best-practices.html#bestpractice1
    private static final SdkHttpClient sdkHttpClient = UrlConnectionHttpClient.create();
    private static final EnvironmentVariableCredentialsProvider
            environmentVariableCredentialsProvider =
                    EnvironmentVariableCredentialsProvider.create();

    public ClientFactoryService() {
        awsRegion = Region.of(System.getenv("AWS_REGION"));
    }

    public ClientFactoryService(Region awsRegion) {
        this.awsRegion = awsRegion;
    }

    public KmsClient getKMSClient() {
        return KmsClient.builder()
                .region(awsRegion)
                .httpClient(sdkHttpClient)
                .credentialsProvider(environmentVariableCredentialsProvider)
                .build();
    }

    public SqsClient getSqsClient() {
        return SqsClient.builder()
                .region(awsRegion)
                .httpClient(sdkHttpClient)
                .credentialsProvider(environmentVariableCredentialsProvider)
                .build();
    }

    public DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
        DynamoDbClient dynamoDbClient =
                DynamoDbClient.builder()
                        .region(awsRegion)
                        .httpClient(sdkHttpClient)
                        .credentialsProvider(environmentVariableCredentialsProvider)
                        .build();

        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    // ThreadLocalRandom not used cryptographically here
    @java.lang.SuppressWarnings("java:S2245")
    public SSMProvider getSSMProvider() {
        SsmClient ssmClient =
                SsmClient.builder()
                        .region(awsRegion)
                        .httpClient(sdkHttpClient)
                        .credentialsProvider(environmentVariableCredentialsProvider)
                        .build();

        // A random cache age between 5-15 minutes (in seconds)
        // Avoids multiple scaling lambdas expiring their caches at the exact same time
        int maxCacheAge = ThreadLocalRandom.current().nextInt(900 - 300 + 1) + 300;

        LOGGER.info("PowerTools SSMProvider defaultMaxAge selected as {} seconds", maxCacheAge);

        return ParamManager.getSsmProvider(ssmClient)
                .defaultMaxAge(maxCacheAge, ChronoUnit.SECONDS);
    }

    // ThreadLocalRandom not used cryptographically here
    @java.lang.SuppressWarnings("java:S2245")
    public SecretsProvider getSecretsProvider() {

        // A random cache age between 5-15 minutes (in seconds)
        // Avoids multiple scaling lambdas expiring their caches at the exact same time
        int maxCacheAge = ThreadLocalRandom.current().nextInt(900 - 300 + 1) + 300;

        LOGGER.info("PowerTools SecretsProvider defaultMaxAge selected as {} seconds", maxCacheAge);

        return ParamManager.getSecretsProvider(getSecretsManagerClient())
                .defaultMaxAge(maxCacheAge, ChronoUnit.SECONDS);
    }

    public SecretsManagerClient getSecretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(awsRegion)
                .httpClient(sdkHttpClient)
                .credentialsProvider(environmentVariableCredentialsProvider)
                .build();
    }
}
