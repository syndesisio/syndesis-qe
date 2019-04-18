# @sustainer: jsafarik@redhat.com

# Since there is not a stable sample OData service to use
# this feature should be used only for manual testing.
# Sample OData service for testing TBD.

@ui
@odata
Feature: OData Connector

  Background: Clean application state and get new key from sample service
    Given clean application state
    And deploy OData server
    And create OData credentials
    And log into the Syndesis
    And created connections
      | OData | odata | OData | sample OData service |
    And navigate to the "Home" page
    And reset OData service

#
# 1. Parameterized tests for read action.
#
  @smoke
  @integrations-odata-read
  Scenario Outline: Read <name> from OData service tests

    # Create new integration
    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # Add OData connection as start step
    When selects the "OData" connection
    And select "Read" integration action
    # check button to make sure correct page is loaded
    Then check visibility of the "Done" button
    When fill in values by element ID
      | resourcePath | Products        |
      | keyPredicate | <key_predicate> |
      | queryParams  | <query>         |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"

    # Add Log step as finish step
    When select the "Log" connection
    Then check visibility of the "Done" button
    And fill in values
      | Message Body | true |
    Then click on the "Done" button

    # Save integration and publish it
    Then check visibility of page "add to integration"
    When click on the "Save" button
    And set integration name "OData_Read_2_Log"
    And publish integration
    Then Integration "OData_Read_2_Log" is present in integrations list
    And wait until integration "OData_Read_2_Log" gets into "Running" state

    #Validate logs output
    When sleep for "10000" ms
    Then validate that logs of integration "OData_Read_2_Log" <does_contain_1> string "<validate_string_1>"
    And validate that logs of integration "OData_Read_2_Log" <does_contain_2> string "<validate_string_2>"

    Examples:
      | name               | key_predicate | query                                                       | does_contain_1 | validate_string_1                                       | does_contain_2  | validate_string_2        |
      | entity collection  |               |                                                             | contains       | 1UMTS PDA                                               | contains        | Notebook Professional 17 |
      | single entity      | 2             |                                                             | contains       | 1UMTS PDA                                               | doesn't contain | Notebook Professional 17 |
      | property of entity | 2/Description |                                                             | contains       | Ultrafast 3G UMTS/HSDPA Pocket PC, supports GSM network | doesn't contain | 1UMTS PDA                |
      | with $expand query |               | $filter=Name eq 'Notebook Professional 17'&$expand=Category | contains       | Notebooks                                               | doesn't contain | 1UMTS PDA                |


  #
  # 2. Parametrized tests for Create and Update operations
  #
  @smoke
  @integrations-odata-create
  Scenario: Create entity in OData service

    When reset content of "contact" table
    And inserts into "contact" table
      | Jianathan | Yang | RH | lead_source |

    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    And fill in periodic query input with "SELECT * FROM contact WHERE first_name = 'Jianathan'" value
    And fill in period input with "1" value
    And select "Hours" from sql dropdown
    And click on the "Next" button
    And check that position of connection to fill is "Finish"

    When select the "OData" connection
    And select "Create" integration action
    # check button to make sure correct page is loaded
    Then check visibility of the "Done" button
    When select "Products" from "resourcePath" dropdown
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | first_name | Name        |
      | last_name  | Description |
    And click on the "Done" button

    Then check visibility of page "add to integration"
    When click on the "Save" button
    And set integration name "OData_Create"
    And publish integration
    Then Integration "OData_Create" is present in integrations list
    And wait until integration "OData_Create" gets into "Running" state
    When sleep for "10000" ms

    Then validate that OData service contains entity with "Name":"Jianathan" property:value pair in "Products" collection

  #
  # 3. Test for Delete operation
  #
  @gh-4889
  @reproducer
  @integrations-odata-delete
  Scenario: Delete entity in OData service

    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When selects the "OData" connection
    And select "Read" integration action
    Then check visibility of the "Done" button
    When fill in values by element ID
      | resourcePath | Products |
      | keyPredicate | 1        |
      | delay        | 10       |
      | select-delay | Minutes  |
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When selects the "OData" connection
    And select "Delete" integration action
    Then check visibility of the "Next" button
    When select "Products" from "resourcePath" dropdown
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | ID | keyPredicate |
    And click on the "Done" button

    Then check visibility of page "add to integration"
    When click on the "Save" button
    And set integration name "OData_Delete"
    And publish integration
    Then Integration "OData_Delete" is present in integrations list
    And wait until integration "OData_Delete" gets into "Running" state
    When sleep for "10000" ms

    Then check that entity "1" is not present in "Products" collection on OData service