# @sustainer: avano@redhat.com

@syndesis-upgrade
Feature: Syndesis Upgrade

  Background:
    When get upgrade versions
    Given clean default namespace
      And clean upgrade modifications
      And deploy Syndesis from template
      And wait for Syndesis to become ready
      And verify syndesis "given" version
      And deploy HTTP endpoints
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
      And verify integration with task "X"

  @upgrade
  Scenario: Syndesis Upgrade
    When perform test modifications
      And modify s2i tag in syndesis-server-config
      And perform syndesis upgrade to newer version
    Then verify syndesis "upgraded" version
      And verify successful test modifications
      And verify integration with task "X"
    When refresh server port-forward
      And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
      And create HTTP "GET" step
      And create integration with name: "timer-to-http"
      And wait for integration with name: "timer-to-http" to become active
    Then verify that after "2.5" seconds there were "2" calls

  @rollback
  @gh-4410
  Scenario: Syndesis Upgrade rollback
    When perform test modifications
      And add rollback cause to upgrade script
      And perform syndesis upgrade to newer version
    Then verify syndesis "given" version
      And verify test modifications rollback
      And verify integration with task "X"
    When refresh server port-forward
      And add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
      And create HTTP "GET" step
      And create integration with name: "timer-to-http"
      And wait for integration with name: "timer-to-http" to become active
    Then verify that after "2.5" seconds there were "2" calls
