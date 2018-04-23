@slack
Feature: Slack Connector

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis

    Given imported extensions
      | syndesis-extension-body-1.0.0.jar  | ../syndesis-extensions/syndesis-extension-body/target/  |
      | syndesis-connector-timer-1.0.0.jar | ../syndesis-extensions/syndesis-connector-timer/target/ |
    Given created connections
      | slack | QE Slack       | QE Slack | SyndesisQE Slack test |
      | Timer | no credentials | Timer    | Timer description     |

    And "Camilla" navigates to the "Home" page


#
#  1. Check that slack message exists
#
  @slack-check-message
  Scenario: Check message


    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections


    When Camilla selects the "Timer" connection
    And she selects "Simple Timer" integration action
    And she fills in values
      | period | 200s |
    And clicks on the "Next" button
    And clicks on the "Done" button


    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "QE Slack" connection
    And she selects "Channel" integration action
    And she fills in values
      | Channel | test |


    And Camilla clicks on the "Done" button

    When Camilla clicks on the "Add a Step" button

    # add data mapper step
    And she selects "Set Body" integration step
    And she fills in values
      | Body | test message |



    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Integration_with_slack"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Integration_with_slack" integration details
    And "Camilla" navigates to the "Integrations" page

    And Integration "Integration_with_slack" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "Integration_with_slack" gets into "Published" state


    Then checks that last slack message equals "test message" on channel "test"

