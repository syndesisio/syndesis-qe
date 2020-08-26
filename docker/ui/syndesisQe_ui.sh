#!/bin/bash

echo "=============== Syndesis QE (ui) test suite ==============="
echo "Environment variables:"
echo "URL: ${URL}"
echo "ADMIN_USERNAME: ${ADMIN_USERNAME}"
echo "ADMIN_PASSWORD: ${ADMIN_PASSWORD}"
echo "UI_USERNAME: ${UI_USERNAME}"
echo "UI_PASSWORD: ${UI_PASSWORD}"
echo "NAMESPACE: ${NAMESPACE}"
echo "TAGS: ${TAGS}"
echo "CREDENTIALS_URL: ${CREDENTIALS_URL}"
echo "USER: $(whoami)"

if [[ -z "${CREDENTIALS_URL}" ]]; then
  echo "URL for credentials.json with 3rd party services credentials was not set in CREDENTIALS_URL env. Tests that use 3rd party services will fail."
else
  if [[ ${CREDENTIALS_URL} == *"credentials.json" ]]; then
    echo "URL for credentials.json exist. That credentials will be used."
    wget -N "${CREDENTIALS_URL}" --no-check-certificate
  else
    echo "The URL set in CREDENTIALS_URL doesn't contain credentials.json. The default credentials.json will be used. Tests that use 3rd party services will fail."
  fi
fi

if [[ ! -z "${UI_URL}" ]] ; then
  echo "The Syndesis UI URL was specify. Please make sure that you have correctly set UI IP in your /etc/hosts since the container uses hosts \
        from the local machine where it is running!"
  echo "UI_URL: ${UI_URL}"
  echo "====== /etc/hosts: ${UI_IP} ======"
  sudo cat /etc/hosts
  echo "=================================="
  UI_URL_CONFIG="-Dsyndesis.config.ui.url="${UI_URL}""
else
    echo "The Syndesis UI URL was not specify. The test suite suppose that the Syndesis UI URL is in default format (when the user doesn't \
          specify URL during the Syndesis install, e.g. https://syndesis-<namespace>.apps.<cluster>). The OAuth tests will not work!"
 fi

Xvfb :99 -ac &

./mvnw clean verify -P ui \
		-Dtags="${TAGS}" \
		-Dsyndesis.config.openshift.url="${URL}" \
		-Dsyndesis.config.admin.username="${ADMIN_USERNAME}" \
		-Dsyndesis.config.admin.password="${ADMIN_PASSWORD}" \
		-Dsyndesis.config.ui.username="${UI_USERNAME}" \
		-Dsyndesis.config.ui.password="${UI_PASSWORD}" \
		-Dsyndesis.config.openshift.namespace="${NAMESPACE}" \
		-Dsyndesis.config.openshift.namespace.lock=false \
		-Dsyndesis.config.openshift.namespace.cleanup=false \
	    $(echo ${UI_URL_CONFIG}) \
		-Dmaven.failsafe.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xnoagent -Djava.compiler=NONE" \
		-Dsyndesis.config.enableTestSupport=true \
		-Dsyndesis.config.ui.browser=firefox

STATUS=$?

echo "Status code from mvn command is: $STATUS . The test results will be stored into /test-run-results folder"

[ -d "/test-run-results" ] && sudo rm -rf /test-run-results/cucumber /test-run-results/log || sudo mkdir /test-run-results

sudo mv ./ui-tests/target/cucumber \
   ./ui-tests/log/ \
   /test-run-results

exit $STATUS