# @sustainer: avano@redhat.com

@manual
@syndesis-upgrade-prod
Feature: Syndesis Upgrade - Productized Version

  # Before upgrading, create some integration and verify that it is working
  @prod-upgrade-before
  Scenario: Syndesis Prod Upgrade - Before upgrading
    Then verify syndesis version
    Given deploy HTTP endpoints
      And create HTTP connection
    When inserts into "contact" table
      | X | Y | Z | db |
      And create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
      And add a split step
      And start mapper definition with name: "mapping 1"
      And MAP using Step 2 and field "/first_name" to "/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 0)"
    Then create integration with name: "upgrade"
      And wait for integration with name: "upgrade" to become active
      And verify upgrade integration with task "X"

  # After the upgrade is done, create a new integration and verify that it is working + verify that the old one is still working as well
  @prod-upgrade-after
  Scenario: Syndesis Prod Upgrade - After upgrading
    Then verify syndesis version
    When rebuild integration with name "upgrade"
    Then wait for integration with name: "upgrade" to become active
      And verify upgrade integration with task "X"
    Given add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
    And create HTTP "GET" step
    And create integration with name: "timer-to-http"
    And wait for integration with name: "timer-to-http" to become active
    Then verify that after "2.5" seconds there were "2" calls
      And verify upgrade integration with task "X"
