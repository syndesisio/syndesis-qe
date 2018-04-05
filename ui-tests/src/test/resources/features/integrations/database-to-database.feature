@integrations-database-to-database
Feature: Integration - DB to DB

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis

    Given reset content of "todo" table
    Given reset content of "contact" table
#
#  1. select - update
#
  @db-connection-crud-1-read-update
  Scenario: Read & update operations

    Then inserts into "todo" table
      | Joe |

    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #wip this query doesnt work ftb #698
    Then she fills invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
#    there is no done button:
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "first_name" to "TASK"

    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD1-read-update E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "CRUD1-read-update E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "CRUD1-read-update E2E" gets into "Published" state

    Then validate that all todos with task "Joe" have value completed "1", period in ms: "5000"


#
#  2. select - insert
#
  @db-connection-crud-2-read-create
  Scenario: Read & create operations
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
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

    Then validate that all todos with task "Joe" have value completed "2", period in ms: "5000"

#
#  3. select - delete
#
  @db-connection-crud-3-read-delete
  Scenario: Read & delete operations
    Then inserts into "todo" table
      | Joe |
    Then inserts into "todo" table
      | Jimmy |

    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT WHERE first_name = 'Joe'" value
    Then she fills period input with "10" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    #wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task = :#TASK" value
    And clicks on the "Done" button

    # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
#    And she stays there for "16000" ms
    And she creates mapping from "first_name" to "TASK"

    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD3-read-delete E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "CRUD3-read-delete E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "CRUD3-read-delete E2E" gets into "Published" state

    Then validate that number of all todos with task "Joe" is "0", period in ms: "5000"
    Then validate that number of all todos with task "Jimmy" is "1", period in ms: "1"

#
#  4. select - create (via buildin procedure)
#
  @db-connection-crud-4-read-update-inbuilt
  Scenario: Read & create operations on stored procedure
      # INSERT INTO CONTACT(first_name, last_name, company, lead_source) VALUES('Josef','Stieranka','Istrochem','db');


    When inserts into "contact" table
      | Josef | Stieranka | Istrochem | db |

    Then "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "10" value
    Then she selects "Seconds" from sql dropdown
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
    And she creates mapping from "last_name" to "first_and_last_name"
    And she creates mapping from "lead_source" to "lead_source"

#    And scroll "top" "right"
    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "CRUD4-read-create-inbuilt E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "CRUD4-read-create-inbuilt E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "CRUD4-read-create-inbuilt E2E" gets into "Published" state
    Then validate add_lead procedure with last_name: "Stieranka", company: "Istrochem", period in ms: "10000"



#
#  5. builtin sql query checker
#
  @db-connection-5-sqlquery-checker
  Scenario: Sql query checker
    Then inserts into "todo" table
      | Joe |
    Then inserts into "todo" table
      | Jimmy |

    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
#    wrong query:
    Then she fills periodic query input with "SELECT * FROM CONTACT-A" value
    Then she fills period input with "5" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button
    And she can see alert notification
    #@wip time_unit_id is not yet available

    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    And clicks on the "Done" button

    # select postgresDB connection as 'to' point
    #Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "UPDATE TODO-A SET completed=1 WHERE task like '%:#TASK%'" value
    And clicks on the "Done" button
    And she can see alert notification
    Then she fills invoke query input with "DELETE FROM TODO-A WHERE task like '%:#TASK%'" value
    And clicks on the "Done" button
    And she can see alert notification
    Then she fills invoke query input with "INSERT INTO TODO-A(task) VALUES(:#TASK)" value
    And clicks on the "Done" button
    And she can see alert notification
#    correct one:
    #wip this query doesnt work ftb #698
    Then she fills invoke query input with "DELETE FROM TODO WHERE task = :#TASK" value
    And clicks on the "Done" button

      # add data mapper step
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
#    And she stays there for "5000" ms
    And she creates mapping from "first_name" to "TASK"

    And clicks on the "Done" button
    And clicks on the "Publish" button
    And she sets the integration name "DB Connection 5 SQL query checker E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "DB Connection 5 SQL query checker E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "DB Connection 5 SQL query checker E2E" gets into "Published" state

    Then validate that number of all todos with task "Joe" is "0", period in ms: "5000"
    Then validate that number of all todos with task "Jimmy" is "1", period in ms: "1"
