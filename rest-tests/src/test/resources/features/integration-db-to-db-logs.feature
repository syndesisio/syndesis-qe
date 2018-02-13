@integrations-db-to-db-logs
Feature: Test functionality of logs monitoring system, part 1.

  Background: Clean application state
    And remove all records from DB

  @integrations-db-db-logs
  Scenario: DB to DB integration with basic monitoring
    Then inserts into "contact" table
      | Josef3 | Stieranka | Istrochem | db |

    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
    And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 0)"
    And create mapper step using template: "db-db-monitoring"

    Then create integration with name: "DB to DB logs rest test"
    Then wait for integration with name: "DB to DB logs rest test" to become active

    Then validate that number of all todos with task "Josef3" is "1", period in ms: "5000"

#    monitoring part:
    Then validate that log of integration "DB to DB logs rest test" has been created, period in ms: "5000"