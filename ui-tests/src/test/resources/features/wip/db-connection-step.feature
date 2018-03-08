@wip
@db-connection-step
Feature: Test functionality of DB connection

  @db-connection-step-clean-application-state
  Scenario: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis

#
#  1. select - select - delete
#
  @db-connection-step-crud-1-read-update-delete
  Scenario: Create integration to test DB connector for update operation of step connection
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    #@wip time_unit_id to be specified after new update is available:
    Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #@wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button

    #adds STEP-connection postgresDB
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Connection" button
    Then Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "SELECT * FROM TODO" value
    And clicks on the "Done" button
    # STEP - CONNECTION

    # add datamapper-1 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds first step between START and STEP connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
#  (does not make sense here, so just click done)
    And clicks on the "Done" button

    # add datamapper-2 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds second step between STEP and FINISH connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    #@wip this doesn't work for the time being #681:
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD1-step-read-read E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "CRUD1-step-read-read E2E" integration details
    And she clicks on the "Done" button
    Then she waits until integration "CRUD1-step-read-read E2E" gets into "Active" state


#
#  2. select - insert - delete
#
  @wip
  @db-connection-step-crud-2-read-create-delete
  Scenario: Create integration to test DB connector for create operation of step connection
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    #@wip time_unit_id to be specified after new update is available:
    Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #@wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button

    #adds STEP-connection postgresDB
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Connection" button
    Then Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "INSERT INTO TODO(task) VALUES(:#task)" value
    And clicks on the "Done" button
    # STEP - CONNECTION

    # add datamapper-1 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds first step between START and STEP connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "company" to "TASK"
    And she creates mapping from "first_name" to "TASK"
    And she creates mapping from "last_name" to "TASK"
    And she creates mapping from "lead_source" to "TASK"
    And clicks on the "Done" button

    # add datamapper-2 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds second step between STEP and FINISH connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    #@wip this doesn't work for the time being #681:
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD2-step-read-create E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "CRUD2-step-read-create E2E" integration details
    And she clicks on the "Done" button
    Then she waits until integration "CRUD2-step-read-create E2E" gets into "Active" state


#
#  3. select - update - delete
#
  @wip
  @db-connection-step-crud-3-read-update-delete
  Scenario: Create integration to test DB connector for update operation of step connection
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    #@wip time_unit_id to be specified after new update is available:
    Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #@wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button

    #adds STEP-connection postgresDB
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Connection" button
    Then Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #wip this query doesnt work ftb #698
    Then she fills invoke query input with "UPDATE TODO SET completed=1 WHERE task like '%:#task%'" value
    And clicks on the "Done" button
    # STEP - CONNECTION

    # add datamapper-1 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds first step between START and STEP connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button

    # add datamapper-2 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds second step between STEP and FINISH connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    #@wip  this doesn't work for the time being, but should #681:
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD3-step-read-update E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "CRUD3-step-read-update E2E" integration details
    And she clicks on the "Done" button
    Then she waits until integration "CRUD3-step-read-update E2E" gets into "Active" state


#
#  4. select - delete - delete
#
  @wip
  @db-connection-step-crud-4-read-delete-delete
  Scenario: Create integration to test DB connector for delete operation of step connection
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    #@wip time_unit_id to be specified after new update is available:
    Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #@wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button

    #adds STEP-connection postgresDB
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Connection" button
    Then Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #@wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button
    # STEP - CONNECTION

    # add datamapper-1 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds first step between START and STEP connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button

    # add datamapper-2 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds second step between STEP and FINISH connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    #@wip  this doesn't work for the time being, but should #681:
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD4-step-read-delete E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "CRUD4-step-read-delete E2E" integration details
    And she clicks on the "Done" button
    Then she waits until integration "CRUD4-step-read-delete E2E" gets into "Active" state


#
#  5. builtin sql query checker
#
  @wip
  @db-connection-step-sqlquery-checker
  Scenario: Create integration to test inbuilt sql query checker for step connection
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    #@wip time_unit_id to be specified after new update is available:
    Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #@wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button

    #adds STEP-connection postgresDB
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Connection" button
    Then Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
#    wrong queries:
    Then she fills invoke query input with "SELECT * FROM TODO-A" value
    And she can see alert notification
    Then she fills invoke query input with "UPDATE TODO-A SET completed=1 WHERE task like '%:#task%'" value
    And she can see alert notification
    Then she fills invoke query input with "DELETE FROM TODO-A WHERE task like '%:#task%'" value
    And she can see alert notification
    Then she fills invoke query input with "INSERT INTO TODO-A(task) VALUES(:#task)" value
    And she can see alert notification
    #  correct query:
    Then she fills periodic query input with "INSERT INTO TODO(task) VALUES(:#task)" value
    And clicks on the "Done" button
    # STEP - CONNECTION

    # add datamapper-1 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds first step between START and STEP connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "company" to "TASK"
    And she creates mapping from "first_name" to "TASK"
    And she creates mapping from "last_name" to "TASK"
    And she creates mapping from "lead_source" to "TASK"
    And clicks on the "Done" button

    # add datamapper-2 step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When she adds second step between STEP and FINISH connection
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    #@wip  this doesn't work for the time being, but should #681:
    And she creates mapping from "first_name" to "TASK"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "Step connection SQL query checker E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "Step connection SQL query checker E2E" integration details
    And she clicks on the "Done" button
    Then she waits until integration "Step connection SQL query checker E2E" gets into "Active" state
