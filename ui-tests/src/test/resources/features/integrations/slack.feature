# @sustainer: mcada@redhat.com

@slack
Feature: Slack Connector

  Background: Clean application state
    Given clean application state
    Given reset content of "contact" table
    Given log into the Syndesis


    Given created connections
      | Slack | QE Slack producer | QE Slack producer | SyndesisQE Slack test |
      | Slack | QE Slack consumer | QE Slack consumer | SyndesisQE Slack test |

    And navigate to the "Home" page


#
#  1. Check that slack message exists
#
  @deprecated
  @slack-check-message-body
  Scenario: Check message

    Given import extensions from syndesis-extensions folder
      | syndesis-extension-body  |

    And navigate to the "Home" page

    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"


    When select the "Timer" connection
    And select "Simple Timer" integration action
    And fill in values
      | period        | 200     |
      | select-period | Seconds |
    And click on the "Done" button


    Then check that position of connection to fill is "Finish"

    When select the "QE Slack producer" connection
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

    When select the "QE Slack producer" connection
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

#
#  3. Check that slack message is save to DB. The data mapper and basic filter are used.
#
  @slack-to-db
  Scenario: Check message in DB
    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select slack connection as start integration
    When select the "QE Slack consumer" connection
    And select "Read Messages" integration action
    Then select "test" from "channel" dropdown
    And click on the "Done" button.

    # select postgresDB connection as finish integration
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into CONTACT values (:#AUTOR , 'Dvere', :#COMPANY , 'some lead', '1999-01-01')" value
    And click on the "Done" button

    # add data mapper step
    Then check visibility of page "Add to Integration"
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create mapping from "username" to "AUTOR"
    And create mapping from "text" to "COMPANY"
    And click on the "Done" button

    # add basic filter step
    When click on the "Add a Step" button
    Then check visibility of the "Add a step" link
    And click on the "Add a step" link
    And select "Basic Filter" integration step
    And check visibility of "Basic Filter" step configuration page
    Then fill in the configuration page for "Basic Filter" step with "ANY of the following, text, contains, Red Hat testSlack" parameter
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "slack-to-db"
    And click on the "Publish" button

    # assert integration is present in list
    Then check visibility of "slack-to-db" integration details

    When navigate to the "Integrations" page
    Then Integration "slack-to-db" is present in integrations list
    And wait until integration "slack-to-db" gets into "Running" state

    When send message "Red Hat testSlack" on channel "test"
    And send message "Red Hat test incorrect Slack" on channel "test"
    And sleep for "10000" ms

    Then checks that query "select * from contact where company = 'Red Hat testSlack' AND first_name = 'syndesis-bot'" has some output
    Then checks that query "select * from contact where company = 'Red Hat test incorrect Slack'" has no output
