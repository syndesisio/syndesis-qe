@integrations-db-to-db-mysql
Feature: Integration - DB to DB

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis
    Given clean MySQL server
    Given deploy MySQL server
    And   she stays there for "10000" ms
    Given create standard table schema on "mysql" driver

    When inserts into "contact" table on "mysql"
      | Josef_mysql | Stieranka | Istrochem | db |
    Given created connections
      | Database | MySQL | MySQL | Mysql on OpenShift |

#
#  2. select - insert
#
  @db-connection-crud-2-read-create-mysql
  Scenario: Read & create operations
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "MySQL" connection
    And she selects "Periodic SQL Invocation" integration action
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "MySQL" connection
    And she selects "Invoke SQL" integration action
#    @wip - bug to be reported, wrong is: (:#TASK, 2). (:#TASK, :#MASK) is OK
    Then she fills invoke query input with "INSERT INTO TODO(task, completed) VALUES (:#TASK, 2)" value
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"

#    And scroll "top" "right"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD2-read-create E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "CRUD2-read-create E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "CRUD2-read-create E2E" gets into "Published" state

    Then validate that all todos with task "Josef_mysql" have value completed "2", period in ms: "5000" on "mysql"
