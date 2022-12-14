#!/usr/bin/env bash

set -e

REPORT_DIR="${TEST_REPORT_DIR:=$PWD}"

export BROWSER="${BROWSER:-chrome-headless}"
export ENVIRONMENT="${ENVIRONMENT:-build}"
export NO_CHROME_SANDBOX=true


# Added to accommodate ssm stack
if [[ -z "${CFN_StackName}" ]]; then
  if [[ -z "${SAM_STACK_NAME}" ]]; then
    export STACK_NAME="local"
  else
    export STACK_NAME="${SAM_STACK_NAME}"
  fi
else
  export STACK_NAME="${CFN_StackName}"
fi

# Added to accommodate ssm stack
if [[ -z "${ENVIRONMENT}" ]]; then
  if [[ -z "${TEST_ENVIRONMENT}" ]]; then
    export ENVIRONMENT="build"
  else
    export ENVIRONMENT="${TEST_ENVIRONMENT}"
  fi
else
  export ENVIRONMENT="${ENVIRONMENT}"
fi

echo "ENVIRONMENT ${ENVIRONMENT}"
echo "STACK_NAME ${STACK_NAME}"

if [ "${STACK_NAME}" != "local" ]; then
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
else
  export JOURNEY_TAG="${TEST_TAG}"
fi

pushd /home/gradle
gradle cucumber -P tags=${JOURNEY_TAG}
popd

cp -r /home/gradle/build/test-results "$REPORT_DIR"
