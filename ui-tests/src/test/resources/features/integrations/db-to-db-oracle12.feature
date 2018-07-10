# @sustainer: mastepan@redhat.com

@integrations-db-to-db-oracle12
Feature: Integration - DB to DB oracle12

  Background: Allocating Oracle Database
    Given allocate new "oracle12cR1" database for "Oracle12" connection
    Given clean application state
    Given log into the Syndesis
    Given import extensions from syndesis-extensions folder
      | syndesis-library-jdbc-driver |
    And wait until "meta" pod is reloaded
    Given create standard table schema on "oracle12" driver
    When inserts into "contact" table on "oracle12"
      | Josef_oracle12 | Stieranka | Istrochem | db |
    Given created connections
      | Database | Oracle12 | Oracle12 | Oracle 12 RC1 |

#
#  2. select - insert
#
  @db-connection-crud-2-read-create-oracle12
  Scenario: Read & create operations
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "Oracle12" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Done" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Seconds" from sql dropdown
    And click on the "Done" button

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "Oracle12" connection
    And select "Invoke SQL" integration action
#    @wip - bug to be reported, wrong is: (:#TASK, 2). (:#TASK, :#MASK) is OK
    Then fill in invoke query input with "INSERT INTO TODO(task, completed) VALUES (:#TASK, 2)" value
    And click on the "Done" button

      # add data mapper step
    Then check visibility of page "Add to Integration"
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create mapping from "FIRST_NAME" to "TASK"

#    And scroll "top" "right"
    And click on the "Done" button
    And click on the "Publish" button
    And set integration name "CRUD2-read-create E2E"
    And click on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then check visibility of "CRUD2-read-create E2E" integration details
    Then navigate to the "Integrations" page
    Then wait until integration "CRUD2-read-create E2E" gets into "Running" state

    Then validate that all todos with task "Josef_oracle12" have value completed "2", period in ms: "5000" on "oracle12"

    And free allocated "oracle12cR1" database
