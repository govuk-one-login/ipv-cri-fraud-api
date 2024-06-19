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

## Pre-Commit Checking / Verification

There is a `.pre-commit-config.yaml` configuration setup in this repo, this uses [pre-commit](https://pre-commit.com/) to verify your commit before actually commiting, it runs the following checks:

* Check Json files for formatting issues
* Fixes end of file issues (it will auto correct if it spots an issue - you will need to run the git commit again after it has fixed the issue)
* It automatically removes trailing whitespaces (again will need to run commit again after it detects and fixes the issue)
* Detects aws credentials or private keys accidentally added to the repo
* runs cloud formation linter and detects issues
* runs checkov and checks for any issues.

### Dependency Installation
To use this locally you will first need to install the dependencies, this can be done in 2 ways:

#### Method 1 - Python pip

Run the following in a terminal:

```
sudo -H pip3 install checkov pre-commit cfn-lint
```

this should work across platforms

#### Method 2 - Brew

If you have brew installed please run the following:

```
brew install pre-commit ;\
brew install cfn-lint ;\
brew install checkov
```

### Post Installation Configuration
once installed run:
```
pre-commit install
```

To update the various versions of the pre-commit plugins, this can be done by running:

```
pre-commit autoupdate && pre-commit install
```

This will install / configure the pre-commit git hooks,  if it detects an issue while committing it will produce an output like the following:

```
 git commit -a
check json...........................................(no files to check)Skipped
fix end of files.........................................................Passed
trim trailing whitespace.................................................Passed
detect aws credentials...................................................Passed
detect private key.......................................................Passed
AWS CloudFormation Linter................................................Failed
- hook id: cfn-python-lint
- exit code: 4

W3011 Both UpdateReplacePolicy and DeletionPolicy are needed to protect Resources/PublicHostedZone from deletion
core/deploy/dns-zones/template.yaml:20:3

Checkov..............................................(no files to check)Skipped
- hook id: checkov
```

To remove the pre-commit hooks should there be an issue
```
pre-commit uninstall
```

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
