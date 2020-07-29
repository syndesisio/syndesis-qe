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
    When insert into "todo" table
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
    And validate that number of all todos with task "task1 task2 task3" is 1

  @ENTESB-13935
  @data-mapper-reconfigure
  Scenario: Check reconfiguring datamapper doesn't delete the mappings
    Given truncate "todo" table

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"author":"New Author","title":"Book Title"} |
    And click on the "Next" button

    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (task) VALUES (:#task)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | author | task |
    And click on the "Done" button

    And edit integration step on position 2
    And click on the "Done" button

    When click on the "Publish" link
    And set integration name "Datamapper-reconfigure"
    And publish integration

    And wait until integration "Datamapper-reconfigure" gets into "Running" state
    And invoke post request to webhook in integration Datamapper-reconfigure with token test-webhook and body {"author":"New Author","title":"Book Title"}
    And wait until integration Datamapper-reconfigure processed at least 1 message
    Then validate that number of all todos with task "New Author" is greater than 0
