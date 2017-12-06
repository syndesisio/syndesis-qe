@tp3
@db-connection
Feature: Test functionality of DB connection

  @db-connection-clean-application-state
  Scenario: Clean application state
    Given "Camilla" logs into the Syndesis
    Given clean application state

#
#  1. select - update
#
  @db-connection-crud-1-read-update
  Scenario: Create integration to test DB connector for read and update operations
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    # time_unit_id to be specified after new update is available:
#    Then she fills period input with "1" value
#    Then she selects "Minutes" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #UPDATE todo SET completed=1 WHERE task like '%${first_name}%';:
    Then she fills invoke query input with "UPDATE TODO SET completed=1 WHERE task like '%:#task%'" value
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"

    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she defines integration name "CRUD1-read-update E2E"
    And clicks on the "Publish" button
      # assert integration is present in list
    #(this step is temporarily commented-out, there is no more h1 label with integration name there, syndesis #430 )
#    Then Camilla is presented with "CRUD1-read-update E2E" integration details
    And she clicks on the "Done" button
      # wait for integration to get in active state
    Then she wait until integration "CRUD1-read-update E2E" get into "Active" state


#
#  2. select - insert
#
  @db-connection-crud-2-read-create
  Scenario: Create integration to test DB connector for read and create operations
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    # time_unit_id to be specified after new update is available:
#    Then she fills period input with "1" value
#    Then she selects "Minutes" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
#    Then she fills "query" action configure component input with "INSERT INTO TODO(task) VALUES(:#task)" value
    Then she fills invoke query input with "INSERT INTO TODO(task) VALUES(:#task)" value
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "company" to "TASK"
    And she creates mapping from "first_name" to "TASK"
    And she creates mapping from "last_name" to "TASK"
    And she creates mapping from "lead_source" to "TASK"

#    And scroll "top" "right"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she defines integration name "CRUD2-read-create E2E"
    And clicks on the "Publish" button
      # assert integration is present in list
    #(this step is temporarily commented-out, there is no more h1 label with integration name there, syndesis #430 )
#    Then Camilla is presented with "CRUD2-read-create E2E" integration details
    And she clicks on the "Done" button
      # wait for integration to get in active state
    Then she wait until integration "CRUD2-read-create E2E" get into "Active" state


#
#  3. select - delete
#
  @db-connection-crud-3-read-delete
  Scenario: Create integration to test DB connector for read and delete operations
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "10000" value
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #DELETE FROM todo where taks like '%${first_name}%';:
#    Then she fills "query" action configure component input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value

    And clicks on the "Done" button

    # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"

    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she defines integration name "CRUD3-read-delete E2E"
    And clicks on the "Publish" button
    #(this step is temporarily commented-out, there is no more h1 label with integration name there, syndesis #430 )
#    Then Camilla is presented with "CRUD3-read-delete E2E" integration details
    And she clicks on the "Done" button
    Then she wait until integration "CRUD3-read-delete E2E" get into "Active" state

#
#  4. select - create (via buildin procedure)
#
  @db-connection-crud-4-read-update-inbuilt
  Scenario: Create integration to test DB connector for read and create operations via stored procedure
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    # time_unit_id to be specified after new update is available:
#    Then she fills period input with "1" value
#    Then she selects "Minutes" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke Stored Procedure" integration action
    And she selects "add_lead" from "procedureName" dropdown
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "company" to "company"
    And she creates mapping from "first_name" to "first_and_last_name"
    And she creates mapping from "last_name" to "first_and_last_name"
    And she creates mapping from "lead_source" to "lead_source"

#    And scroll "top" "right"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she defines integration name "CRUD4-read-create-inbuilt E2E"
    And clicks on the "Publish" button
    #(this step is temporarily commented-out, there is no more h1 label with integration name there, syndesis #430 )
#    Then Camilla is presented with "CRUD4-read-create-inbuilt E2E" integration details
    And she clicks on the "Done" button
    Then she wait until integration "CRUD4-read-create-inbuilt E2E" get into "Active" state


#
#  5. builtin sql query checker
#
  @db-connection-sqlquery-checker
  Scenario: Create integration to test inbuilt sql query checker for basic operations: (SELECT, INSERT, UPDATE, DELETE)
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"

    Then she checks "Done" button is "Disabled"
#    wrong query:
    Then she fills periodic query input with "SELECT * FROM CONTACT-A" value
    Then she fills period input with "5000" value
    And she is presented with sql-warning
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    # time_unit_id to be specified after new update is available:
#    Then she fills period input with "1" value
#    Then she selects "Minutes" from "time_unit_id" dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
#    Then she fills "query" action configure component input with "UPDATE TODO-A SET completed=1 WHERE task like '%:#task%'" value
    Then she fills invoke query input with "UPDATE TODO-A SET completed=1 WHERE task like '%:#task%'" value
    And she is presented with sql-warning
#    Then she fills "query" action configure component input with "DELETE FROM TODO-A WHERE task like '%:#task%'" value
    Then she fills invoke query input with "DELETE FROM TODO-A WHERE task like '%:#task%'" value
    And she is presented with sql-warning
#    Then she fills "query" action configure component input with "INSERT INTO TODO-A(task) VALUES(:#task)" value
    Then she fills invoke query input with "INSERT INTO TODO-A(task) VALUES(:#task)" value
    And she is presented with sql-warning
#    correct one:
#    Then she fills "query" action configure component input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    Then she fills invoke query input with "DELETE FROM TODO WHERE task like '%:#task%'" value
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"

    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she defines integration name "SQL query checker E2E"
    And clicks on the "Publish" button
      # assert integration is present in list
    #(this step is temporarily commented-out, there is no more h1 label with integration name there, syndesis #430 )
#    Then Camilla is presented with "SQL query checker E2E" integration details
    And she clicks on the "Done" button
      # wait for integration to get in active state
    Then she wait until integration "SQL query checker E2E" get into "Active" state
