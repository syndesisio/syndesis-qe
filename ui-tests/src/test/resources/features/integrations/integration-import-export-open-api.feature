# @sustainer: acadova@redhat.com

@ui
@api-provider
@api-connector
@import
@export

Feature: API Provider Integration - Import Export

  # TODO: import/export tests with OpenAPI resource referenced from api-provider-based integration

  Background:
    Given log into the Syndesis
    And clean application state
    And truncate "todo" table
    And Set Todo app credentials

  @import-export-open-api

  Scenario: Create an integration with custom API connector and then export and import the integration

    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | file | swagger/connectors/todo.swagger.yaml |

    When click on the "Next" button
    Then check visibility of page "Review Actions"

    When click on the "Next" link
    Then check visibility of page "Specify Security"

    And click on the "Next" button
    And fill in values by element data-testid
      | name     | Todo connector |
      | basepath | /api           |
    And fill in TODO API host URL
    And click on the "Save" button

    When created connections
      | Todo connector | todo | Todo connection | no validation |
    And navigate to the "Home" page

    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select API Provider operation flow Create new task
    Then check flow title is "Create new task"

    When add integration step on position "0"

    When select the "Todo connection" connection
    Then select "Create new task" integration action
    And click on the "Next" button

    When add integration step on position "1"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And sleep for jenkins delay or "2" seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "3"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And sleep for jenkins delay or "2" seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    When execute POST on API Provider route i-todo-integration endpoint "/api/" with body '{"id":1,"completed":1,"task":"task1"}'
    Then verify response has status 201
    And verify response has body
      """
        {"id":1,"completed":1,"task":"task1"}
      """
    And validate that all todos with task "task1" have value completed "1", period in ms: "1000"
    And validate that number of all todos with task "task1" is "1"



    And select the "TODO Integration" integration
    Then check visibility of "TODO Integration" integration details
    When clean webdriver download folder
    And export the integraion

    # now we have exported integration, we can clean state and try to import
    And clean application state
    And log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" link
    Then import integration "TODO Integration"

    When navigate to the "Integrations" page
    Then Integration "TODO Integration" is present in integrations list
    Then wait until integration "TODO Integration" gets into "Stopped" state

    When select the "TODO Integration" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    And sleep for jenkins delay or "3" seconds

    And click on the "Edit Integration" link
    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then Integration "TODO Integration" is present in integrations list

    Then wait until integration "TODO Integration" gets into "Running" state
    When execute POST on API Provider route i-todo-integration endpoint "/api/" with body '{"id":1,"completed":1,"task":"task1"}'
    Then verify response has status 201
    And verify response has body
      """
        {"id":1,"completed":1,"task":"task1"}
      """
    And validate that all todos with task "task1" have value completed "1", period in ms: "1000"
    And validate that number of all todos with task "task1" is "1"





