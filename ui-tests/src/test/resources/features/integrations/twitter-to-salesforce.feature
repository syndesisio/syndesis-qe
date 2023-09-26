# @sustainer: mkralik@redhat.com

@ignore
@ui
@doc-tutorial
@oauth
@twitter
@salesforce
@datamapper
@integrations-twitter-to-salesforce
Feature: Integration - Twitter to Salesforce

  Background: Clean application state
    Given clean application state
    And delete contact from SF with email: "integrations@salesforce.com"
    And clean all tweets in twitter_talky account
    And log into the Syndesis
    And navigate to the "Settings" page
    And fills all oauth settings
    And create connections using oauth
      | Twitter    | Twitter Listener |
      | Salesforce | QE Salesforce    |

  Scenario: Twitter to salesforce integration
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    # select salesforce connection
    When select the "QE Salesforce" connection
    And select "Create or update record" integration action
    And select "Contact" from "sobjectname" dropdown
    And click on the "Next" button
    And select "TwitterScreenName" from "sobjectidname" dropdown
    And click on the "Done" button

    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And define constant "email" with value "integrations@salesforce.com" of type "String" in data mapper
    When create data mapper mappings
      | user.screenName | TwitterScreenName__c |
      | text            | Description          |
      | user.name       | FirstName; LastName  |
      | email           | Email                |

    And scroll "top" "right"
    And click on the "Done" button

    # add basic filter step
    And add integration step on position "0"
    And select "Basic Filter" integration step
    Then check visibility of "Basic Filter" step configuration page
    And check that basic filter step path input options contains "text" option
    When fill in the configuration page for "Basic Filter" step with "ANY of the following, text, contains, #syndesis4ever" parameter
    And click on the "Done" button

     # add advanced filter step
    And add integration step on position "1"
    And select "Advanced Filter" integration step
    Then check visibility of "Advanced Filter" step configuration page
    When fill in the configuration page for "Advanced Filter" step with "${body.text} contains '#e2e'" parameter
    And click on the "Done" button

    # finish and save integration
    And click on the "Save" link
    And set integration name "Twitter to Salesforce E2E"
    And publish integration
    Then Integration "Twitter to Salesforce E2E" is present in integrations list
    And wait until integration "Twitter to Salesforce E2E" gets into "Running" state

    When tweet a message from twitter_talky to "Twitter Listener" with text "test #syndesis4ever"
    And wait until integration Twitter to Salesforce E2E processed at least 1 message
    Then check SF "does not contain" contact with a email: "integrations@salesforce.com"

    When tweet a message from twitter_talky to "Twitter Listener" with text "test #e2e"
    And wait until integration Twitter to Salesforce E2E processed at least 2 messages
    Then check SF "does not contain" contact with a email: "integrations@salesforce.com"

    When tweet a message from twitter_talky to "Twitter Listener" with text "test #e2e #syndesis4ever"
    And wait until integration Twitter to Salesforce E2E processed at least 3 messages
    Then check SF "contains" contact with a email: "integrations@salesforce.com"
    And check that contact from SF with email: "integrations@salesforce.com" has description "test #e2e #syndesis4ever"

    # clean-up in salesforce
    When delete contact from SF with email: "integrations@salesforce.com"
