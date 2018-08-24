@timer-connector
Feature: Timer Connector
#
  Background: Clean application state
    Given clean application state
    Given deploy AMQ broker and add accounts
    And navigate to the "Home" page

    And created connections
      | AMQ Message Broker   | AMQ            | AMQ   | AMQ connection    |
#      | Log   | no credentials | Log   | Log description   |

##
#  1. Check that log message exists
#
  @simple-timer
  Scenario: Check log message

    # create integration
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Timer" connection
    And select "Simple Timer" integration action
    And fill in values
      | period | 0.016 |

    Then check "Done" button is "Disabled"

    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "AMQ" connection
    And select "Publish Messages" integration action

    And fill in values
      | Destination Name | queue-simple-timer|
      | Destination Type | Queue          |
    And click on the "Next" button
    And click on the "Done" button

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
