#!/usr/bin/env bash

LATEST=`cat /tmp/backup/LATEST`

# Add a new label to the config map
CONTENT=`cat /tmp/backup/${LATEST}/target/resources/ConfigMap/syndesis-ui-config.json`
CONTENT=`echo ${CONTENT} | sed 's#"syndesis.io/app":"syndesis"#"syndesis.io/app":"syndesis","TEST":"UPGRADE"#g'`

cat <<EOT | oc replace -f -
`echo ${CONTENT}`
EOT
