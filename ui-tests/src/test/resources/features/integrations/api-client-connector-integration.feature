@api-connector-integration
Feature: Integration - DB to API

  Background:
    Given "Camilla" logs into the Syndesis
    Given clean application state
    Given Set Todo app credentials

  @DB-to-TODO-custom-api-connector-integration
  Scenario: Create
    When Camilla creates new API connector
      | source    | file            | swagger/connectors/todo.swagger.yaml  |
      | security  | authType        | HTTP Basic Authentication             |
      | details   | connectorName   | Todo connector                        |
      | details   | routeHost       | todo                                  |
      | details   | baseUrl         |                                       |

    Then creates connections without validation
      | Todo connector | todo | Todo connection | |

#    Then creates integration
    Then "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.

    Then Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button

    Then Camilla selects the "Todo connection" connection
    And she selects "Create new task" integration action
    And clicks on the "Done" button

    Then Camilla clicks on the "Add a Step" button
    When she selects "Data Mapper" integration step
    And she creates mapping from "last_name" to "body.task"
    And clicks on the "Done" button

    Then click on the "Publish" button
    And she sets the integration name "Todo integration"
    Then click on the "Publish" button

    When "Camilla" navigates to the "Integrations" page
    Then she waits until integration "Todo integration" gets into "Published" state

    When she goes to Todo app
    Then she checks Todo list grows in "15" second
