# @sustainer: jsafarik@redhat.com

@ui
@datamapper
Feature: Data Mapper

  Background: Clean application state
    Given clean application state
    And log into the Syndesis
    And navigate to the "Home" page

  @ENTESB-11959
  @mapped-duplicate-fields
  Scenario: Check mapping doesn't create duplicate fields
    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Next" button

    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    When fill in invoke query input with "INSERT INTO todo (task) VALUES (:#task)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And sleep for 2 seconds
    And create data mapper mappings
      | task | task |
    And click on the "Done" button

    And edit integration step on position 2
    Then check element with id "task" is present 2 times

  @reproducer
  @ENTESB-11870
  @map-collection-to-single
  Scenario: Map values from collection to single entry

    Given truncate "todo" table
    When inserts into "todo" table
      | task1 |
      | task2 |
      | task3 |

    And click on the "Create Integration" link
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # postgres start connection that provides a value
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    # select Log as 'to' point
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (task) VALUES (:#task)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | task | task |
    And click on the "Done" button

    # save the integration with new name and publish it
    When click on the "Publish" link
    And set integration name "11870"
    And publish integration
    Then Integration "11870" is present in integrations list
    And wait until integration "11870" gets into "Running" state

    # validate that all items from db are present
    And wait until integration 11870 processed at least 1 message
    And validate that number of all todos with task "task1 task2 task3" is "1"