# @sustainer: jsafarik@redhat.com

@ui
@odata-v2
@long-running
Feature: OData V2 Connector

  Background: Clean application state and get new key from sample service
    Given clean application state
    And create OData v2 credentials
    And reset OData v2 service
    And log into the Syndesis
    And created connections
      | OData V2 | odata V2 | OData | sample OData service |
    And navigate to the "Home" page

#
# 1. Parameterized tests for read action.
#
  @integrations-odata-v2-read
  Scenario Outline: Read <name> from OData v2 service tests

    # Create new integration
    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # Add OData connection as start step
    When selects the "OData" connection
    And select "Read" integration action
    When fill in values by element data-testid
      | resourcepath | Suppliers       |
      | keypredicate | <key_predicate> |
      | queryparams  | <query>         |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    # Add Log step as finish step
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | false |
      | bodyloggingenabled    | true  |
    Then click on the "Next" button

    # Save integration and publish it
    When click on the "Publish" link
    And set integration name "OData_Read_2_Log_<name>"
    And publish integration
    Then Integration "OData_Read_2_Log_<name>" is present in integrations list
    And wait until integration "OData_Read_2_Log_<name>" gets into "Running" state

    #Validate logs output
    When wait until integration OData_Read_2_Log_<name> processed at least 1 message

    Then validate that logs of integration "OData_Read_2_Log_<name>" <does_contain_1> string "<validate_string_1>"
    And validate that logs of integration "OData_Read_2_Log_<name>" <does_contain_2> string "<validate_string_2>"

    Examples:
      | name               | key_predicate | query                           | does_contain_1 | validate_string_1 | does_contain_2  | validate_string_2 |
      | entity collection  |               |                                 | contains       | Sammamish         | contains        | Tokyo Traders     |
      | single entity      | 1             |                                 | contains       | Redmond           | doesn't contain | Exotic Liquids    |
      | property of entity | 1/Name        |                                 | contains       | Tokyo Traders     | doesn't contain | Redmond           |
      | with expand query  |               | $filter=Name eq 'Tokyo Traders' | contains       | NE 40th           | doesn't contain | Sammamish         |
      | complex property   | 1/Address     |                                 | contains       | 98052             | doesn't contain | Tokyo Traders     |

  #
  # 2. Parametrized tests for Create and Update operations
  #
  @integrations-odata-v2-create
  Scenario: Create entity in OData service

    When reset content of "todo" table
    And insert into "todo with id" table
      | 3 | Jianathan |

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    And fill in periodic query input with "SELECT * FROM todo WHERE task = 'Jianathan'" value
    And fill in period input with "1" value
    And select "Hours" from sql dropdown
    And click on the "Next" button
    And check that position of connection to fill is "Finish"

    When select the "OData" connection
    And select "Create" integration action
    And select "Suppliers" from "resourcepath" dropdown
    When fill in values by element data-testid
      | resourcepath    | Categories |
      | omitjsonwrapper | true       |
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | task | Name |
      | id   | ID   |
    And click on the "Done" button

    #Then check visibility of page "add to integration"
    When click on the "Publish" link
    And set integration name "OData_Create"
    And publish integration
    Then Integration "OData_Create" is present in integrations list
    And wait until integration "OData_Create" gets into "Running" state
    And wait until integration OData_Create processed at least 1 message

    Then validate that OData V2 service contains entity with "Name":"Jianathan" property:value pair in "Categories" collection

  #
  # 3. Test for Delete operation
  #
  @integrations-odata-v2-delete
  Scenario: Delete entity in OData service

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When selects the "OData" connection
    And select "Read" integration action
    #Then check visibility of the "Done" button
    When select "Suppliers" from "resourcepath" dropdown
    When fill in values by element data-testid
      | resourcepath   | Categories |
      | keypredicate   | 1          |
      | delay          | 10         |
      | delay-duration | Minutes    |
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    When selects the "OData" connection
    And select "Delete" integration action
    When select "Suppliers" from "resourcepath" dropdown
    When select "Categories" from "resourcepath" dropdown
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | ID | keyPredicate |
    And click on the "Done" button

    # Executing this step just to make sure the entity is present before deleting it.
    And check that OData V2 "1" entity in "Categories" collection contains
      |  |
    When click on the "Publish" link
    And set integration name "OData_Delete"
    And publish integration
    Then Integration "OData_Delete" is present in integrations list
    And wait until integration "OData_Delete" gets into "Running" state
    And wait until integration OData_Delete processed at least 1 message

    Then check that entity "1" is not present in "Categories" collection on OData V2 service

#
# 3. Test for Delete operation
#
  @integrations-odata-v2-update
  Scenario: Update entity in OData service tests

    When reset content of "todo" table
    And insert into "todo with id" table
      | 1 | Jianathan |

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    And fill in periodic query input with "SELECT * FROM todo WHERE task = 'Jianathan'" value
    And fill in period input with "1" value
    And select "Hours" from sql dropdown
    And click on the "Next" button
    And check that position of connection to fill is "Finish"

    When select the "OData" connection
    And select "Update" integration action
    And select "Suppliers" from "resourcepath" dropdown
    When fill in values by element data-testid
      | resourcepath    | Categories |
      | omitjsonwrapper | true       |
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | task | Name         |
      | id   | ID           |
      | id   | keyPredicate |
    And click on the "Done" button

    #Then check visibility of page "add to integration"
    When click on the "Publish" link
    And set integration name "OData_Update"
    And publish integration
    Then Integration "OData_Update" is present in integrations list
    And wait until integration "OData_Update" gets into "Running" state
    And wait until integration OData_Update processed at least 1 message

    Then validate that OData V2 service contains entity with "Name":"Jianathan" property:value pair in "Categories" collection
