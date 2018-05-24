@manual
Feature: Syndesis Upgrade

  Background:
    When get upgrade versions
    Given clean default namespace
      And deploy Syndesis from template
      And wait for Syndesis to become ready
      And verify syndesis "given" version
    Then inserts into "contact" table
      | X | Y | Z | db |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
    And start mapper definition with name: "mapping 1"
    And MAP using Step 1 and field "/first_name" to "/task"
    And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 0)"
    Then create integration with name: "upgrade"
    Then wait for integration with name: "upgrade" to become active
    Then verify integration with task "X"

  @upgrade
  Scenario: Syndesis Upgrade
    When perform test modifications
      And perform syndesis upgrade to newer version
    Then verify syndesis "upgraded" version
      And verify successful test modifications
      And verify integration with task "X"

  @rollback
  Scenario: Syndesis Upgrade rollback
    When perform test modifications
      And add rollback cause to upgrade script
      And perform syndesis upgrade to newer version
    Then verify syndesis "given" version
      And verify test modifications rollback
      And verify integration with task "X"
