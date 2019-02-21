# @sustainer: mcada@redhat.com

@webhook
@api-connector
@datamapper
@integrations-webhook-api-connector
Feature: Integration - Webhook to API connector

  Background:
    Given log into the Syndesis
    And clean application state
    And wait for Todo to become ready
    And Set Todo app credentials
    And create new API connector
      | source   | file          | swagger/connectors/todo.swagger.yaml |
      | security | authType      | HTTP Basic Authentication            |
      | details  | connectorName | Todo connector                       |
      | details  | routeHost     | todo                                 |
      | details  | baseUrl       | /api                                 |

    And created connections
      | Todo connector | todo | Todo connection | no validation |

    And navigate to the "Home" page

  @reproducer
  @webhook-api-connector-new-task
  @gh-3729
  @gh-4125
  Scenario: Webhook to Custom API connector
    When click on the "Create Integration" button to create a new integration.
    Then check that position of connection to fill is "Start"
    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values
      | Webhook Token | test-webhook |
    And click on the "Next" button
    And fill in values
      | Select Type | JSON Instance |
    And fill in values by element ID
      | specification | {"author":"New Author","title":"Book Title"} |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"

    When select the "Todo connection" connection
    And select "Create new task" integration action
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | author | body.task |
    And click on the "Done" button
    And publish integration
    And set integration name "webhook-custom-api-connector-new-task"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "webhook-custom-api-connector-new-task" gets into "Running" state

    And select the "webhook-custom-api-connector-new-task" integration
    And invoke post request to webhook with body {"author":"New Author","title":"Book Title"}
    And sleep for jenkins delay or "3" seconds

    Then validate that number of all todos with task "New Author" is greater than "0"

  @reproducer
  @webhook-api-connector-list-tasks
  @gh-3727
  Scenario: Test to reproduce #3727
    When click on the "Create Integration" button to create a new integration.
    Then check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values
      | Webhook Token | test-webhook |
    And click on the "Next" button
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"

    When select the "Todo connection" connection
    And select "List all tasks" integration action

    And click on the "Save" button
    And set integration name "webhook-gh-3727"
    And publish integration

    When navigate to the "Integrations" page
    And wait until integration "webhook-gh-3727" gets into "Running" state
    And select the "webhook-gh-3727" integration
        # here the original issue caused HTTP ERROR 500
    And invoke post request to webhook with body {}
