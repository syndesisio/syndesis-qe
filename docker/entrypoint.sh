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
if [ ! "${MODULE,,}" = "all" ]; then
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
echo "RETRIES: ${RETRIES}"

echo "CATALOG_SOURCE: ${CATALOG_SOURCE}"
echo "CSV_VERSION: ${CSV_VERSION}"
echo "CSV_CHANNEL: ${CSV_CHANNEL}"

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


Xvfb :99 -ac &
if [ ! "${VNC,,}" = "false" ]; then
	/opt/bin/start-vnc.sh &
  /opt/bin/start-novnc.sh &
fi

cat <<EOF > ./test.properties
syndesis.config.ui.username=${UI_USERNAME}
syndesis.config.ui.password=${UI_PASSWORD}
syndesis.config.openshift.namespace=${NAMESPACE}
syndesis.config.openshift.namespace.lock=false
syndesis.config.ui.browser=chrome
syndesis.config.single.user=${ONE_USER}
EOF

if [ -n "${UI_URL}" ]; then
	echo "syndesis.config.ui.url=${UI_URL}" >> ./test.properties
fi

if [ "${MODE,,}" = "delorean" ]; then
	echo "syndesis.config.environment.delorean=true" >> ./test.properties
else
	cat <<EOF >> ./test.properties
syndesis.config.openshift.url=${URL}
syndesis.config.admin.username=${ADMIN_USERNAME}
syndesis.config.admin.password=${ADMIN_PASSWORD}
syndesis.config.enableTestSupport=true
EOF
fi

oc login  --insecure-skip-tls-verify=true -u "${ADMIN_USERNAME}" -p "${ADMIN_PASSWORD}" "${URL}"
# Full mode means install & test
if [ "${MODE,,}" = "full" ]; then

  if [ -z "${CSV_VERSION}" ]
  then
        echo "\$CSV_VERSION env is empty. Trying to detect CSV version from CatalogSource"
        # get all packagemanifests from the desired catalogsource, print name and CSV version for the latest channel, then grep only `fuse online` element and return only CSV version
        CSV_VERSION=$(oc get -n openshift-marketplace packagemanifests -l catalog==${CATALOG_SOURCE} -o=custom-columns=NAME:.metadata.name,CSVLATEST:".status.channels[?(@.name==\"${CSV_CHANNEL}\")].currentCSV" | grep 'fuse-online ' | awk -F ' ' '{ print $2 }')
        # empty when catalog source doesn't exist, none when channel doesn't exist
        if [ -z "${CSV_VERSION}" ] || [ "${CSV_VERSION}" == '<none>' ]
        then
          echo "[ERROR] Unable to find CSV_VERSION in the \"${CSV_CHANNEL}\" channel for fuse-online packagemanifest in \"${CATALOG_SOURCE}\" catalog source. Check it manually if catalogsource or channel exist or specify CSV_VERSION env explicitly!"
          exit 1
        fi
  fi

	echo "Using full mode, deploying Fuse Online via OperatorHub. CatalogSource: ${CATALOG_SOURCE}, csv version: ${CSV_VERSION}"

	oc delete project ${NAMESPACE} || :
	until ! oc project "${NAMESPACE}"; do echo "Project still exists"; sleep 5; done

	oc login  --insecure-skip-tls-verify=true -u "${UI_USERNAME}" -p "${UI_PASSWORD}" "${URL}"
	oc new-project ${NAMESPACE}
	oc login  --insecure-skip-tls-verify=true -u "${ADMIN_USERNAME}" -p "${ADMIN_PASSWORD}" "${URL}"
	oc project ${NAMESPACE}
	
	cat <<EOF >> ./test.properties
syndesis.config.install.operatorhub=true
syndesis.config.operatorhub.catalogsource=${CATALOG_SOURCE}
syndesis.config.operatorhub.csv.name=${CSV_VERSION}
syndesis.config.operatorhub.csv.channel=${CSV_CHANNEL}
syndesis.config.append.repository=false
EOF
	#deploy Fuse Online
	./mvnw clean verify -fn -Prest -Pdeploy -Dtags="@deploy"
fi


CURRENT_RETRIES=0
while [ ${CURRENT_RETRIES} -lt ${RETRIES} ]; do
	./mvnw clean verify -fn -P "${PROFILE}" -Dtags="${TAGS}" -Dmaven.failsafe.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Xnoagent -Djava.compiler=NONE"

	HAS_FAILURES="$(grep -R --exclude-dir docker "<failure message" . || :)"
	[ -z "${HAS_FAILURES}" ] && break
	(( CURRENT_RETRIES++ ))
done

[ -d "/test-run-results" ] && sudo rm -rf /test-run-results/* || sudo mkdir /test-run-results

while read -r FILE; do sudo mkdir -p /test-run-results/"$(dirname "$FILE")"; sudo cp "$FILE" /test-run-results/"$(dirname "$FILE")"; done <<< "$(find * -type f -name "*.log")"
while read -r DIR; do sudo mkdir -p /test-run-results/"$DIR"; sudo cp -r "$DIR"/* /test-run-results/"$DIR"; done <<< "$(find * -maxdepth 2 -type d -wholename "*target/cucumber*")"

[ -z "${HAS_FAILURES}" ] && exit 0 || exit 1

