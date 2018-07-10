# @sustainer: mastepan@redhat.com

@api-connector-integration
Feature: Integration - DB to API

  Background:
    Given log into the Syndesis
    Given clean application state
    Given Set Todo app credentials

  @DB-to-TODO-custom-api-connector-integration
  Scenario: Create
    When create new API connector
      | source   | file          | swagger/connectors/todo.swagger.yaml |
      | security | authType      | HTTP Basic Authentication            |
      | details  | connectorName | Todo connector                       |
      | details  | routeHost     | todo                                 |
      | details  | baseUrl       | /api                                 |

    Then creates connections without validation
      | Todo connector | todo | Todo connection |  |

#    Then creates integration
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.

    Then select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Seconds" from sql dropdown
    And click on the "Done" button

    Then select the "Todo connection" connection
    And select "Create new task" integration action

    Then click on the "Add a Step" button
    When select "Data Mapper" integration step

    Then create data mapper mappings
      | last_name | body.task |

    And click on the "Done" button

    Then click on the "Publish" button
    And set integration name "Todo integration"
    Then click on the "Publish" button

    When navigate to the "Integrations" page
    Then wait until integration "Todo integration" gets into "Running" state

    When she goes to Todo app
    Then check Todo list grows in "15" second
