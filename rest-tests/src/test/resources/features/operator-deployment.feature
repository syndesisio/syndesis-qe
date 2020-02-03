# @sustainer: avano@redhat.com

@operator
@ignore
Feature: Operator Deployment

  Background:
    Given clean default namespace
      And deploy Syndesis CRD
      And grant permissions to user
      And create pull secret
      And deploy Syndesis operator

  @operator-deploy-default
  Scenario: Syndesis Operator - Deploy default configuration
    When deploy Syndesis CR from file "minimal.yml"
      And wait for Syndesis to become ready
    Then check deployed syndesis version
      And check that deployment config "todo" does not exist
      And check that SAR check is enabled for namespace "" 

  @operator-routehostname
  Scenario: Syndesis Operator - Route hostname
    When deploy Syndesis CR from file "spec/routeHostname.yml"
      And check the deployed route

  @ENTESB-12099
  @operator-is-namespace
  Scenario: Syndesis Operator - ImageStreams namespace
    When deploy Syndesis CR from file "spec/imagestreamsNamespace.yml"
      And wait for Syndesis to become ready
      # TODO once ENTESB-12099 is figured out

  @ENTESB-12104
  @operator-demodata
  Scenario: Syndesis Operator - Demo data
    When deploy Syndesis CR from file "spec/demoData.yml"
      And wait for Syndesis to become ready
      # TODO once ENTESB-12104 is done

  @ENTESB-12106
  @operator-deploy-integrations
  @operator-server
  Scenario: Syndesis Operator - Components - Server - Don't deploy integrations
    When deploy Syndesis CR from file "spec/components/server/deployIntegrations.yml"
      And wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add log step
      And create integration with name: "deploy-integrations-check"
    Then verify that the integration with name "deploy-integrations-check" is not started

  @operator-integration-limit
  @operator-server
  Scenario: Syndesis Operator - Components - Server - Integration Limit
    When deploy Syndesis CR from file "spec/components/server/integrationLimit.yml"
      And wait for Syndesis to become ready
    Then check that the "syndesis-server-config" config map contains
      | application.yml |  maxIntegrationsPerUser: '2' |
      | application.yml |  maxDeploymentsPerUser: '2'  |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add log step
      And create integration with name: "my-int-1"
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add log step
      And create integration with name: "my-int-2"
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add log step
      And create integration with name: "my-int-3"
    Then wait for integration with name: "my-int-1" to become active
      And wait for integration with name: "my-int-2" to become active
      And verify that the integration with name "my-int-3" is not started

  @operator-integration-state-check-interval
  @operator-server
  Scenario: Syndesis Operator - Components - Server - Integration State Check Interval
    When deploy Syndesis CR from file "spec/components/server/integrationStateCheckInterval.yml"
      And wait for Syndesis to become ready
    Then check that the "syndesis-server-config" config map contains
      | application.yml |  integrationStateCheckInterval: '111' |

  @operator-addons-todo
  @operator-addons
  Scenario: Syndesis Operator - Addons - Todo
    When deploy Syndesis CR from file "spec/addons/todo.yml"
      And wait for Syndesis to become ready
    Then check that deployment config "todo" does exist

  @ENTESB-12177
  @ENTESB-12421
  @operator-addons-camelk
  @operator-addons
  Scenario: Syndesis Operator - Addons - Camel K
    When deploy Camel-K
    Then wait for Camel-K to become ready
    When deploy Syndesis CR from file "spec/addons/camelk.yml"
    Then wait for Syndesis to become ready
      And check that the "syndesis-server-config" config map contains
        | application.yml | integration: camel-k |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add log step
      And create integration with name: "sql-to-log"
    # Camel-K operator initially needs to download all dependencies and it takes time with combination with our nexus
    # Especially when the artifacts are not cached on nexus yet
    Then wait max 30 minutes for integration with name: "sql-to-log" to become active
      And check that pod "i-sql-to-log" logs contain string "Jackson"

  @operator-maven-repositories
  @operator-server
  Scenario: Syndesis Operator - Components - Server - Maven Repositories
    When deploy Syndesis CR from file "spec/components/server/mavenRepositories.yml"
    Then wait for Syndesis to become ready
      And check that the "syndesis-server-config" config map contains
        | application.yml | customRepo1: https://customRepo1 |
        | application.yml | customRepo2: https://customRepo2 |

  @ENTESB-12418
  @operator-addons-dv
  @operator-addons
  Scenario: Syndesis Operator - Addons - Data Virtualization
    When deploy DV
      And deploy Syndesis CR from file "spec/addons/dv.yml"
    Then wait for Syndesis to become ready
      And check that the "syndesis-ui-config" config map contains
        | config.json | "enabled": 1 |
      And check that deployment config "syndesis-dv" does exist
      And wait for DV to become ready

  @operator-addons-ops
  @operator-addons
  Scenario: Syndesis Operator - Addons - Ops
    When deploy Syndesis CR from file "spec/addons/ops.yml"
    Then wait for Syndesis to become ready
      And check that service "syndesis-integrations" does exist

  @operator-addons-jaeger-integration
  @operator-addons
  @operator-addons-jaeger
  Scenario: Syndesis Operator - Addons - Jaeger
    When deploy Jaeger
      And deploy Syndesis CR from file "spec/addons/jaeger.yml"
    Then wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
      And create integration with name: "sql-sql-jaeger"
    Then wait for integration with name: "sql-sql-jaeger" to become active
      And check that jaeger is collecting metrics for integration "sql-sql-jaeger"

  @operator-addons-jaeger-sampler
  @operator-addons
  @operator-addons-jaeger
  Scenario: Syndesis Operator - Addons - Jaeger - Sampler
    When deploy Jaeger
      And deploy Syndesis CR from file "spec/addons/jaeger.sampler.yml"
    Then wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
      And create integration with name: "sql-sql-jaeger"
    Then wait for integration with name: "sql-sql-jaeger" to become active
      And check that the deployment config "syndesis-server" contains variables:
        | JAEGER_SAMPLER_TYPE  | probabilistic |
        | JAEGER_SAMPLER_PARAM | 0.001         |
      And check that the deployment config "syndesis-meta" contains variables:
        | JAEGER_SAMPLER_TYPE  | probabilistic |
        | JAEGER_SAMPLER_PARAM | 0.001         |
      And check that jaeger is collecting metrics for integration "sql-sql-jaeger"

  @operator-addons-knative
  @operator-addons
  Scenario: Syndesis Operator - Addons - Knative
    When todo

  @ENTESB-12418
  @ENTESB-12618
  @operator-components-limits-memory
  @operator-addons-dv
  @operator-db
  @operator-meta
  @operator-server
  @operator-prometheus
  Scenario: Syndesis Operator - Components - Memory limits
    When deploy DV
      And deploy Syndesis CR from file "spec/components/resources.limits.memory.yml"
    Then wait for Syndesis to become ready
      And check correct memory limits

  @operator-components-volume-capacity
  @operator-meta
  @operator-prometheus
  Scenario: Syndesis Operator - Components - Volume Capacity
    When deploy Syndesis CR from file "spec/components/resources.volumeCapacity.yml"
    Then wait for Syndesis to become ready
      And check correct volume capacity

  @operator-components-database-volumes
  @operator-components-database-volume-capacity
  @operator-db
  Scenario: Syndesis Operator - Components - Database - Volume Capacity
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeCapacity.yml"
    Then check that database persistent volume capacity is greater or equals to "3Gi"

  @operator-components-database-volumes
  @operator-components-database-volume-name
  @operator-db
  Scenario: Syndesis Operator - Components - Database - Volume Name
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeName.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12533
  @operator-components-database-volumes
  @operator-components-database-volume-access-modes
  @operator-db
  Scenario: Syndesis Operator - Components - Database - Volume Access Modes
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeAccessModes.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12533
  @operator-components-database-volumes
  @operator-components-database-volume-labels
  @operator-db
  Scenario: Syndesis Operator - Components - Database - Volume Labels
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeLabels.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @operator-components-database-volume-storage-class
  @operator-components-database-volumes
  @operator-db
  Scenario: Syndesis Operator - Components - Database - Volume Storage Class
    When create test persistent volumes with "filesystem" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeStorageClass.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12424
  @operator-components-external-db
  @operator-db
  Scenario: Syndesis Operator - Components - Database - External Database
    When deploy custom database
      And deploy Syndesis CR from file "spec/components/database/externalDb.yml"
    Then wait for Syndesis to become ready
      And check that deployment config "syndesis-db" does not exist
    When deploy HTTP endpoints
      And create HTTP connection
      And create HTTP "OPTIONS" step with period "5" "SECONDS"
      And add log step
      And create integration with name: "http-to-log"
    Then wait for integration with name: "http-to-log" to become active
      And check that pod "i-http-to-log" logs contain string "[[options]]"

  @operator-components-oauth-disableSarCheck
    Scenario: Syndesis Operator - Components - OAuth - Disable SAR Check
      When deploy Syndesis CR from file "spec/components/oauth/disableSarCheck.yml"
      Then wait for Syndesis to become ready
        And check that SAR check is disabled

  @operator-components-oauth-sarNamespace
  Scenario: Syndesis Operator - Components - OAuth - SAR namespace
    When deploy Syndesis CR from file "spec/components/oauth/sarNamespace.yml"
    Then wait for Syndesis to become ready
      And check that SAR check is enabled for namespace "testNamespace"

  @operator-components-prometheus-rules
  @operator-prometheus
  Scenario: Syndesis Operator - Components - Prometheus - Rules
    When todo

  @ENTESB-12280
  @operator-components-3scale
  Scenario: Syndesis Operator - Components - Server - 3Scale
    When deploy Syndesis CR from file "spec/components/server/managementUrlFor3Scale.yml"
      And wait for Syndesis to become ready
    Then check that the deployment config "syndesis-server" contains variables:
      | OPENSHIFT_MANAGEMENT_URL_FOR3SCALE | asdf |

  @ENTESB-12114
  @operator-backup-restore
  Scenario: Syndesis Operator - Backup and Restore
    Given deploy ActiveMQ broker
      And clean destination type "queue" with name "backup-in"
      And clean destination type "queue" with name "backup-out"
      And create sample bucket on S3 with name "syndesis-backup"
    When create pull secret for backup
      And deploy Syndesis CR from file "spec/backup.yml"
      And wait for Syndesis to become ready
      And create ActiveMQ connection
      # Not needed for the integration, just to check if it is present after restoring the backup
      And import extension from path "./src/test/resources/extensions/set-sqs-group-id-extension-1.0-SNAPSHOT.jar"
      And create ActiveMQ "subscribe" action step with destination type "queue" and destination name "backup-in"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "backup-out"
      And create integration with name: "amq-amq-backup"
    Then wait for integration with name: "amq-amq-backup" to become active
    When publish message with content "Hello backup" to "queue" with name "backup-in"
    Then verify that JMS message with content 'Hello backup' was received from "queue" "backup-out"

    When wait for backup
      And download the backup file
      And prepare backup folder

      # Deploy new instance of syndesis
      And clean application state
      And undeploy Syndesis
      And clean destination type "queue" with name "backup-in"
      And clean destination type "queue" with name "backup-out"
      And deploy Syndesis CR from file "minimal.yml"
      And wait for Syndesis to become ready

      # Restore backup
      And perform restore from backup

      And sleep for jenkins delay or "5" seconds
      And refresh server port-forward

    # Ideally the connection should be updated so that the password is encrypted with the current key
    # And then the integration should be rebuilt to pick up the changed connection, but the integration should be rebuilt almost from scratch using rest
    # And the update connections / rebuild is covered by export-import scenario, so just verify that the connection/extension/integration is there
    Then check that connection "Fuse QE ACTIVEMQ" exists
      And check that extension "set-sqs-group-id-extension" exists
      And verify that integration with name "amq-amq-backup" exists

