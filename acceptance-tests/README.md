# di-ipv-cri-fraud-test

This folder has be created as a central location for any work related to Fraud CRI testing.

## Build

Build with `./gradlew`

### Run tests
When running locally the following environment variables must be set


ENVIRONMENT \
AWS_STACK_NAME \
API_GATEWAY_ID_PRIVATE \
apiBaseUrl \
coreStubUrl \
coreStubUsername \
coreStubPassword \
passportCriUrl  \
orchestratorStubUrl 


Speak to a member of the test team for these values
When running in the pipeline these will be taken from AWS

Run tests with `./gradlew cucumber -P tags=@fraud_CRI`
or if its your first time running these tests `./run-local-tests.sh` will help you setup
and run the tests
