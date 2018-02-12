@integrations-db-to-db-monitoring
Feature: Test functionality of monitoring system, part 1.

  Background: Clean application state
    And remove all records from DB

  @integrations-db-db-monitoring
  Scenario: DB to DB integration with basic monitoring
    Then inserts into "contact" table
      | Josef2 | Stieranka2 | Istrochem | db |

    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
    And create finish DB invoke sql action step with query "INSERT INTO TODO (task) VALUES (:#task)"
    And create mapper step using template: "db-db-monitoring"

    Then create integration with name: "DB to DB monitoring rest test"
    Then wait for integration with name: "DB to DB monitoring rest test" to become active

    Then validate that number of all todos with task "Josef2" is "1", period in ms: "5000"

#    monitoring part:
    Then validate that number of all messages through integration "DB to DB monitoring rest test" is greater than "3", period in ms: "10000"