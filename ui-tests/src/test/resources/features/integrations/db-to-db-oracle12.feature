# @sustainer: alice.rum@redhat.com

@ui
@notOsd
@oracle12
@database
@datamapper
@integrations-db-to-db-oracle12
Feature: Integration - DB to DB oracle12

  Background: Allocating Oracle Database
    Given allocate new "oracle12cR1" database for "Oracle12" connection
    Given clean application state
    Given log into the Syndesis
    Given import extensions from syndesis-extensions folder
      | syndesis-library-jdbc-driver |
#    And wait until "meta" pod is reloaded  TODO: OCP4.9 the pod is reloaded to early, so the code use that actual pod as previous
    And sleep for "30000" ms
    Given create standard table schema on "oracle12" driver
    When insert into "contact" table on "oracle12"
      | Josef_oracle12 | Stieranka | Istrochem | db |
    Given created connections
      | Database | Oracle12 | Oracle12 | Oracle 12 RC1 |

#
#  2. select - insert
#
  @db-connection-crud-2-read-create-oracle12
  Scenario: Read and create operations on oracle
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "Oracle12" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "Oracle12" connection
    And select "Invoke SQL" integration action
#    @wip - bug to be reported, wrong is: (:#TASK, 2). (:#TASK, :#MASK) is OK
    Then fill in invoke query input with "INSERT INTO TODO(task, completed) VALUES (:#TASK, 2)" value
    And click on the "Next" button

      # add data mapper step
    # Then check visibility of page "Add to Integration"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | FIRST_NAME | TASK |

#    And scroll "top" "right"
    And click on the "Done" button
    And publish integration
    And set integration name "db-connection-crud-2-read-create-oracle12"
    And publish integration
    And wait until integration "db-connection-crud-2-read-create-oracle12" gets into "Running" state
    And wait until integration db-connection-crud-2-read-create-oracle12 processed at least 1 message

    Then validate that all todos with task "Josef_oracle12" have value completed 2, period in ms: 5000 on "oracle12"

    And free allocated "oracle12cR1" database

  @reproducer
  @ENTESB-17710
  @db-oracle12-lowercase-table-name
  Scenario: Read and create operations on oracle
    When execute SQL command "CREATE TABLE todo2 ( id int, task VARCHAR(250), completed int)" on "oracle12" driver
    And execute SQL command "INSERT INTO todo2(task) VALUES('test1')" on "oracle12" driver

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period        | 1       |
      | select-period | Minutes |
    And click on the "Next" button

    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | false |
      | bodyloggingenabled    | true  |
    Then click on the "Next" button

        #FHIR transaction
    When add integration step on position "0"
    And select the "Oracle12" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo2 WHERE task=:#name" value
    And click on the "Done" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    When define constant "param" with value "test1" of type "String" in data mapper
    And create data mapper mappings
      | param | name |
    Then click on the "Done" button

    When publish integration
    And set integration name "reproducer-17710"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "reproducer-17710" gets into "Running" state
    And wait until integration reproducer-17710 processed at least 1 message
    Then validate that logs of integration "reproducer-17710" contains string "test1"
