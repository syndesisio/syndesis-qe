# @sustainer: jsafarik@redhat.com

@ui
@odata-v4
@long-running
Feature: OData V4 Connector

  Background: Clean application state and get new key from sample service
    Given clean application state
    And deploy OData v4 server
    And create OData v4 credentials
    And log into the Syndesis
    And created connections
      | OData V4 | odata V4 | OData | sample OData service |
    And navigate to the "Home" page
    And reset OData v4 service

#
# 1. Parameterized tests for read action.
#
  @integrations-odata-v4-read
  Scenario Outline: Read <name> from OData v4 service tests

    # Create new integration
    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # Add OData connection as start step
    When selects the "OData" connection
    And select "Read" integration action
    When fill in values by element data-testid
      | resourcepath | Products        |
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
    And set integration name "OData_Read_4_Log_<name>"
    And publish integration
    Then Integration "OData_Read_4_Log_<name>" is present in integrations list
    And wait until integration "OData_Read_4_Log_<name>" gets into "Running" state

    #Validate logs output
    When wait until integration OData_Read_4_Log_<name> processed at least 1 message

    Then validate that logs of integration "OData_Read_4_Log_<name>" <does_contain_1> string "<validate_string_1>"
    And validate that logs of integration "OData_Read_4_Log_<name>" <does_contain_2> string "<validate_string_2>"

    Examples:
      | name               | key_predicate | query                                                       | does_contain_1 | validate_string_1                                       | does_contain_2  | validate_string_2        |
      | entity collection  |               |                                                             | contains       | 1UMTS PDA                                               | contains        | Notebook Professional 17 |
      | single entity      | 2             |                                                             | contains       | 1UMTS PDA                                               | doesn't contain | Notebook Professional 17 |
      | property of entity | 2/Description |                                                             | contains       | Ultrafast 3G UMTS/HSDPA Pocket PC, supports GSM network | doesn't contain | 1UMTS PDA                |
      | with expand query  |               | $filter=Name eq 'Notebook Professional 17'&$expand=Category | contains       | Notebooks                                               | doesn't contain | 1UMTS PDA                |

    @gh-5067
    Examples:
      | name             | key_predicate   | query | does_contain_1 | validate_string_1 | does_contain_2  | validate_string_2 |
      | complex property | 2/Configuration |       | contains       | MX150             | doesn't contain | 1UMTS PDA         |

  #
  # 2. Parametrized tests for Create and Update operations
  #
  @integrations-odata-v4-create
  Scenario: Create entity in OData service

    When reset content of "contact" table
    And insert into "contact" table
      | Jianathan | Yang | RH | lead_source |

    When click on the "Create Integration" link to create a new integration
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
    When select "Products" from "resourcepath" dropdown
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | last_name  | Description |
      | first_name | Name        |
    And click on the "Done" button

    #Then check visibility of page "add to integration"
    When click on the "Publish" link
    And set integration name "OData_4_Create"
    And publish integration
    Then Integration "OData_4_Create" is present in integrations list
    And wait until integration "OData_4_Create" gets into "Running" state
    And wait until integration OData_4_Create processed at least 1 message

    Then validate that OData service contains entity with "Name":"Jianathan" property:value pair in "Products" collection

  #
  # 3. Test for Delete operation
  #
  @gh-4889
  @reproducer
  @integrations-odata-v4-delete
  Scenario: Delete entity in OData service

    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When selects the "OData" connection
    And select "Read" integration action
    #Then check visibility of the "Done" button
    When fill in values by element data-testid
      | resourcepath   | Products |
      | keypredicate   | 1        |
      | delay          | 10       |
      | delay-duration | Minutes  |
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    When selects the "OData" connection
    And select "Delete" integration action
    When select "Products" from "resourcepath" dropdown
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | ID | keyPredicate |
    And click on the "Done" button

    # Executing this step just to make sure the entity is present before deleting it.
    And check that OData "1" entity in "Products" collection contains
      |  |
    When click on the "Publish" link
    And set integration name "OData_4_Delete"
    And publish integration
    Then Integration "OData_4_Delete" is present in integrations list
    And wait until integration "OData_4_Delete" gets into "Running" state
    And wait until integration OData_4_Delete processed at least 1 message

    Then check that entity "1" is not present in "Products" collection on OData service

  # Those bugs were very similar in reproducing - created one scenario outline for all of them
  @reproducer
    @integrations-odata-v4-read-update
  Scenario Outline: Read <name> from OData v4 service tests

    # Create new integration
    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # Add OData connection as start step
    When selects the "OData" connection
    And select "Read" integration action
    # check button to make sure correct page is loaded
    Then check visibility of the "Next" button
    When fill in values by element data-testid
      | resourcepath      | <resource_collection> |
      | keypredicate      | <key_predicate>       |
      | filteralreadyseen | <filter>              |
      | splitresult       | <split>               |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    # Add OData update as finish step
    When select the "OData" connection
    And select "Update" integration action
    When select "<resource_collection>" from "resourcepath" dropdown
    And click on the "Next" button

    # Add Data mapper (checks output datashape of OData read action is correct)
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | <mapping_one> | <mapping_two> |
      | ID            | keyPredicate  |
    And click on the "Done" button

    # Save integration and publish it
    When publish integration
    And set integration name "Odata_v4_<name>"
    And publish integration
    Then Integration "Odata_v4_<name>" is present in integrations list
    And wait until integration "Odata_v4_<name>" gets into "Running" state

    #Validate logs output
    And wait until integration Odata_v4_<name> processed at least 1 message

    Then validate that OData service contains entity with "<key>":"<value>" property:value pair in "<resource_collection>" collection

    @gh-5386
    Examples:
      | name            | resource_collection | key_predicate | filter | split | key         | value     | mapping_one | mapping_two |
      | wrong_datashape | Products            | 2             | false  | false | Description | 1UMTS PDA | Name        | Description |


    @gh-5533
    Examples:
      | name | resource_collection | key_predicate | filter | split | key         | value     | mapping_one | mapping_two |
      | NPE  | Products            | 2             | true   | true  | Description | 1UMTS PDA | Name        | Description |

    @gh-5241
    Examples:
      | name                            | resource_collection | key_predicate | filter | split | key       | value    | mapping_one | mapping_two |
      | string predicate without quotes | Users               | coolBob       | true   | true  | FirstName | CoolName | LastName    | FirstName   |

    @gh-5151
    Examples:
      | name                   | resource_collection | key_predicate | filter | split | key | value | mapping_one       | mapping_two       |
      | entity w complex param | Products            | 2             | true   | true  | CPU | MX150 | Configuration.GPU | Configuration.CPU |


  @gh-5060
  @reproducer
  @integrations-odata-v4-read-update
  Scenario: Read enum mapping from OData service tests

    # Create new integration
    When click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # Add OData connection as start step
    When selects the "OData" connection
    And select "Read" integration action
    # check button to make sure correct page is loaded
    Then check visibility of the "Next" button
    When fill in values by element data-testid
      | resourcepath      | Users   |
      | keypredicate      | coolBob |
      # for gh-6294 the filteralreadyseen can be set to true
      | filteralreadyseen | false   |
      | splitresult       | true    |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    # Add OData update as finish step
    When select the "OData" connection
    And select "Update" integration action
    When select "Users" from "resourcepath" dropdown
    And click on the "Next" button

    # Add Data mapper (checks output datashape of OData read action is correct)
    When add integration step on position "0"
    And select "Data Mapper" integration step
    When define constant "key" with value "whatever" of type "String" in data mapper
    And create data mapper mappings
      | Gender | Gender       |
      | key    | keyPredicate |
    And click on the "Done" button

    # Save integration and publish it
    When publish integration
    And set integration name "Enum_v4"
    And publish integration
    Then Integration "Enum_v4" is present in integrations list
    And wait until integration "Enum_v4" gets into "Running" state
    And wait until integration Enum_v4 processed at least 1 message

    #Validate logs output
    Then check that OData "whatever" entity in "Users" collection contains
      | Gender | MALE |

  @gh-5559
  @reproducer
  @odata-v4-server-certificate
  Scenario: OData connector needs server certificate
    When create OData HTTPS v4 credentials
    And log into the Syndesis
    And created connections
      | OData V4 | odataHttps | ODataHttps | sample OData service |
    Then check visibility of the "ODataHttps" connection
