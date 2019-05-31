# @sustainer: mastepan@redhat.com

@ui
@mysql
@database
@datamapper
@integrations-db-to-db-mysql
Feature: Integration - DB to DB mysql

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    Given clean MySQL server
    Given deploy MySQL server
    And wait until mysql database starts
    Given create standard table schema on "mysql" driver

    When inserts into "contact" table on "mysql"
      | Josef_mysql | Stieranka | Istrochem | db |
    Given created connections
      | Database | MySQL | MySQL | Mysql on OpenShift |

#
#  2. select - insert
#
  @db-connection-crud-2-read-create-mysql
  Scenario: Read and create operations
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "MySQL" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "MySQL" connection
    And select "Invoke SQL" integration action
#    @wip - bug to be reported, wrong is: (:#TASK, 2). (:#TASK, :#MASK) is OK
    Then fill in invoke query input with "INSERT INTO TODO(task, completed) VALUES (:#TASK, 2)" value
    And click on the "Next" button

      # add data mapper step
    # Then check visibility of page "Add to Integration"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And open data mapper collection mappings
    And create mapping from "first_name" to "TASK"

#    And scroll "top" "right"
    And click on the "Done" button
    And publish integration
    And set integration name "CRUD2-read-create E2E"
    And publish integration
    Then wait until integration "CRUD2-read-create E2E" gets into "Running" state

    Then validate that all todos with task "Josef_mysql" have value completed "2", period in ms: "5000" on "mysql"
