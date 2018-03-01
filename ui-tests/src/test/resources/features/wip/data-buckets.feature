@data-buckets
Feature: Test functionality of data buckets during creation of integration

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis

    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce    | QE Salesforce    | SyndesisQE salesforce test          |
    And "Camilla" navigates to the "Home" page

#
#  1. hover with mouse - error check
#
  @data-buckets-check-popups
  Scenario: Check that there is error without data mapper step
    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # select twitter connection
    When Camilla selects the "Twitter Listener" connection
    And she selects "Mention" integration action
    And clicks on the "Done" button

    Then she is prompted to select a "Finish" connection from a list of available connections

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
    And clicks on the "Done" button

    # check pop ups
    Then she checks that text "Output: Twitter Mention" is "visible" in hover table over "start" step
    Then she checks that text "Add a datamapper step" is "visible" in hover table over "finish" step

    # add data mapper step
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    When she creates mapping from "user.screenName" to "TASK"
    And scroll "top" "right"
    And click on the "Done" button

    # check pop ups
    Then she checks that text "Output: Twitter Mention" is "visible" in hover table over "start" step
    Then she checks that text "Add a datamapper step" is "not visible" in hover table over "finish" step
    Then she checks that text "Input: SQL Parameter" is "visible" in hover table over "finish" step

    Then she checks that text "Input: All preceding outputs" is "visible" in hover table over "middle" step
    Then she checks that text "Output: Data Mapper" is "visible" in hover table over "middle" step


#
#  2. check that data buckets are available
#
  @data-buckets-usage
  Scenario: Check that data buckets can be used
    # clean salesforce before tests
    Given clean SF contacts related to TW account: "Twitter Listener"

    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # first database connection - get some information to be used by second datamapper
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "select * from contact where create_date='2018-02-21'" value
    Then she fills period input with "10000" value
    #@wip time_unit_id to be specified after new update is available:
    #Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button


    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "QE Salesforce" connection
    And she selects "Create or update record" integration action
    And she selects "Contact" from "sObjectName" dropdown
    And Camilla clicks on the "Next" button
    And she selects "TwitterScreenName" from "sObjectIdName" dropdown
    And Camilla clicks on the "Done" button



    # add another connection
    When Camilla clicks on the "Add a Connection" button
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    # wip this query doesnt work ftb #698
    Then she fills invoke query input with "select company as firma from contact limit 1;" value
    # there is no done button:
    And clicks on the "Done" button


    Then she is presented with the "Add a Step" button

    Then she checks that text "Add a datamapper step" is "not visible" in hover table over "middle" step
    Then she checks that text "Add a datamapper step" is "visible" in hover table over "finish" step

    Then she adds second step between STEP and FINISH connection
    # add data mapper step
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then check that data bucket "1 - SQL Result" is available and opens it
    Then check that data bucket "2 - SQL Result" is available and opens it

    When she creates mapping from "company" to "TwitterScreenName__c"
    When she creates mapping from "last_name" to "LastName"
    When she creates mapping from "first_name" to "FirstName"

    When she creates mapping from "firma" to "Description"

    And scroll "top" "right"
    And click on the "Done" button

    Then she checks that text "Add a datamapper step" is "not visible" in hover table over "finish" step

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Integration_with_buckets"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Integration_with_buckets" integration details
    And "Camilla" navigates to the "Integrations" page

    And Integration "Integration_with_buckets" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "Integration_with_buckets" gets into "Published" state


    # validate salesforce contacts
    Then check that contact from SF with last name: "Jackson" has description "Red Hat"
    # clean-up in salesforce
    Then delete contact from SF with last name: "Jackson"



