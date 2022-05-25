# Digital Identity Fraud Credential Issuer

Identity and Fraud Check Credential Issuer

## Build

Build with `./gradlew`

## Deploy

Build a sam config toml file once only by running:
`sam deploy -t infrastructure/lambda/template.yaml --guided`
Then run `gds aws <account> -- ./deploy.sh`
