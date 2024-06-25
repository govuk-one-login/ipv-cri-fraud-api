# di-ipv-cri-fraud-test

This folder has be created as a central location for any work related to Fraud CRI testing.

## SDKMan
This project has an `.sdkmanrc` file

Install SDKMan via the instructions on `https://sdkman.io/install`

For auto-switching between JDK versions, edit your `~/.sdkman/etc/config` and set `sdkman_auto_env=true`

Then use sdkman to install Java JDK listed in this projects `.sdkmanrc`
e.g `sdk install java x.y.z-amzn`

Restart your terminal

## Gradle

Gradle 8 is used on this project

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

If running local test using `./run-local-tests.sh` these are the recommended parameters:

`What environment are you running against? dev`

`Are your running locally? yes`

`Are you including E2E tests? no`

`Are you including Backend tests? no`

`Which tag would you like to run (include the @ in the name)? @build-fraud`
