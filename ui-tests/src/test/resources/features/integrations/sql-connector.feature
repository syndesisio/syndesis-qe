# @sustainer: jsafarik@redhat.com

@ui
@database
@log
@sql-connector
Feature: SQL Connector

  Background: Clean application state
    Given clean application state
    And log into the Syndesis
    And truncate "todo" table
    And navigate to the "Home" page

  #
  # 1. Check that SQL connector pulls all rows on exchange when SQL connector is as middle step
  # (having SQL connector as 'from' or 'to' doesn't cause issue from GH-3856)
  #
  @reproducer
  @gh-3856
  @sql-connector-all-rows
  Scenario: Check SQL pulls all rows

    When inserts into "todo" table
      | ABCsampleTODO |
      | XYZsampleTODO |
      | KLMsampleTODO |

    And click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select Timer as 'from' point
    When select the "Timer" connection
    And select "Simple Timer" integration action
    And fill in values by element ID
      | period        | 1       |
      | select-period | Minutes |
    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    # select Log as 'to' point
    When select the "Log" connection
    And fill in values
      | Message Context | true |
      | Message Body    | true |
    Then click on the "Done" button

    # select postgresDB as middle point
    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then check "Next" button is "Disabled"

    # fill in postgresDB connection
    When fill in invoke query input with "SELECT * FROM  todo" value
    And click on the "Next" button

    # save the integration with new name and publish it
    When click on the "Publish" button
    And set integration name "DB_2_Log"
    And publish integration
    Then Integration "DB_2_Log" is present in integrations list
    And wait until integration "DB_2_Log" gets into "Running" state

    # validate that all items from db are present
    When sleep for "10000" ms
    Then validate that logs of integration "DB_2_Log" contains string "ABCsampleTODO"
    And validate that logs of integration "DB_2_Log" contains string "XYZsampleTODO"
    And validate that logs of integration "DB_2_Log" contains string "KLMsampleTODO"

  @reproducer
  @gh-5016
  @sql-connector-predicates
  Scenario Outline: Check SQL connector with predicate <predicate>

    And inserts into "todo" table
      | task1 |
      | task2 |
      | task3 |
      | task4 |
      | task5 |

    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # postgres start connection that provides a value
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in invoke query input with "SELECT <value> AS id" value
    And click on the "Next" button

    # end connection
    And select the "Log" connection
    And fill in values
      | Message Context | true |
      | Message Body    | true |
    And click on the "Done" button

    # select postgresDB as middle point
    And add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT id FROM todo WHERE <predicate>" value
    And click on the "Next" button

    # add data mapper
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | id | id |
    And click on the "Done" button

    When add integration step on position "2"
    And select "Log" integration step
    And fill in values
      | Message Context | true |
      | Message Body    | true |
    Then click on the "Done" button

    And click on the "Publish" button
    And set integration name "predicates"
    And publish integration
    Then Integration "predicates" is present in integrations list
    And wait until integration "predicates" gets into "Running" state

    Then validate that logs of integration "predicates" contains items with IDs "<result>"

    # TODO: we might want to add more predicates, also function calls on either side etc
    Examples:
      | predicate             | value | result  |
      | id < :#id             | 3     | 1,2     |
      | id > :#id             | 3     | 4,5     |
      | id <= :#id            | 3     | 1,2,3   |
      | id >= :#id            | 3     | 3,4,5   |
      | id != :#id            | 3     | 1,2,4,5 |
      | id <= :#id AND id > 1 | 3     | 2,3     |
      | id BETWEEN :#id AND 4 | 2     | 2,3,4   |
      | id <= :#id OR id = 5  | 3     | 1,2,3,5 |


  # this is here because #5017 was deemed fixed and the failing cases are now tracked as an RFE
  # we should merge this with the tests for #5017 once #5840 is implemented
  @reproducer
  @gh-5084
  @sql-connector-predicates-rfe
  Scenario Outline: Check SQL connector with predicate <predicate>

    And inserts into "todo" table
      | task1 |
      | task2 |
      | task3 |
      | task4 |
      | task5 |

    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # postgres start connection that provides a value
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in invoke query input with "SELECT <value> AS id" value
    And click on the "Next" button

    # end connection
    And select the "Log" connection
    And fill in values
      | Message Context | true |
      | Message Body    | true |
    And click on the "Done" button

    # select postgresDB as middle point
    And add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT id FROM todo WHERE <predicate>" value
    And click on the "Next" button

    # add data mapper
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | id | id |
    And click on the "Done" button

    When add integration step on position "2"
    And select "Log" integration step
    And fill in values
      | Message Context | true |
      | Message Body    | true |
    Then click on the "Done" button

    And click on the "Publish" button
    And set integration name "predicates"
    And publish integration
    Then Integration "predicates" is present in integrations list
    And wait until integration "predicates" gets into "Running" state

    Then validate that logs of integration "predicates" contains items with IDs "<result>"

    Examples:
      | predicate        | value | result |
      | :#id > id        | 3     | 1,2    |
      | (id+1) < :#id    | 4     | 1,2    |
      | id in (1, :#id)  | 4     | 1,4    |
      | id = floor(:#id) | 4     | 4      |

  @reproducer
  @gh-4466
  @sql-connector-return-keys
  Scenario: Return generated keys for INSERT statement

    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    #Start step
    When select the "Timer" connection
    And select "Simple Timer" integration action
    And fill in values by element ID
      | period        | 1       |
      | select-period | Minutes |
    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    # Finish step
    When select the "Log" connection
    And fill in values
      | Message Context | false |
      | Message Body    | true  |
    Then click on the "Done" button

    # Select postgresDB as middle step and insert new value
    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (task, completed) VALUES ('task1', 0)" value
    And click on the "Next" button
    Then check visibility of page "Add to Integration"

    And publish integration
    And set integration name "Generated_Keys"
    And click on the "Publish" button

    # Wait for "Running" state and validate
    And wait until integration "Generated_Keys" gets into "Running" state
    And validate that logs of integration "Generated_Keys" contains items with IDs "1"

  @reproducer
  @gh-5493
  @sql-connector-return-keys-start-step
  Scenario: Return generated keys for INSERT statement from start step

    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # Start step
    And select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in invoke query input with "INSERT INTO todo (task, completed) VALUES ('task1', 0)" value
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    # Finish step
    When select the "Log" connection
    And fill in values
      | Message Context | false |
      | Message Body    | true  |
    Then click on the "Done" button


    And publish integration
    And set integration name "Generated_Keys"
    And click on the "Publish" button

    # Wait for "Running" state and validate
    And wait until integration "Generated_Keys" gets into "Running" state
    And validate that logs of integration "Generated_Keys" contains items with IDs "1"
