# @sustainer: mkralik@redhat.com
# https://github.com/syndesisio/syndesis-quickstarts/tree/master/db-2-db

@quickstart
@database
@datamapper
@quickstart-db-2-db
#(https://issues.jboss.org/browse/FUSEQE-4592)
@ignore
Feature: Quickstart db to db

  Background: Clean application state and prepare what is needed
    Given log into the Syndesis
    And reset content of "TODO" table
    And reset content of "CONTACT" table
    And clean application state

  @quickstart-solution
  Scenario: Import and run solution
    When navigate to the "Integrations" page
    And click on the "Import" button
    And import integration from relative file path "./src/test/resources/quickstarts/DB-2-DB-export.zip"

    And navigate to the "Integrations" page
    And Integration "DB-2-DB" is present in integrations list
    And wait until integration "DB-2-DB" gets into "Stopped" state
    And select the "DB-2-DB" integration
    And click on the "Edit Integration" button

    Then check there are 3 integration steps
    And check that 1. step has Periodic SQL invocation title
    And check that 2. step has Data Mapper title
    And check that 3. step has Invoke SQL title

    When publish integration
    And navigate to the "Integrations" page
    And wait until integration "DB-2-DB" gets into "Running" state
    And sleep for "60000" ms

    And select the "DB-2-DB" integration
    And click on the "Activity" tab
    Then check that 1. step in the 1. activity is Data Mapper step
    And check that 2. step in the 1. activity is Invoke SQL step

    When navigate to Todo app
    Then check that "1". task on Todo app page contains text "Joe Jackson Red Hat"

  # https://youtu.be/BeVK5RCkog0
  @quickstart-video
  Scenario: Check process in the video
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "SELECT * FROM CONTACT" value
    And fill in values by element ID
      | schedulerExpression        | 1       |
      | select-schedulerExpression | Minutes |
    And click on the "Done" button

    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO TODO (task, completed) VALUES(:#task, 0)" value
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When open data mapper collection mappings
    And create data mapper mappings
      | first_name; last_name; company | task |
    And click on the "Done" button

    And publish integration
    And set integration name "DB-2-DB video"
    And publish integration
    And inserts into "contact" table
      | Joe | Jackson | Red Hat | db |

    And navigate to the "Integrations" page
    And wait until integration "DB-2-DB video" gets into "Running" state
    And sleep for "60000" ms

    And select the "DB-2-DB video" integration
    And click on the "Activity" tab
    Then check that 1. step in the 1. activity is Data Mapper step
    And check that 2. step in the 1. activity is Invoke SQL step

    When navigate to Todo app
    Then check that "1". task on Todo app page contains text "Joe Jackson Red Hat"
