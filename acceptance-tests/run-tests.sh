#!/usr/bin/env bash

set -eu

REPORT_DIR="${TEST_REPORT_DIR:=$PWD}"

export BROWSER="${BROWSER:-chrome-headless}"
export ENVIRONMENT="${ENVIRONMENT:-build}"
export NO_CHROME_SANDBOX=true
export STACK_NAME="${CFN_StackName:-local}"
export JOURNEY_TAG=$(aws ssm get-parameter --name "/tests/${STACK_NAME}/TestTag" | jq -r ".Parameter.Value")

PARAMETERS_NAMES=(coreStubPassword coreStubUrl coreStubUsername passportCriUrl apiBaseUrl orchestratorStubUrl)
tLen=${#PARAMETERS_NAMES[@]}
 for (( i=0; i<${tLen}; i++ ));
do
  echo "/tests/$STACK_NAME/${PARAMETERS_NAMES[$i]}"
  PARAMETER=$(aws ssm get-parameter --name "/tests/$STACK_NAME/${PARAMETERS_NAMES[$i]}" --region eu-west-2)
  VALUE=$(echo "$PARAMETER" | jq '.Parameter.Value')
  NAME=$(echo "$PARAMETER" | jq '.Parameter.Name' | cut -d "/" -f4 | sed 's/.$//')

  eval $(echo "export ${NAME}=${VALUE}")
done

pushd /home/gradle
gradle cucumber -P tags=${JOURNEY_TAG}
popd

cp -r /home/gradle/build/test-results "$REPORT_DIR"
