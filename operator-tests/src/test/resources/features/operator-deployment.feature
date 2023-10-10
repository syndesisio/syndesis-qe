# @sustainer: avano@redhat.com

@operator
Feature: Operator Deployment

  Background:
    Given clean default namespace
      And deploy Syndesis CRD
      And install cluster resources
      And grant permissions to user
      And create pull secret
      And deploy Syndesis operator

  @smoke
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

  @operator-integration-limit
  @operator-server
  Scenario: Syndesis Operator - Components - Server - Integration Limit
    When deploy Syndesis CR from file "spec/components/server/integrationLimit.yml"
      And wait for Syndesis to become ready
    Then check that the "syndesis-server-config" config map contains
      | application.yml |  maxIntegrationsPerUser: '2' |
      | application.yml |  maxDeploymentsPerUser: '2'  |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "my-int-1"
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "my-int-2"
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
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

  @ENTESB-15863
  @ENTESB-12104
  @operator-addons-todo
  @operator-addons
  Scenario: Syndesis Operator - Addons - Todo
    When deploy Syndesis CR from file "spec/addons/todo.yml"
      And wait for Syndesis to become ready
    Then check that deployment config "todo" does exist
      And execute SQL command "SELECT * FROM TODO"

  @ENTESB-14068
  @operator-maven-additional-arguments
  @operator-server
  Scenario: Syndesis Operator - Components - Server - Additional maven arguments
    When deploy Syndesis CR from file "spec/components/server/additionalMavenArguments.yml"
    Then wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "additional-arguments"
      And wait for integration with name: "additional-arguments" to become active
    Then check that the "syndesis-server-config" config map contains
      | application.yml | additionalMavenArguments: "--strict-checksums -DtestProperty=testValue" |
      And check that the build config "i-additional-arguments" contains variables:
        | MAVEN_ARGS_APPEND | --strict-checksums -DtestProperty=testValue |
      And check that the build log "i-additional-arguments" contains "-DtestProperty=testValue"

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
    When deploy Syndesis CR from file "spec/addons/jaeger.yml"
    Then wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
      And create integration with name: "sql-sql-jaeger"
    Then wait for integration with name: "sql-sql-jaeger" to become active
      And check that jaeger pod "syndesis-jaeger" is collecting metrics for integration "sql-sql-jaeger"

  @operator-addons-jaeger-sampler
  @operator-addons
  @operator-addons-jaeger
  Scenario: Syndesis Operator - Addons - Jaeger - Sampler
    When deploy Syndesis CR from file "spec/addons/jaeger.sampler.yml"
    Then wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
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
    And check that jaeger pod "syndesis-jaeger" is collecting metrics for integration "sql-sql-jaeger"

  @operator-addons-jaeger-external
  @operator-addons
  @operator-addons-jaeger
  Scenario: Syndesis Operator - Addons - Jaeger - external
    When deploy Jaeger
    And deploy Syndesis CR from file "spec/addons/jaeger-external.yml"
    Then wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
    And add a split step
    And start mapper definition with name: "mapping 1"
    And MAP using Step 2 and field "/first_name" to "/<>/task"
    And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
    And create integration with name: "sql-sql-jaeger"
    Then wait for integration with name: "sql-sql-jaeger" to become active
    And check that jaeger pod "jaeger-all-in-one" is collecting metrics for integration "sql-sql-jaeger"

  @ENTESB-15540
  @operator-addons-jaeger-hybrid
  @operator-addons
  @operator-addons-jaeger
  Scenario: Syndesis Operator - Addons - Jaeger - hybrid
    When deploy Syndesis CR from file "spec/addons/jaeger-hybrid.yml"
    Then wait for Syndesis to become ready
      And check that the pod "syndesis-jaeger" has not appeared
    When create jaeger cr from "jaeger-syndesis-for-hybrid" file
    Then wait until "syndesis-jaeger" pod is running
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
      And create integration with name: "sql-sql-jaeger-hybrid"
    Then wait for integration with name: "sql-sql-jaeger-hybrid" to become active
      And check that jaeger pod "syndesis-jaeger" is collecting metrics for integration "sql-sql-jaeger-hybrid"
      #ENTESB-15540
      And validate that activity log is working for integration "sql-sql-jaeger-hybrid"

  @operator-addons-knative
  @operator-addons
  Scenario: Syndesis Operator - Addons - Knative
    When deploy Syndesis CR from file "spec/addons/knative.yml"
      Then wait for Syndesis to become ready
      And check that the deployment config "syndesis-server" contains variables:
        | KNATIVE_ENABLED  | true |

  @ENTESB-12418
  @ENTESB-12618
  @ENTESB-14015
  @operator-components-limits-memory
  @operator-db
  @operator-meta
  @operator-server
  @operator-prometheus
  Scenario: Syndesis Operator - Components - Memory limits
    When deploy Syndesis CR from file "spec/components/resources.limits.requests.memory.cpu.yml"
    Then wait for Syndesis to become ready
      And check correct memory limits

  @ENTESB-13622
  @ENTESB-13623
  @operator-components-volumes
  @operator-components-volume-capacity
  @operator-db
  @operator-meta
  @operator-prometheus
  Scenario Outline: Syndesis Operator - Components - <component> - Volume Capacity
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "<file>"
    Then check that "<pvcName>" persistent volume capacity is greater or equals to "3Gi"

    Examples:
      | component  | file                                          | pvcName             |
      | Database   | spec/components/database/volumeCapacity.yml   | syndesis-db         |
      | Meta       | spec/components/meta/volumeCapacity.yml       | syndesis-meta       |
      | Prometheus | spec/components/prometheus/volumeCapacity.yml | syndesis-prometheus |

  @ENTESB-13622
  @ENTESB-13623
  @operator-components-volumes
  @operator-components-volume-name
  @operator-db
  @operator-meta
  @operator-prometheus
  Scenario Outline: Syndesis Operator - Components - <component> - Volume Name
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "<file>"
    Then check that test persistent volume is claimed by "<pvcName>"

    Examples:
      | component  | file                                      | pvcName             |
      | Database   | spec/components/database/volumeName.yml   | syndesis-db         |
      | Meta       | spec/components/meta/volumeName.yml       | syndesis-meta       |
      | Prometheus | spec/components/prometheus/volumeName.yml | syndesis-prometheus |

  @ENTESB-12533
  @ENTESB-13622
  @ENTESB-13623
  @operator-components-volumes
  @operator-components-volume-access-modes
  @operator-db
  @operator-meta
  @operator-prometheus
  Scenario Outline: Syndesis Operator - Components - <component> - Volume Access Modes
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "<file>"
    Then check that test persistent volume is claimed by "<pvcName>"

    Examples:
      | component  | file                                             | pvcName             |
      | Database   | spec/components/database/volumeAccessModes.yml   | syndesis-db         |
      | Meta       | spec/components/meta/volumeAccessModes.yml       | syndesis-meta       |
      | Prometheus | spec/components/prometheus/volumeAccessModes.yml | syndesis-prometheus |

  @ENTESB-12533
  @ENTESB-13622
  @ENTESB-13623
  @operator-components-volumes
  @operator-components-volume-labels
  @operator-db
  @operator-meta
  @operator-prometheus
  Scenario Outline: Syndesis Operator - Components - <component> - Volume Labels
    When create test persistent volumes with "" storage class name
      And deploy Syndesis CR from file "<file>"
    Then check that test persistent volume is claimed by "<pvcName>"

    Examples:
      | component  | file                                        | pvcName             |
      | Database   | spec/components/database/volumeLabels.yml   | syndesis-db         |
      | Meta       | spec/components/meta/volumeLabels.yml       | syndesis-meta       |
      | Prometheus | spec/components/prometheus/volumeLabels.yml | syndesis-prometheus |

  @ENTESB-13622
  @ENTESB-13623
  @operator-components-volume-storage-class
  @operator-components-volumes
  @operator-db
  Scenario Outline: Syndesis Operator - Components - <component> - Volume Storage Class
    When create test persistent volumes with "filesystem" storage class name
      And deploy Syndesis CR from file "<file>"
    Then check that test persistent volume is claimed by "<pvcName>"

    Examples:
      | component  | file                                              | pvcName             |
      | Database   | spec/components/database/volumeStorageClass.yml   | syndesis-db         |
      | Meta       | spec/components/meta/volumeStorageClass.yml       | syndesis-meta       |
      | Prometheus | spec/components/prometheus/volumeStorageClass.yml | syndesis-prometheus |

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

  @ENTESB-17153
  @operator-components-oauth-env
  Scenario: Syndesis Operator - Components - OAuth - Environment variables
    When deploy Syndesis CR from file "spec/components/oauth/environments.yml"
    And wait for Syndesis to become ready
    Then check that the pod "syndesis-oauthproxy" contains variables:
      | myEnv1 | myEnv1Val |
      | myEnv2 | myEnv2Val |

  @ENTESB-12280
  @operator-components-3scale
  Scenario: Syndesis Operator - Components - Server - 3Scale
    When deploy Syndesis CR from file "spec/components/server/managementUrlFor3Scale.yml"
      And wait for Syndesis to become ready
    Then check that the deployment config "syndesis-server" contains variables:
      | OPENSHIFT_MANAGEMENT_URL_FOR3SCALE | asdf |

  @operator-backup-restore
  @operator-backup-multiple
  Scenario: Syndesis Operator - Multiple backups
    Given create sample bucket on S3 with name "syndesis-backup"
      And clean backup S3 bucket
    When create pull secret for backup
      And deploy Syndesis CR from file "spec/backup-multiple.yml"
      And wait for Syndesis to become ready
      And wait for backup with 5s interval
    When clean backup S3 bucket
    Then sleep for jenkins delay or 600 seconds
      And verify that there are 3 backups in S3

  @ENTESB-12114
  @ENTESB-12846
  @ENTESB-13046
  @operator-backup-restore
  @operator-backup-restore-procedure
  Scenario Outline: Backup and Restore - db <type>, method <method>
    # We can always deploy the custom db even it's not used in that combination
    Given deploy custom database
      And deploy ActiveMQ broker
      And clean destination type "queue" with name "backup-in"
      And clean destination type "queue" with name "backup-out"
      And create sample bucket on S3 with name "syndesis-backup"
      And clean backup S3 bucket
    When create pull secret for backup
      And deploy Syndesis CR from file "<customResource>"
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

    When wait for backup with 30s interval
      And download the backup file
      And prepare backup folder

      # Deploy new instance of syndesis
      And clean application state
      And undeploy Syndesis
      And redeploy custom database
      And clean destination type "queue" with name "backup-in"
      And clean destination type "queue" with name "backup-out"
      And deploy Syndesis CR from file "<customResourceAfter>"
    Then wait for Syndesis to become ready

    # Restore backup
    When perform "<method>" "<type>" restore from backup
      And sleep for jenkins delay or 30 seconds
      And refresh server port-forward

    # Ideally the connection should be updated so that the password is encrypted with the current key
    # And then the integration should be rebuilt to pick up the changed connection, but the integration should be rebuilt almost from scratch using rest
    # And the update connections / rebuild is covered by export-import scenario, so just verify that the connection/extension/integration is there
    Then check that connection "Fuse QE ACTIVEMQ" exists
      And check that extension "set-sqs-group-id-extension" exists
      And verify that integration with name "amq-amq-backup" exists

    Examples:
      | customResource              | method   | type     | customResourceAfter                    |
#      | spec/backup.yml             | operator | standard | minimal.yml                            | ENTESB-13046
      | spec/backup.yml             | manual   | standard | minimal.yml                            |
#      | spec/backup-external-db.yml | operator | external |spec/components/database/externalDb.yml | ENTESB-13046
      | spec/backup-external-db.yml | manual   | external |spec/components/database/externalDb.yml |

  @operator-affinity
  @operator-affinity-infra
  @ENTESB-13803
  Scenario: Syndesis operator - Affinity - Infra
    When deploy Syndesis CR from file "spec/affinityInfra.yml"
    Then wait for Syndesis to become ready
      And check affinity for infra pods
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "affinity-test"
    Then wait for integration with name: "affinity-test" to become active
      And check affinity not set for integration pods

  @operator-affinity
  @operator-affinity-integration
  @ENTESB-13803
  Scenario: Syndesis operator - Affinity - Integration
    When deploy Syndesis CR from file "spec/affinityIntegration.yml"
    Then wait for Syndesis to become ready
      And check affinity not set for infra pods
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "affinity-test"
    Then wait for integration with name: "affinity-test" to become active
      And check affinity for integration pods

  @operator-tolerations
  @operator-tolerations-infra
  @ENTESB-13803
  Scenario: Syndesis operator - Tolerations - Infra
    When deploy Syndesis CR from file "spec/tolerationsInfra.yml"
    Then wait for Syndesis to become ready
      And check tolerations for infra pods
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "tolerations-test"
    Then wait for integration with name: "tolerations-test" to become active
      And check tolerations not set for integration pods

  @operator-tolerations
  @operator-tolerations-integration
  @ENTESB-13803
  Scenario: Syndesis operator - Tolerations - Integration
    When deploy Syndesis CR from file "spec/tolerationsIntegration.yml"
    Then wait for Syndesis to become ready
      And check tolerations not set for infra pods
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
      And add log step
      And create integration with name: "tolerations-test"
    Then wait for integration with name: "tolerations-test" to become active
      And check tolerations for integration pods

  @ENTESB-14875
  @server-respin
  @operator-server
  Scenario Outline: Syndesis Operator - Components - Server - <name> - Don't respin on configmap update
    When deploy Syndesis CR from file "minimal.yml"
      And wait for Syndesis to become ready
    Then check that the "syndesis-server-config" config map doesn't contain
      | application.yml | <newValue> |
    When update CR with "<crFile>" file
    Then check that the pod "syndesis-server" is not redeployed by server
      And check that the "syndesis-server-config" config map contains
        | application.yml | <newValue> |

    Examples:
      | name              | crFile                                              | newValue                                                                |
      | Integration Limit | spec/components/server/integrationLimit.yml         | maxIntegrationsPerUser: '2'                                             |
      | Maven Args        | spec/components/server/additionalMavenArguments.yml | additionalMavenArguments: "--strict-checksums -DtestProperty=testValue" |
      | Maven Repo        | spec/components/server/mavenRepositories-append.yml | customRepo2: https://customRepo2                                        |

  @operator-auditing
  Scenario: Syndesis Operator - auditing
    When deploy Syndesis CR from file "spec/components/server/auditing.yml"
    And wait for Syndesis to become ready
    And deploy ActiveMQ broker
    And create ActiveMQ connection
    And sleep for jenkins delay or 5 seconds
    Then check that the last audit record contains following parameters
      | type       | connection       |
      | name       | Fuse QE ACTIVEMQ |
      | user       | pista            |
      | recordType | created          |
    And check that the last audit record contains the event with the following parameters
      | type     | set              |
      | property | name             |
      | current  | Fuse QE ACTIVEMQ |
    And check that the last audit record contains the event with the following parameters
      | type     | set                            |
      | property | configuredProperties.brokerUrl |
      | current  | tcp://syndesis-amq-tcp:61616   |
    And check that the last audit record contains the event with the following parameters
      | type     | set                           |
      | property | configuredProperties.username |
      | current  | amq                           |
    And check that the last audit record contains the event with the following parameters
      | type     | set                           |
      | property | configuredProperties.password |
      | current  | **********                    |
