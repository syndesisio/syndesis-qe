# @sustainer: avano@redhat.com

@rest
@integration-edit-dc
Feature: Integration - Edit DC

  Background: Create sample integration
    Given clean application state
      And remove all records from table "CONTACT"

  @delorean
  @ENTESB-11690
  @ENTESB-11640
  @integration-edit-dc-replicas
  Scenario: Edit Integration DC - Replicas
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "20000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task) VALUES (:#task)"
    Then create integration with name: "edit-dc-replicas"
      And wait for integration with name: "edit-dc-replicas" to become active
    When remove all records from table "TODO"
      And inserts into "CONTACT" table
        | X | Y | Z | db |
    Then validate that number of all todos with task "X" is "1"
    When edit replicas count for deployment config "i-edit-dc-replicas" to 2
      And change deployment strategy for "i-edit-dc-replicas" deployment config to "Rolling"
    Then check that the pod "i-edit-dc-replicas" is not redeployed by server
    When remove all records from table "CONTACT"
      And remove all records from table "TODO"
      And inserts into "CONTACT" table
        | X | Y | Z | db |
    Then validate that number of all todos with task "X" is "2"
    When rebuild integration with name "edit-dc-replicas"
    Then wait for integration with name: "edit-dc-replicas" to become active
      And check that there are 2 pods for integration "edit-dc-replicas"

  @delorean
  @ENTESB-10194
  @integration-edit-dc-env-var
  Scenario: Edit Integration DC - Env variable
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "20000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task) VALUES (:#task)"
    Then create integration with name: "edit-dc-env-var"
      And wait for integration with name: "edit-dc-env-var" to become active
    When remove all records from table "TODO"
      And inserts into "CONTACT" table
        | X | Y | Z | db |
    Then validate that number of all todos with task "X" is "1"
    When add following variables to the "i-edit-dc-env-var" deployment config:
      | TEST_KEY1 | TEST_VALUE1 |
      | TEST_KEY2 | TEST_VALUE2 |
    Then wait until "i-edit-dc-env-var" pod is reloaded
    When rebuild integration with name "edit-dc-env-var"
    Then wait for integration with name: "edit-dc-env-var" to become active
      And check that the deployment config "i-edit-dc-env-var" contains variables:
      | TEST_KEY1 | TEST_VALUE1 |
      | TEST_KEY2 | TEST_VALUE2 |
    When remove all records from table "CONTACT"
      And remove all records from table "TODO"
      And inserts into "CONTACT" table
        | X | Y | Z | db |
    Then validate that number of all todos with task "X" is "1"

  @ENTESB-11640
  @integration-edit-dc-autoscaler
  Scenario: Edit Integration DC - Autoscaler
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "20000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task) VALUES (:#task)"
    Then create integration with name: "edit-dc-hpa"
      And wait for integration with name: "edit-dc-hpa" to become active
    When remove all records from table "TODO"
      And inserts into "CONTACT" table
        | X | Y | Z | db |
    Then validate that number of all todos with task "X" is "1"
    When create HPA for deployment config "i-edit-dc-hpa" with 2 replicas
      And rebuild integration with name "edit-dc-hpa"
    Then wait for integration with name: "edit-dc-hpa" to become active
      And check that there are 2 pods for integration "edit-dc-hpa"
    When remove all records from table "CONTACT"
      And remove all records from table "TODO"
      And inserts into "CONTACT" table
        | X | Y | Z | db |
    Then validate that number of all todos with task "X" is "2"
