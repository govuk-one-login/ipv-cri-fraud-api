# Digital Identity Fraud Credential Issuer
Identity and Fraud Check Credential Issuer

## Checkout submodules
> The first time you check out or clone the repository, you will need to run the following commands:
 
`git submodule update --init --recursive`

> Subsequent times you will need to run the following commands:

`git submodule update --recursive`

### Updating submodules to the latest "main" branch
> You can also update the submodules to the latest "main" branch, but this is not done automatically 
> in case there have been changes made to the shared libraries you do not yet want to track

cd into each submodule (folders are `/common-lib` and `/common-lambdas`) and run the following commands:

`git checkout main && git pull`

## Build

Build with `./gradlew`

## Deploy

### Prerequisites

See onboarding guide for instructions on how to setup the following command line interfaces (CLI)
- aws cli
- aws-vault
- sam cli
- gds cli

### Deploy to dev account

Any time you wish to deploy, run:

`aws-vault exec fraud-dev -- ./deploy.sh my-fraud-api-stack-name`

### Delete stack from dev account
> The stack name *must* be unique to you and created by you in the deploy stage above.
> Type `yes` when prompted to delete the stack and the folders in S3 bucket

The command to run is:

`aws-vault exec fraud-dev -- sam delete --config-env dev --stack-name <unique-stack-name>`

## TestData Strategy

For testing purposes, this CRI has the ability to route users requests to either
a real 3rd Party UAT instance of the service OR route users requests to an internally 
managed, stubbed version of the 3rd party service.

Routing for the above is dictated by the client ID sent to the CRI from IPVCore/stubs. For lower
environments there is an IPV core stub that is configured to for routing CRIs to 3rd party stubs and another 
IPV core stub that is configured for routing CRIs to the 3rd party UAT environment. 

For testing purposes if you wish to route to the stubbed version of the 3rd party then use the following 
core stub URL - https://cri.core.stubs.account.gov.uk/
If you wish to route to the real 3rd partys UAT instance of the service use the following 
core stub url - https://cri-3rdparty.core.stubs.account.gov.uk/

Additional details on these stubs can be found on this confluence page - 
https://govukverify.atlassian.net/wiki/spaces/OJ/pages/3147333723/Stubs+for+testing+journeys


