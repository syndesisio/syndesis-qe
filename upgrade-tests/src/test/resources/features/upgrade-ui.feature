# @sustainer: mkralik@redhat.com

@syndesis-upgrade-ui
Feature: Upgrade Syndesis

  Background: Clean application state
    Given prepare upgrade
    And clean default namespace
    And deploy previous Syndesis CR "syndesis-cr-previous.yaml"
    And wait for Syndesis to become ready
    And verify syndesis "previous" version
    And insert into "todo" table
      | task1 |
    And log into the Syndesis

  # Issue in 7.7
  @ENTESB-13988
  Scenario: Upgrade Syndesis - check UI
    When click on the "Create Integration" link to create a new integration.
    Then check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And click on the "Next" button

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo limit 1" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | task | task |
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "webhook-before-upgrade"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "webhook-before-upgrade" gets into "Running" state

    When invoke post request to webhook in integration webhook-before-upgrade with token test-webhook and body {}
    And wait until integration webhook-before-upgrade processed at least 1 message
    Then check that query "select * from todo where task='task1'" has 2 rows output

    # close all connections used by DB steps. The new connection will be created by that step later
    When close DB connections
    And perform syndesis upgrade to newer version using operator
    Then wait until upgrade is done
    And sleep for jenkins delay or 180 seconds
    And wait for Syndesis to become ready
    And verify syndesis "upgraded" version
    And check that pull secret is linked in the service accounts

    When log into the Syndesis
    And navigate to the "Home" page

    # redeploy old integration TODO check steps
    And navigate to the "Integrations" page
    And select the "webhook-before-upgrade" integration
    And click on the "Edit Integration" link

    And add integration step on position "0"
    And select "Log" integration step
    And fill in values by element data-testid
      | customtext | after update log |
    And click on the "Done" button
    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "webhook-before-upgrade" gets into "Running" state

    And invoke post request to webhook in integration webhook-before-upgrade with token test-webhook and body {}
    And wait until integration webhook-before-upgrade processed at least 2 messages
    Then check that query "select * from todo where task='task1'" has 3 rows output
    And validate that logs of integration "webhook-before-upgrade" contains string "after update log"

    When select the "webhook-before-upgrade" integration
    And click on the "Activity" tab
    And check that 1. activity version contains Version 2

    # create new integration
    And navigate to the "Home" page
    And navigate click on the "Create Integration" link to create a new integration.
    Then check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test |
    And click on the "Next" button
    And click on the "Next" button

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo limit 1" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | task | task |
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "webhook-after-upgrade"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "webhook-after-upgrade" gets into "Running" state
    And invoke post request to webhook in integration webhook-after-upgrade with token test and body {}
    And wait until integration webhook-after-upgrade processed at least 1 message
    Then check that query "select * from todo where task='task1'" has 4 rows output
