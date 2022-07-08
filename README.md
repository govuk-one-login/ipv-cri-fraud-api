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

Before your **first** deploy, build a sam config toml file.
> The stack name *must* be unique to you.
> Ensure you set **Parameter Environment** and **SAM configuration environment**, when asked to `dev`.
> All other defaults can be accepted by leaving them blank

The command to run is: 

`gds aws  di-ipv-cri-dev -- sam deploy -t infrastructure/lambda/template.yaml --guided`

Any time you wish to deploy, run:

`gds aws  di-ipv-cri-dev -- ./deploy.sh`

### Delete stack from dev account
> The stack name *must* be unique to you and created by you in the deploy stage above.
> The default location of sam config toml file is `./infrastructure/lambda/samconfig.toml`.
> **Remember to specify the environment name**:`dev`.
> Type `y`es when prompted to delete the stack and the folders in S3 bucket

The command to run is:

`gds aws di-ipv-cri-dev -- sam delete --config-file ./infrastructure/lambda/samconfig.toml --config-env dev --stack-name <unique-stack-name>`
