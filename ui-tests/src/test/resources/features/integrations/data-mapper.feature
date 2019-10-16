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
    And open data mapper collection mappings
    And create data mapper mappings
      | task | task |
    And click on the "Done" button

    And edit integration step on position 2
    And open data mapper collection mappings
    Then check element with id "task" is present 2 times