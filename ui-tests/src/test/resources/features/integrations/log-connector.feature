# @sustainer: mcada@redhat.com

@log-connector
Feature: Log Connector

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis
    And created connections
      | Log   | no credentials | Log   | Log description   |
    And navigate to the "Home" page

#
#  1. Check that log message exists
#
  @log-connector-error-message
  Scenario: Check log message

    # create integration
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Done" button is "Disabled"

    When fill in periodic query input with "select * from contact limit(1)" value
    And fill in period input with "1" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And select "Simple Logger" integration action
    And fill in values
      | log level      | ERROR |
      | Log Body       | true  |
      | Log message Id | true  |
      | Log Headers    | true  |
      | Log everything | true  |

    Then click on the "Done" button

    When click on the "Save as Draft" button
    And set integration name "Integration_with_log"
    And click on the "Publish" button
    Then check visibility of "Integration_with_log" integration details

    When navigate to the "Integrations" page
    Then Integration "Integration_with_log" is present in integrations list
    And wait until integration "Integration_with_log" gets into "Running" state

    When sleep for "10000" ms
    Then validate that logs of integration "integration_with_log" contains string "Red Hat"
