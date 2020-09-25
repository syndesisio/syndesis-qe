#!/bin/bash
ONE_USER=false
if [ -z "${UI_USERNAME}" ]; then
	ONE_USER=true
	UI_USERNAME="${ADMIN_USERNAME}"
	UI_PASSWORD="${ADMIN_PASSWORD}"
elif [ -z "${ADMIN_USERNAME}" ]; then
	ONE_USER=true
	ADMIN_USERNAME="${UI_USERNAME}"
	ADMIN_PASSWORD="${UI_PASSWORD}"
fi

PROFILE="rest,ui"
if [ ! "${MODULE}" = "all" ]; then
	PROFILE="${MODULE}"
fi

echo "=============== Syndesis QE test suite ==============="
echo "Environment variables:"
echo "URL: ${URL}"
echo "ADMIN_USERNAME: ${ADMIN_USERNAME}"
echo "ADMIN_PASSWORD: ${ADMIN_PASSWORD}"
echo "UI_USERNAME: ${UI_USERNAME}"
echo "UI_PASSWORD: ${UI_PASSWORD}"
echo "NAMESPACE: ${NAMESPACE}"
echo "TAGS: ${TAGS}"
echo "CREDENTIALS_URL: ${CREDENTIALS_URL}"
echo "ONE_USER: ${ONE_USER}"
echo "MODULE: ${PROFILE}"
echo "MODE: ${MODE}"
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

if [ -n "${UI_URL}" ]; then
  echo "The Syndesis UI URL was specified. Please make sure that you have correctly set UI IP in your /etc/hosts since the container uses hosts \
        from the local machine where it is running!"
  echo "UI_URL: ${UI_URL}"
  echo "====== /etc/hosts: ${UI_IP} ======"
  sudo cat /etc/hosts
  echo "=================================="
else
    echo "The Syndesis UI URL was not specified. The test suite suppose that the Syndesis UI URL is in default format (when the user doesn't specify URL during the Syndesis install, e.g. https://syndesis-<namespace>.apps.<cluster>). The OAuth tests will not work!"
fi

IS_DELOREAN=false
# Full mode means install & test
if [ "${MODE,,}" = "full" ]; then
	echo "Using full mode, deploying Fuse Online"

	oc login  --insecure-skip-tls-verify=true -u "${ADMIN_USERNAME}" -p "${ADMIN_PASSWORD}" "${URL}"
	oc delete project ${NAMESPACE} || :
	until ! oc project "${NAMESPACE}"; do echo "Project still exists"; sleep 5; done

	pushd /home/seluser/fuse-online-install

	oc login  --insecure-skip-tls-verify=true -u "${UI_USERNAME}" -p "${UI_PASSWORD}" "${URL}"
	oc new-project ${NAMESPACE}
	oc login  --insecure-skip-tls-verify=true -u "${ADMIN_USERNAME}" -p "${ADMIN_PASSWORD}" "${URL}"
	oc project ${NAMESPACE}
	./install_ocp.sh --setup
	./install_ocp.sh --grant ${UI_USERNAME}

	oc login  --insecure-skip-tls-verify=true -u "${UI_USERNAME}" -p "${UI_PASSWORD}" "${URL}"
	oc project ${NAMESPACE}
	if [ -n "${PULL_SECRET}" ]; then
		base64 -d <<< "${PULL_SECRET}" > /tmp/secret
		oc create secret generic syndesis-pull-secret --from-file /tmp/secret
	fi
	./install_ocp.sh

	oc patch syndesis app -p '{"spec": {"addons": {"todo": {"enabled": true}}}}' --type=merge
	oc set env dc/syndesis-operator TEST_SUPPORT=true

	echo "Waiting until all pods are ready"
	sleep 60
	until [[ ! "$(oc get pods -l syndesis.io/component=syndesis-server 2>&1 || echo No resources)" == "No resources"* ]]; do sleep 5; done
	until [[ ! "$(oc get pods -l syndesis.io/component -o jsonpath='{.items[*].status.containerStatuses[0].ready}' 2>/dev/null || echo false)" == *"false"* ]]; do sleep 5; done
	popd
elif [ "${MODE,,}" = "delorean" ]; then
	IS_DELOREAN=true
fi

Xvfb :99 -ac &

cat <<EOF > ./test.properties
syndesis.config.ui.username=${UI_USERNAME}
syndesis.config.ui.password=${UI_PASSWORD}
syndesis.config.openshift.namespace=${NAMESPACE}
syndesis.config.openshift.namespace.lock=false
syndesis.config.openshift.namespace.cleanup=false
syndesis.config.ui.browser=firefox
syndesis.config.single.user=${ONE_USER}
EOF

if [ -n "${UI_URL}" ]; then
	echo "syndesis.config.ui.url=${UI_URL}" >> ./test.properties
fi

if [ "${IS_DELOREAN}" = "true" ]; then
	echo "syndesis.config.environment.delorean=true" >> ./test.properties
else
	cat <<EOF >> ./test.properties
syndesis.config.openshift.url=${URL}
syndesis.config.admin.username=${ADMIN_USERNAME}
syndesis.config.admin.password=${ADMIN_PASSWORD}
syndesis.config.enableTestSupport=true
EOF
fi

./mvnw clean test -P "${PROFILE}" -Dcucumber.options="--tags '""${TAGS}""'" -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xnoagent -Djava.compiler=NONE"

STATUS=$?

echo "Status code from mvn command is: $STATUS . The test results will be stored into /test-run-results folder"

[ -d "/test-run-results" ] && sudo rm -rf /test-run-results/* || sudo mkdir /test-run-results

while read -r FILE; do sudo mkdir -p /test-run-results/$(dirname $FILE); sudo cp $FILE /test-run-results/$(dirname $FILE); done <<< "$(find * -type f -name "*.log")"
while read -r DIR; do sudo mkdir -p /test-run-results/$DIR; sudo cp -r $DIR/* /test-run-results/$DIR; done <<< "$(find * -maxdepth 2 -type d -wholename "*target/cucumber*")"

exit $STATUS
