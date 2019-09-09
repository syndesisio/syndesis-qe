# @sustainer: mcada@redhat.com

@ui
@slack
@datamapper
@database
@integrations-slack
Feature: Slack Connector

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis
    And created connections
      | Slack | QE Slack | QE Slack | SyndesisQE Slack test |
    And navigate to the "Home" page
    
#
#  2. Check that slack message exists, use data mapper
#
  @slack-check-message-data-mapper
  Scenario: Check that slack received a message from an integration

    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection

    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"

    When fill in periodic query input with "SELECT company FROM CONTACT limit(1)" value
    And fill in period input with "200" value
    And select "Seconds" from sql dropdown
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    When select the "QE Slack" connection
    And select "Channel" integration action
    And fill in values by element data-testid
      | channel | test |
    And click on the "Done" button
    And add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When open data mapper collection mappings
    And create data mapper mappings
      | company | message |
    And click on the "Done" button
    And click on the "Save" link
    And set integration name "Integration_with_slack"
    And publish integration
    Then Integration "Integration_with_slack" is present in integrations list
    And wait until integration "Integration_with_slack" gets into "Running" state
    And sleep for jenkins delay or "15" seconds
    And check that last slack message equals "Red Hat" on channel "test"

#
#  3. Check that slack message is saved to DB. The data mapper and basic filter are used.
#
  @slack-to-db
  Scenario: Check that slack message is saved into DB

    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "QE Slack" connection
    And select "Read Messages" integration action
    And fill in values by element data-testid
      | channel  | test |
    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into CONTACT values (:#AUTOR , 'Dvere', :#COMPANY , 'some lead', '1999-01-01')" value
    And click on the "Done" button
    # Then check visibility of page "Add to Integration"

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | username | AUTOR   |
      | text     | COMPANY |
    And click on the "Done" button
    And add integration step on position "0"
    And select "Basic Filter" integration step
    Then check visibility of "Basic Filter" step configuration page

    When fill in the configuration page for "Basic Filter" step with "ANY of the following, text, contains, Red Hat testSlack" parameter
    And click on the "Done" button
    And click on the "Save" link
    And set integration name "slack-to-db"
    And publish integration
    Then Integration "slack-to-db" is present in integrations list
    And wait until integration "slack-to-db" gets into "Running" state

    When send message "Red Hat testSlack" on channel "test"
    And send message "Red Hat test incorrect Slack" on channel "test"
    Then checks that query "select * from contact where company = 'Red Hat testSlack' AND first_name = 'syndesis-bot'" has some output
    And checks that query "select * from contact where company = 'Red Hat test incorrect Slack'" has no output

#
#  4. Check Maximum Messages to Retrieve and Delay function in SLACK consumer ( GH issue: #3761 )
#
  @slack-to-db-delay-and-maxmessage
  Scenario: Check Maximum Messages to Retrieve and Delay function in SLACK consumer
    # create integration
    Given click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select slack connection as start integration
    When select the "QE Slack" connection
    Then select "Read Messages" integration action
    And select "random" from "channel" dropdown
    And fill in values by element ID
      | maxresults | 2     |
      | delay      | 60000 |
    And click on the "Done" button.

    # select postgresDB connection as finish integration
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into CONTACT values (:#AUTOR , 'Dvere', :#COMPANY , 'some lead', '1999-01-01')" value
    And click on the "Done" button

    # add data mapper step
    # Then check visibility of page "Add to Integration"
    When add integration step on position "0"
    Then select "Data Mapper" integration step
    And check visibility of data mapper ui
    And create data mapper mappings
      | username | AUTOR   |
      | text     | COMPANY |
    And click on the "Done" button

    # finish and save integration
    When click on the "Save" link
    And set integration name "slack-to-db-delay-and-maxmessage"
    And send message "message1" on channel "random"
    And send message "message2" on channel "random"
    And send message "message3" on channel "random"
    And send message "message4" on channel "random"
    And publish integration

    Then Integration "slack-to-db-delay-and-maxmessage" is present in integrations list
    And wait until integration "slack-to-db-delay-and-maxmessage" gets into "Running" state
    And sleep for jenkins delay or "15" seconds

    # test Maximum Messages to Retrieve after start
    Then checks that query "select * from contact where company = 'message1'" has no output
    And checks that query "select * from contact where company = 'message2'" has no output
    And checks that query "select * from contact where company = 'message3' AND first_name = 'syndesis-bot'" has "1" output
    And checks that query "select * from contact where company = 'message4' AND first_name = 'syndesis-bot'" has "1" output

    # test delay
    Then send message "messageDelayed" on channel "random"
    And checks that query "select * from contact where company = 'messageDelayed'" has no output
    And sleep for "60000" ms
    Then checks that query "select * from contact where company = 'messageDelayed' AND first_name = 'syndesis-bot'" has "1" output

    # test Maximum Messages to Retrive after delay
    Then send message "message5" on channel "random"
    And send message "message6" on channel "random"
    And send message "message7" on channel "random"
    And send message "message8" on channel "random"
    And sleep for "60000" ms
    #After first delay it should consume only two messages (Max)
    Then checks that query "select * from contact where company = 'message5' AND first_name = 'syndesis-bot'" has "1" output
    And checks that query "select * from contact where company = 'message6' AND first_name = 'syndesis-bot'" has "1" output
    And checks that query "select * from contact where company = 'message7'" has no output
    And checks that query "select * from contact where company = 'message8'" has no output
    And sleep for "60000" ms
    #After next delay it should consume next two messages
    Then checks that query "select * from contact where company = 'message7' AND first_name = 'syndesis-bot'" has "1" output
    And checks that query "select * from contact where company = 'message8' AND first_name = 'syndesis-bot'" has "1" output
