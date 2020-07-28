#!/bin/bash

echo "=============== Syndesis QE (rest) test suite ==============="
echo "Environment variables:"
echo "URL: ${URL}"
echo "ADMIN_USERNAME: ${ADMIN_USERNAME}"
echo "ADMIN_PASSWORD: ${ADMIN_PASSWORD}"
echo "NAMESPACE: ${NAMESPACE}"
echo "TAGS: ${TAGS}"
echo "CREDENTIALS_URL: ${CREDENTIALS_URL}"

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

mvn clean test -P rest \
		-Dcucumber.options="--tags '""${TAGS}""'" \
		-Dsyndesis.config.openshift.url="${URL}" \
		-Dsyndesis.config.admin.username="${ADMIN_USERNAME}" \
		-Dsyndesis.config.admin.password="${ADMIN_PASSWORD}" \
		-Dsyndesis.config.openshift.namespace="${NAMESPACE}" \
		-Dsyndesis.config.openshift.namespace.lock=false \
		-Dsyndesis.config.openshift.namespace.cleanup=false \
		-Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xnoagent -Djava.compiler=NONE" \
		-Dsyndesis.config.enableTestSupport=true

STATUS=$?

echo "Status code from mvn command is: $STATUS . The test results will be stored into /test-run-results folder"

[ -d "/test-run-results" ] && rm -rf /test-run-results/cucumber /test-run-results/log || mkdir /test-run-results

mv ./rest-tests/target/cucumber \
   ./rest-tests/log/ \
   /test-run-results

exit $STATUS