# @sustainer: mcada@redhat.com

@oauth
Feature: Connections - OAuth

  Background: Clean application state
    Given clean application state
    And clean SF contacts related to TW account: "twitter_talky"
    And clean all tweets in twitter_talky account
    And log into the Syndesis

#
#  1. test oauth connections in an integration
#
  @oauth-create-integration
  Scenario: Create integration using connections with OAuth
    When navigate to the "Settings" page
    Then check that settings item "Salesforce" has button "Register"
    When fill all oauth settings
    Then create connections using oauth
      | SAP Concur | Test-Concur-connection     |
      | Gmail      | Test-Gmail-connection      |
      | Salesforce | Test-Salesforce-connection |
      | Twitter    | Test-Twitter-connection    |

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor

    When check that position of connection to fill is "Start"
    # select twitter connection
    And select the "Test-Twitter-connection" connection
    And select "Mention" integration action
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

    When create data mapper mappings
      | user.screenName | TwitterScreenName__c |
      | text            | Description          |
      | user.name       | FirstName; LastName  |
    And scroll "top" "right"
    Then click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Twitter to Salesforce oauth"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "Twitter to Salesforce oauth" integration details

    When navigate to the "Integrations" page
    Then Integration "Twitter to Salesforce oauth" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "Twitter to Salesforce oauth" gets into "Running" state

    #there is a problem that integration is not listening instantly after publishing so delay is necessary - probably start-up time
    When sleep for "15000" ms
    Then check SF does not contain contact for tw account: "twitter_talky"

    When tweet a message from twitter_talky to "Twitter Listener" with text "OAuth syndesis test"
    # give salesforce time to create contact
    And sleep for "5000" ms
    Then validate contact for TW account: "twitter_talky" is present in SF with description: "OAuth syndesis test"
    And clean application state
    And clean SF contacts related to TW account: "twitter_talky"
    And clean all tweets in twitter_talky account
