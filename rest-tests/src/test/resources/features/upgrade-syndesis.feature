# @sustainer: avano@redhat.com

@syndesis-upgrade
Feature: Syndesis Upgrade

  Background:
    Given clean default namespace
      And get upgrade versions
      And clean upgrade modifications
      And deploy Syndesis
      And wait for Syndesis to become ready
      # Delete operator to avoid issues during upgrade
      And delete syndesis operator
      And verify syndesis "given" version
    When inserts into "contact" table
        | X | Y | Z | db |
      And deploy HTTP endpoints
      And create HTTP connection
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add a split step
      And create basic filter step for "last_name" with word "Y" and operation "contains"
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 0)"
    Then create integration with name: "upgrade"
      And wait for integration with name: "upgrade" to become active
      And verify integration with task "X"

  @upgrade
  Scenario: Syndesis Upgrade
    When perform test modifications
      And perform syndesis upgrade to newer version
    Then verify syndesis "upgraded" version
      And verify successful test modifications
      And verify integration with task "X"
    When rebuild integration with name "UPGRADE INTEGRATION NAME"
      # Integration name was updated, so this bc is not valid anymore
      And delete buildconfig with name "i-upgrade"
    Then wait for integration with name: "UPGRADE INTEGRATION NAME" to become active
      And verify integration with task "X"
    When refresh server port-forward
      And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
      And create HTTP "GET" step
      And create integration with name: "timer-to-http"
      And wait for integration with name: "timer-to-http" to become active
    Then verify that after "2.5" seconds there were "2" calls
      And verify correct s2i tag for builds

  @rollback
  @gh-4410
  Scenario: Syndesis Upgrade rollback
    When perform test modifications
      And add rollback cause to upgrade script
      And perform syndesis upgrade to newer version
    Then verify syndesis "given" version
      And verify test modifications rollback
      And verify integration with task "X"
    When rebuild integration with name "upgrade"
    Then wait for integration with name: "upgrade" to become active
    When refresh server port-forward
      And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
      And create HTTP "GET" step
      And create integration with name: "timer-to-http"
      And wait for integration with name: "timer-to-http" to become active
    Then verify that after "2.5" seconds there were "2" calls
      And verify correct s2i tag for builds
