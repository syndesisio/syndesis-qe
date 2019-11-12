# @sustainer: avano@redhat.com

@operator
@ignore
Feature: Operator Deployment

  Background:
    Given clean default namespace
      And deploy Syndesis CRD
      And grant permissions to user
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
  Scenario: Syndesis Operator - Components - Server - Deploy integrations
    When deploy Syndesis CR from file "spec/components/server/deployIntegrations.yml"
      And wait for Syndesis to become ready
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add log step
      And create integration with name: "deploy-integrations-check"
    Then verify that the integration with name "deploy-integrations-check" is not started

  @operator-integration-limit
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
  Scenario: Syndesis Operator - Components - Server - Integration State Check Interval
    When deploy Syndesis CR from file "spec/components/server/integrationStateCheckInterval.yml"
      And wait for Syndesis to become ready
    Then check that the "syndesis-server-config" config map contains
      | application.yml |  integrationStateCheckInterval: '111' |

  @operator-addons-todo
  Scenario: Syndesis Operator - Addons - Todo
    When deploy Syndesis CR from file "spec/addons/todo.yml"
      And wait for Syndesis to become ready
    Then check that deployment config "todo" does exist

  @ENTESB-12177
  @ENTESB-12421
  @operator-addons-camelk
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
    Then wait for integration with name: "sql-to-log" to become active
      And check that pod "i-sql-to-log" logs contain string "Jackson"

  @operator-maven-repositories
  Scenario: Syndesis Operator - Components - Server - Maven Repositories
    When deploy Syndesis CR from file "spec/components/server/mavenRepositories.yml"
    Then wait for Syndesis to become ready
      And check that the "syndesis-server-config" config map contains
        | application.yml | customRepo1: https://customRepo1 |
        | application.yml | customRepo2: https://customRepo2 |

  @ENTESB-12418
  @operator-addons-dv
  Scenario: Syndesis Operator - Addons - Data Virtualization
    When deploy Syndesis CR from file "spec/addons/dv.yml"
    Then wait for Syndesis to become ready
      And check that the "syndesis-ui-config" config map contains
        | config.json | "enabled": 1 |
      And check that deployment config "syndesis-dv" does exist
      And wait for DV to become ready

  @operator-addons-ops
  Scenario: Syndesis Operator - Addons - Ops
    When deploy Syndesis CR from file "spec/addons/ops.yml"
    Then wait for Syndesis to become ready
      And check that service "syndesis-integrations" does exist

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
  Scenario: Syndesis Operator - Addons - Knative
    When todo

  @ENTESB-12418
  @operator-components-limits-memory
  Scenario: Syndesis Operator - Components - Memory limits
    When deploy Syndesis CR from file "spec/components/resources.limits.memory.yml"
    Then wait for Syndesis to become ready
      And check correct memory limits

  @operator-components-volume-capacity
  Scenario: Syndesis Operator - Components - Volume Capacity
    When deploy Syndesis CR from file "spec/components/resources.volumeCapacity.yml"
    Then wait for Syndesis to become ready
      And check correct volume capacity

  @operator-components-database-volume-capacity
  Scenario: Syndesis Operator - Components - Database - Volume capacity
    When create test persistent volumes with "standard" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeCapacity.yml"
    Then check that database persistent volume capacity is greater or equals to "3Gi"

  @operator-components-database-volume-name
  Scenario: Syndesis Operator - Components - Database - Volume name
    When create test persistent volumes with "standard" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeName.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12533
  @operator-components-database-volume-access-modes
  Scenario: Syndesis Operator - Components - Database - Volume access modes
    When create test persistent volumes with "standard" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeAccessModes.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12533
  @operator-components-database-volume-labels
  Scenario: Syndesis Operator - Components - Database - Volume labels
    When create test persistent volumes with "standard" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeLabels.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12533
  @operator-components-database-volume-storage-class
  Scenario: Syndesis Operator - Components - Database - Volume storage class
    When create test persistent volumes with "filesystem" storage class name
      And deploy Syndesis CR from file "spec/components/database/volumeStorageClass.yml"
    Then check that test persistent volume is claimed by syndesis-db

  @ENTESB-12424
  @operator-components-external-db
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
    Scenario: Syndesis Operator - Components - OAuth - Disable SAR check
      When deploy Syndesis CR from file "spec/components/oauth/disableSarCheck.yml"
      Then wait for Syndesis to become ready
        And check that SAR check is disabled

  @operator-components-oauth-sarNamespace
  Scenario: Syndesis Operator - Components - OAuth - SAR namespace
    When deploy Syndesis CR from file "spec/components/oauth/sarNamespace.yml"
    Then wait for Syndesis to become ready
      And check that SAR check is enabled for namespace "testNamespace"

  @operator-components-prometheus-rules
  Scenario: Syndesis Operator - Components - Prometheus - Rules
    When todo

  @ENTESB-12280
  @operator-components-3scale
  Scenario: Syndesis Operator - Components - Server - 3Scale
    When deploy Syndesis CR from file "spec/components/server/managementUrlFor3Scale.yml"
      And wait for Syndesis to become ready
    Then check that the deployment config "syndesis-server" contains variables:
      | OPENSHIFT_MANAGEMENT_URL_FOR3SCALE | asdf |
