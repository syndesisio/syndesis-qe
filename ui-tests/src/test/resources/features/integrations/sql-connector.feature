# @sustainer: jsafarik@redhat.com

@ui
@database
@log
@sql-connector
@long-running
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

    When insert into "todo" table
      | ABCsampleTODO |
      | XYZsampleTODO |
      | KLMsampleTODO |

    And click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select Timer as 'from' point
    When select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period        | 1       |
      | select-period | Minutes |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    # select Log as 'to' point
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |
    Then click on the "Next" button

    # select postgresDB as middle point
    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then check "Next" button is "Disabled"

    # fill in postgresDB connection
    When fill in invoke query input with "SELECT * FROM  todo" value
    And click on the "Next" button

    # save the integration with new name and publish it
    When click on the "Publish" link
    And set integration name "DB_2_Log"
    And publish integration
    Then Integration "DB_2_Log" is present in integrations list
    And wait until integration "DB_2_Log" gets into "Running" state

    # validate that all items from db are present
    And wait until integration DB_2_Log processed at least 1 message

    Then validate that logs of integration "DB_2_Log" contains string "ABCsampleTODO"
    And validate that logs of integration "DB_2_Log" contains string "XYZsampleTODO"
    And validate that logs of integration "DB_2_Log" contains string "KLMsampleTODO"

  @reproducer
  @sql-connector-predicates
  Scenario Outline: Check SQL connector with predicate <predicate>

    And insert into "todo" table
      | task1 |
      | task2 |
      | task3 |
      | task4 |
      | task5 |

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # postgres start connection that provides a value
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    And fill in invoke query input with "SELECT <value> AS id" value
    And click on the "Next" button

    # end connection
    And select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |
    And click on the "Next" button

    # select postgresDB as middle point
    And add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT id FROM todo WHERE <predicate>" value
    And click on the "Next" button

    # add data mapper
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And sleep for "2000" ms
    And create data mapper mappings
      | id | id |
    And click on the "Done" button

    And click on the "Publish" link
    And set integration name "predicates-<name>"
    And publish integration
    Then Integration "predicates-<name>" is present in integrations list
    And wait until integration "predicates-<name>" gets into "Running" state
    And wait until integration predicates-<name> processed at least 1 message

    Then validate that logs of integration "predicates-<name>" contains items with IDs "<result>"

    # TODO: we might want to add more predicates, also function calls on either side etc
    @gh-5016
    Examples:
      | name  | predicate             | value | result  |
      | test1 | id < :#id             | 3     | 1,2     |
      | test2 | id > :#id             | 3     | 4,5     |
      | test3 | id <= :#id            | 3     | 1,2,3   |
      | test4 | id >= :#id            | 3     | 3,4,5   |
      | test5 | id != :#id            | 3     | 1,2,4,5 |
      | test6 | id <= :#id AND id > 1 | 3     | 2,3     |
      | test7 | id BETWEEN :#id AND 4 | 2     | 2,3,4   |
      | test8 | id <= :#id OR id = 5  | 3     | 1,2,3,5 |
    @gh-5084
    @ENTESB-11487
    Examples:
      | name   | predicate        | value | result |
      | test9  | :#id > id        | 3     | 1,2    |
      | test10 | (id+1) < :#id    | 4     | 1,2    |
      | test11 | id in (1, :#id)  | 4     | 1,4    |
      | test12 | id = floor(:#id) | 4     | 4      |

  @reproducer
  @gh-4466
  @sql-connector-return-keys
  Scenario: Return generated keys for INSERT statement

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    #Start step
    When select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period        | 1       |
      | select-period | Minutes |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    # Finish step
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | false |
      | bodyloggingenabled    | true  |
    Then click on the "Next" button

    # Select postgresDB as middle step and insert new value
    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (task, completed) VALUES ('task1', 0)" value
    And click on the "Next" button
    # Then check visibility of page "Add to Integration"

    And click on the "Publish" link
    And set integration name "Generated_Keys"
    And publish integration

    # Wait for "Running" state and validate
    And wait until integration "Generated_Keys" gets into "Running" state
    And wait until integration Generated_Keys processed at least 1 message
    And validate that logs of integration "Generated_Keys" contains items with IDs "1"

  @reproducer
  @gh-5493
  @sql-connector-return-keys-start-step
  Scenario: Return generated keys for INSERT statement from start step

    When click on the "Create Integration" link to create a new integration
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
    And fill in values by element data-testid
      | contextloggingenabled | false |
      | bodyloggingenabled    | true  |
    Then click on the "Next" button


    And click on the "Publish" link
    And set integration name "Generated_Keys_2"
    And publish integration

    # Wait for "Running" state and validate
    And wait until integration "Generated_Keys_2" gets into "Running" state
    And wait until integration Generated_Keys_2 processed at least 1 message
    And validate that logs of integration "Generated_Keys_2" contains items with IDs "1"
