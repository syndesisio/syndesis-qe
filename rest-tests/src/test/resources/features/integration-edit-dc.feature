# @sustainer: avano@redhat.com

@rest
@integration-edit-dc
Feature: Integration - Edit DC

  Background: Create sample integration
    Given clean application state
      And remove all records from table "CONTACT"

  @ENTESB-11690
  @integration-edit-dc-replicas-state
  Scenario: Edit Integration DC - Replicas - redeploy
    When inserts into "CONTACT" table
      | X | Y | Z | db |
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "30000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task) VALUES (:#task)"
    Then create integration with name: "edit-dc-replicas-state"
      And wait for integration with name: "edit-dc-replicas-state" to become active
    When remove all records from table "TODO"
    Then validate that number of all todos with task "X" is "1"
    When edit replicas count for deployment config "i-edit-dc-replicas-state" to 2
    Then check that the pod "i-edit-dc-replicas-state" is not redeployed
    When remove all records from table "TODO"
    Then validate that number of all todos with task "X" is "2"
