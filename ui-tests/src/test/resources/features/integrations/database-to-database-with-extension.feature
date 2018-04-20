@integrations-database-to-database-with-extension
Feature: Integration - DB to DB with extension

  Background: 
    Given clean application state
    Given "Camilla" logs into the Syndesis
    Given reset content of "todo" table
    Given reset content of "contact" table
    Given imported extensions
    	| Log Message Body | syndesis-extension-log-body-1.0.0 |

  @tech-extension-create-integration-with-new-tech-extension
  Scenario: Create
    Then inserts into "contact" table
      | Josef | Stieranka | Istrochem | db |
    And "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # select postgresDB connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "10" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke Stored Procedure" integration action
    And she selects "add_lead" from "procedureName" dropdown
    And clicks on the "Done" button

    # add data mapper step
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "company" to "company"
    And she creates mapping from "last_name" to "first_and_last_name"
    And she creates mapping from "lead_source" to "lead_source"
    And clicks on the "Done" button

    # add tech extension step
    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the "Add a step" link
    Then she selects "Log Body" integration step
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "CRUD4-read-create-inbuilt E2E"
    And click on the "Publish" button

    # assert integration is present in list
    Then Camilla is presented with "CRUD4-read-create-inbuilt E2E" integration details
    And "Camilla" navigates to the "Integrations" page
    And Integration "CRUD4-read-create-inbuilt E2E" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "CRUD4-read-create-inbuilt E2E" gets into "Published" state

    Then validate add_lead procedure with last_name: "Stieranka", company: "Istrochem", period in ms: "10000"

    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"

    When Camilla choose "Delete" action on "Log Message Body" technical extension
    Then she is presented with dialog page "Warning!"
    And she can see notification about integrations "CRUD4-read-create-inbuilt E2E" in which is tech extension used

    Then she clicks on the modal dialog "Cancel" button