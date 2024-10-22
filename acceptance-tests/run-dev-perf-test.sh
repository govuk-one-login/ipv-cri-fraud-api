#!/usr/bin/env bash

# This uses the same config as the run-local-test.sh
# Configure it for the pipeline dev environment
CONF_FILE=test-args.conf
if [ -f "$CONF_FILE" ]; then
  export $(grep -v '^#' $CONF_FILE | xargs)
fi

export BROWSER
export ENVIRONMENT

if [ "$ENVIRONMENT" != "dev" ]; then
  echo -e "\033[0;31mWarning Executing Perf test against $ENVIRONMENT\033[0m"
fi

echo -e "\033[0;31mWarning this is a performance test please close any programs and save work you do not want to loose.\033[0m"
echo -e "\033[0;31mPlease ensure your computer has 16gb of currently FREE memory\033[0m"
echo -e "\033[0;31mPlease ensure your computer not being used for this test to valid\033[0m"
read -p "Press <enter> to continue" I_UNDER_STAND_I_MAY_LOOSE_WORK
echo -e "\033[1;33mTest Running (Expected run-time 20mins~) \033[0m"

export coreStubUrl=$CORE_STUB_URL
export coreStubUsername=$CORE_STUB_USERNAME
export coreStubPassword=$CORE_STUB_PASSWORD
export orchestratorStubUrl=$ORCHESTRATOR_URL
export API_GATEWAY_ID_PRIVATE=$API_GATEWAY_ID_PRIVATE
export API_GATEWAY_ID_PUBLIC=$API_GATEWAY_ID_PUBLIC

###### Run tests
seq 16 | parallel --progress -j8 -n0 ./gradlew cucumber -P tags=${TAG}

echo -e "\033[1;33mTest Complete\033[0m"
