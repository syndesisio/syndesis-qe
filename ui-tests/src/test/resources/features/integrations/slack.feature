@slack
Feature: Slack Connector

  Background: Clean application state
    Given clean application state
    Given reset content of "contact" table
    Given "Camilla" logs into the Syndesis


    Given created connections
      | Slack | QE Slack | QE Slack | SyndesisQE Slack test |

    And "Camilla" navigates to the "Home" page


#
#  1. Check that slack message exists
#
  @slack-check-message-body
  Scenario: Check message

    Given import extensions from syndesis-extensions folder
      | syndesis-extension-body  |
      | syndesis-connector-timer |

    Given created connections
      | Timer | no credentials | Timer | Timer description |

    And "Camilla" navigates to the "Home" page

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


#
#  2. Check that slack message exists, use data mapper
#
  @slack-check-message-data-mapper
  Scenario: Check message

    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections


    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "SELECT company FROM CONTACT limit(1)" value
    Then she fills period input with "200" value
    Then she selects "Seconds" from sql dropdown
    And clicks on the "Done" button



    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "QE Slack" connection
    And she selects "Channel" integration action
    And she fills in values
      | Channel | test |


    And Camilla clicks on the "Done" button


    # add data mapper step
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
    And she creates mapping from "company" to "message"

    And clicks on the "Done" button

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


    Then checks that last slack message equals "Red Hat" on channel "test"

