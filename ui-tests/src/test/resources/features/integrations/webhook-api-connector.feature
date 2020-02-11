# @sustainer: mkralik@redhat.com

@ui
@webhook
@api-connector
@datamapper
@integrations-webhook-api-connector
Feature: Integration - Webhook to API connector

  Background:
    Given log into the Syndesis
    And clean application state
    Then wait for Todo to become ready

    When Set Todo app credentials
    And click on the "Customizations" link
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
    And created connections
      | Todo connector | todo | Todo connection | no validation |
    And navigate to the "Home" page

  @reproducer
  @webhook-api-connector-new-task
  @gh-3729
  @gh-4125
  Scenario: Webhook to Custom API connector
    When click on the "Create Integration" link to create a new integration.
    Then check that position of connection to fill is "Start"

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
    Then check that position of connection to fill is "Finish"

    When select the "Todo connection" connection
    And select "Create new task" integration action
    And click on the "Next" button
    And add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When open data mapper collection mappings
    And create data mapper mappings
      | author | body.task |
    And click on the "Done" button
    And publish integration
    And set integration name "webhook-custom-api-connector-new-task"
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "webhook-custom-api-connector-new-task" gets into "Running" state

    When select the "webhook-custom-api-connector-new-task" integration
    And invoke post request to webhook in integration webhook-custom-api-connector-new-task with token test-webhook and body {"author":"New Author","title":"Book Title"}
    And sleep for jenkins delay or "3" seconds
    Then validate that number of all todos with task "New Author" is greater than "0"

  @reproducer
  @webhook-api-connector-list-tasks
  @gh-3727
  Scenario: Test to reproduce #3727
    When click on the "Create Integration" link to create a new integration.
    Then check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    When select the "Todo connection" connection
    And select "List all tasks" integration action
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "webhook-gh-3727"
    And publish integration

    When navigate to the "Integrations" page
    And wait until integration "webhook-gh-3727" gets into "Running" state
    And select the "webhook-gh-3727" integration

    # need to add a task, otherwise the /api path returns 404
    And inserts into "todo" table
      | any task |

    # here the original issue caused HTTP ERROR 500
    Then invoke post request to webhook in integration webhook-gh-3727 with token test-webhook and body {}
