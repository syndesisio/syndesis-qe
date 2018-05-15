@oauth
Feature: Connections - OAuth

  Background: Clean application state
    Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
    And clean all tweets in twitter_talky account


    Given "Camilla" logs into the Syndesis

#
#  1. test oauth connections in an integration
#
  @oauth-create-integration
  Scenario: Create integration using connections with OAuth
    And "Camilla" navigates to the "Settings" page

    Then settings item "Salesforce" has button "Register"
    Then fill all oauth settings

    Then create connections using oauth
      | Salesforce | Test-Salesforce-connection |
      | Twitter    | Test-Twitter-connection    |


    # vyrobit integraci, spustit, checknout ze se stalo co se melo stat

    # create integration
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections
    # select twitter connection
    When Camilla selects the "Test-Twitter-connection" connection
    And she selects "Mention" integration action
    And Camilla clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections
    # select salesforce connection
    When Camilla selects the "Test-Salesforce-connection" connection
    And she selects "Create or update record" integration action
    And she selects "Contact" from "sObjectName" dropdown
    And Camilla clicks on the "Next" button
    And she selects "TwitterScreenName" from "sObjectIdName" dropdown
    And Camilla clicks on the "Done" button
    Then she is presented with the "Add a Step" button

    # add data mapper step
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    When she creates mapping from "user.screenName" to "TwitterScreenName__c"
    When she creates mapping from "text" to "Description"
    When she creates mapping from "user.name" to "FirstName"
    And she separates "user.name" into "FirstName" as "1" and "LastName" as "2" using "Space [ ]" separator
    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Twitter to Salesforce oauth"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Twitter to Salesforce oauth" integration details
    And "Camilla" navigates to the "Integrations" page
    And Integration "Twitter to Salesforce oauth" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "Twitter to Salesforce oauth" gets into "Published" state
    #And verify s2i build of integration "Twitter to Salesforce E2E" was finished in duration 1 min

    #there was a problem that integration was not listening instantly after publishing so delay is necessary
    And she stays there for "15000" ms
    Then check SF does not contain contact for tw accound: "twitter_talky"
    Then tweet a message from twitter_talky to "Twitter Listener" with text "OAuth syndesis test"
    # give salesforce time to create contact
    And she stays there for "5000" ms
    Then validate contact for TW account: "twitter_talky" is present in SF with description: "OAuth syndesis test"
    Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
    And clean all tweets in twitter_talky account

