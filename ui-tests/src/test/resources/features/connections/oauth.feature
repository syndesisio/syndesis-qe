@oauth
Feature: Connections - OAuth

  Background: Clean application state
    Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
    And clean all tweets in twitter_talky account


    Given log into the Syndesis

#
#  1. test oauth connections in an integration
#
  @oauth-create-integration
  Scenario: Create integration using connections with OAuth
    And navigate to the "Settings" page

    Then settings item "Salesforce" has button "Register"
    Then fill all oauth settings

    Then create connections using oauth
      | Salesforce | Test-Salesforce-connection |
      | Twitter    | Test-Twitter-connection    |


    # vyrobit integraci, spustit, checknout ze se stalo co se melo stat

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    # select twitter connection
    When select the "Test-Twitter-connection" connection
    And select "Mention" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"
    # select salesforce connection
    When select the "Test-Salesforce-connection" connection
    And select "Create or update record" integration action
    And select "Contact" from "sObjectName" dropdown
    And click on the "Next" button
    And select "TwitterScreenName" from "sObjectIdName" dropdown
    And click on the "Done" button
    Then check visibility of the "Add a Step" button

    # add data mapper step
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create mapping from "user.screenName" to "TwitterScreenName__c"
    When create mapping from "text" to "Description"
    When create mapping from "user.name" to "FirstName"
    And separate "user.name" into "FirstName" as "1" and "LastName" as "2" using "Space [ ]" separator
    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Twitter to Salesforce oauth"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "Twitter to Salesforce oauth" integration details
    And navigate to the "Integrations" page
    And Integration "Twitter to Salesforce oauth" is present in integrations list
    # wait for integration to get in active state
    Then wait until integration "Twitter to Salesforce oauth" gets into "Published" state
    #And verify s2i build of integration "Twitter to Salesforce E2E" was finished in duration 1 min

    #there was a problem that integration was not listening instantly after publishing so delay is necessary
    And sleep for "15000" ms
    Then check SF does not contain contact for tw accound: "twitter_talky"
    Then tweet a message from twitter_talky to "Twitter Listener" with text "OAuth syndesis test"
    # give salesforce time to create contact
    And sleep for "5000" ms
    Then validate contact for TW account: "twitter_talky" is present in SF with description: "OAuth syndesis test"
    Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
    And clean all tweets in twitter_talky account

