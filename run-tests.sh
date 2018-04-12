 #!/bin/bash

#There 2 parameters run-tests.sh "MAVEN_PROFILE" "NAMESPACE"
MAVEN_PROFILE=${1:-"all"}
NAMESPACE=${2:-"syndesis-e2e-tests"}
GH_API_REPO="https://api.github.com/repos/syndesisio/syndesis-qe/pulls"

if [ -n "${CIRCLE_PR_NUMBER}" ]; then
  TAG_FROM_PR=$(curl -sH "Authorization: token ${GH_API_TOKEN}" ${GH_API_REPO}/${CIRCLE_PR_NUMBER} | jq -r ".body" | grep "//test" | cut -d'`' -f 2)
  if [ -n ${TAG_FROM_PR} ]; then
    echo "Fetched tag ${TAG_FROM_PR} from PR-${CIRCLE_PR_NUMBER}"
    CUCUMBER_TAG=${TAG_FROM_PR}
  fi
fi
if [ -z "${CUCUMBER_TAG}" ]; then
  echo "No CUCUMBER_TAG provided, using @smoke"
  CUCUMBER_TAG="@smoke"
else
  echo "Tag provided: ${CUCUMBER_TAG}"
fi

#We running in Circle CI job
if [ -n "${OPENSHIFT_TOKEN}" ]; then
    curl -fsSL https://github.com/openshift/origin/releases/download/v3.7.2/openshift-origin-client-tools-v3.7.2-282e43f-linux-64bit.tar.gz | sudo tar xz -C /usr/bin --strip-components 1
    oc login --server "${OPENSHIFT_SERVER}" --token "${OPENSHIFT_TOKEN}"

    oc get cm syndesis-qe -o jsonpath="{ .data.all_test_properties }" -n syndesis-qe-cci > test.properties
    oc get cm syndesis-qe -o jsonpath="{ .data.credentials }" -n syndesis-qe-cci > credentials.json

  if [ -n "${CIRCLE_PR_NUMBER}" ]; then
    NAMESPACE="syndesis-qe-pr"
  else
    NAMESPACE="syndesis-${MAVEN_PROFILE}-tests"
  fi

  echo "Using test namespace: ${NAMESPACE}"
  sed -i "s|NS_PLACEHOLDER|${NAMESPACE}|g" test.properties

  echo "Grant access to namespace: ${NAMESPACE}"
  oc new-project ${NAMESPACE}
  oc adm policy add-role-to-group admin syndesis -n ${NAMESPACE}
else
  echo "Setup OpenShift cluster credentials"
fi

if [ "${MAVEN_PROFILE}" == "all" ]; then
  ./mvnw clean test -B -P ${MAVEN_PROFILE} -Dcucumber.options="--tags ${CUCUMBER_TAG}"
else
  ./mvnw clean test -B -P ${MAVEN_PROFILE} -Dcucumber.options="--tags ~@wip"
fi
