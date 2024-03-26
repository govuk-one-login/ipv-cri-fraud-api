#!/usr/bin/env bash

##### Load previous answers from config file
CONF_FILE=test-args.conf
if [ -f "$CONF_FILE" ]; then
  export $(grep -v '^#' $CONF_FILE | xargs)
fi

if [ -z "$BROWSER" ];
then
  BROWSER="chrome-headless"
  echo "Browser not previously chosen defaulting to $BROWSER";
fi

##### Ask fundamental test questions
read -p "What browser do you want to use? [previous=$BROWSER] " BROWSER_NEW

read -p "What environment are you running against? [previous=$ENVIRONMENT] " ENVIRONMENT_NEW

read -p "Are you using a local stub? [previous=$LOCAL] " LOCAL_NEW

read -p "Are you including E2E tests? [previous=$E2E] " E2E_NEW

read -p "Are you including Backend tests? [previous=$BACKEND] " BACKEND_NEW

read -p "Which tag would you like to run (include the @ in the name)? [previous=$TAG] " TAG_NEW

##### Update env vars with answers if set
BROWSER=$([[ -z "$BROWSER_NEW" ]] && echo "$BROWSER" || echo "$BROWSER_NEW")
ENVIRONMENT=$([[ -z "$ENVIRONMENT_NEW" ]] && echo "$ENVIRONMENT" || echo "$ENVIRONMENT_NEW")
LOCAL=$([ -z "$LOCAL_NEW" ] && echo "$LOCAL" || echo "$LOCAL_NEW")
E2E=$([ -z "$E2E_NEW" ] && echo "$E2E" || echo "$E2E_NEW")
BACKEND=$([ -z "$BACKEND_NEW" ] && echo "$BACKEND" || echo "$BACKEND_NEW")
TAG=$([ -z "$TAG_NEW" ] && echo "$TAG" || echo "$TAG_NEW")

##### Export browser for test run
export BROWSER=${BROWSER}

##### Export environment for test run
export ENVIRONMENT=${ENVIRONMENT}

##### Get stub details if not running on local
if [[ "${LOCAL}" =~ "no" ]]; then
  read -p "Enter the stub url (without protocol http, https etc)? [previous=$CORE_STUB_URL] " CORE_STUB_URL_NEW

  read -p "Enter the stub username? [previous=$CORE_STUB_USERNAME] " CORE_STUB_USERNAME_NEW

  read -p "Enter the stub password? [previous=$CORE_STUB_PASSWORD] " CORE_STUB_PASSWORD_NEW

  CORE_STUB_URL=$([ -z "$CORE_STUB_URL_NEW" ] && echo "$CORE_STUB_URL" || echo "$CORE_STUB_URL_NEW")
  CORE_STUB_USERNAME=$([ -z "$CORE_STUB_USERNAME_NEW" ] && echo "$CORE_STUB_USERNAME" || echo "$CORE_STUB_USERNAME_NEW")
  CORE_STUB_PASSWORD=$([ -z "$CORE_STUB_PASSWORD_NEW" ] && echo "$CORE_STUB_PASSWORD" || echo "$CORE_STUB_PASSWORD_NEW")

  export coreStubUrl=$CORE_STUB_URL
  export coreStubUsername=$CORE_STUB_USERNAME
  export coreStubPassword=$CORE_STUB_PASSWORD

  else

  CORE_STUB_URL=localhost:8085
  export coreStubUrl=$CORE_STUB_URL

fi

###### Get Orchestrator details if running E2E
if [[ "${E2E}" =~ "yes" ]]; then
  read -p "Enter the orchestrator url? [previous=$ORCHESTRATOR_URL] " ORCHESTRATOR_URL_NEW

  ORCHESTRATOR_URL=$([ -z "$ORCHESTRATOR_URL_NEW" ] && echo "$ORCHESTRATOR_URL" || echo "$ORCHESTRATOR_URL_NEW")
  export orchestratorStubUrl=$ORCHESTRATOR_URL

fi

###### Get gateway details if running backend
if [[ "${BACKEND}" =~ "yes" ]]; then
  read -p "Enter the private gateway id? [previous=$API_GATEWAY_ID_PRIVATE] " API_GATEWAY_ID_PRIVATE_NEW

  API_GATEWAY_ID_PRIVATE=$([ -z "$API_GATEWAY_ID_PRIVATE_NEW" ] && echo "$API_GATEWAY_ID_PRIVATE" || echo "$API_GATEWAY_ID_PRIVATE_NEW")
  export API_GATEWAY_ID_PRIVATE=$API_GATEWAY_ID_PRIVATE

  read -p "Enter the public gateway id? [previous=$API_GATEWAY_ID_PUBLIC] " API_GATEWAY_ID_PUBLIC_NEW

  API_GATEWAY_ID_PUBLIC=$([ -z "$API_GATEWAY_ID_PUBLIC_NEW" ] && echo "$API_GATEWAY_ID_PUBLIC" || echo "$API_GATEWAY_ID_PUBLIC_NEW")
  export API_GATEWAY_ID_PUBLIC=$API_GATEWAY_ID_PUBLIC
fi

###### Remove previous config and set with new values
if [ -f "$CONF_FILE" ]; then
  rm test-args.conf
fi

echo "BROWSER=${BROWSER}" >> test-args.conf
echo "ENVIRONMENT=${ENVIRONMENT}" >> test-args.conf
echo "CORE_STUB_URL=${CORE_STUB_URL}" >> test-args.conf
echo "CORE_STUB_USERNAME=${CORE_STUB_USERNAME}" >> test-args.conf
echo "CORE_STUB_PASSWORD=${CORE_STUB_PASSWORD}" >> test-args.conf
echo "ORCHESTRATOR_URL=${ORCHESTRATOR_URL}" >> test-args.conf
echo "LOCAL=${LOCAL}" >> test-args.conf
echo "E2E=${E2E}" >> test-args.conf
echo "BACKEND=${BACKEND}" >> test-args.conf
echo "API_GATEWAY_ID_PRIVATE=${API_GATEWAY_ID_PRIVATE}" >> test-args.conf
echo "API_GATEWAY_ID_PUBLIC=${API_GATEWAY_ID_PUBLIC}" >> test-args.conf
echo "TAG=${TAG}" >> test-args.conf


###### Run tests
./gradlew clean cucumber -P tags=${TAG}
