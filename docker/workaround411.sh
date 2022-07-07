#!/bin/bash

# get the openshift version in format x.yy
ocpVersion=$(oc version|grep ^Server|awk '{print $3}'|awk -F'.' '{print $1"."$2}')
# only proceeds if the openshift is 4.11 or after
if [[ ${ocpVersion} < 4.11 ]]; then
    echo "Openshift is not 4.11 or after this release, so no need to link the service account to the secret token."
    exit 0
fi

# list all secrets of type token
oc get secret -oname|grep token|while read tk; do
  # get the service account name set in the annotation field
  sa=$(oc describe $tk|grep "kubernetes.io/service-account.name"|cut -d\: -f 2|sed 's/ //g')
  # get only the secret object name
  sec=$(echo $tk|cut -d\/ -f2)
  # get the list of secrets in the service account and verify if the secret token is already linked to it
  isSecretSet=$(oc get sa/$sa -oyaml|grep -A10 ^secrets:|grep $sec)
  if [ -z "$isSecretSet" ]; then
    echo "==> link secret $sec to ServiceAccount/$sa";
    oc secret link $sa $sec
  fi
done
