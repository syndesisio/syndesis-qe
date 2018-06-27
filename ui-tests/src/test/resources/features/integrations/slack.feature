@slack
Feature: Slack Connector

  Background: Clean application state
    Given clean application state
    Given reset content of "contact" table
    Given log into the Syndesis


    Given created connections
      | Slack | QE Slack | QE Slack | SyndesisQE Slack test |

    And navigate to the "Home" page


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

    And navigate to the "Home" page

    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"


    When select the "Timer" connection
    And select "Simple Timer" integration action
    And fill in values
      | period | 200s |
    And click on the "Next" button
    And click on the "Done" button


    Then check that position of connection to fill is "Finish"

    When select the "QE Slack" connection
    And select "Channel" integration action
    And fill in values
      | Channel | test |


    And click on the "Done" button

    When click on the "Add a Step" button

    # add data mapper step
    And select "Set Body" integration step
    And fill in values
      | Body | test message |



    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Integration_with_slack"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "Integration_with_slack" integration details
    And navigate to the "Integrations" page

    And Integration "Integration_with_slack" is present in integrations list
    # wait for integration to get in active state
    Then wait until integration "Integration_with_slack" gets into "Running" state


    Then check that last slack message equals "test message" on channel "test"


#
#  2. Check that slack message exists, use data mapper
#
  @slack-check-message-data-mapper
  Scenario: Check message

    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"


    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Done" button is "Disabled"
    Then fill in periodic query input with "SELECT company FROM CONTACT limit(1)" value
    Then fill in period input with "200" value
    Then select "Seconds" from sql dropdown
    And click on the "Done" button



    Then check that position of connection to fill is "Finish"

    When select the "QE Slack" connection
    And select "Channel" integration action
    And fill in values
      | Channel | test |


    And click on the "Done" button


    # add data mapper step
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create mapping from "company" to "message"

    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Integration_with_slack"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "Integration_with_slack" integration details
    And navigate to the "Integrations" page

    And Integration "Integration_with_slack" is present in integrations list
    # wait for integration to get in active state
    Then wait until integration "Integration_with_slack" gets into "Running" state


    Then check that last slack message equals "Red Hat" on channel "test"

