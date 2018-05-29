@support-page
Feature: Support page

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    And navigate to the "Support" page in help menu

#
#  1. version check
#
  @support-page-version-check
  Scenario: Version

    And check version string

#
#  2. download all diagnostic info
#
  @support-page-download-diagnostic
  Scenario: Export diagnostic of all

    And download diagnostics for all integrations

#
#  3. download specific diagnostic info
#
  @support-page-download-specific-diagnostic
  Scenario: Export diagnostic of single integration
    Given remove file "syndesis.zip" if it exists

    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce    | QE Salesforce    | SyndesisQE salesforce test          |

    # create integration
    And navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # select salesforce connection
    When select the "QE Salesforce" connection
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
    And set integration name "my-integration"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "my-integration" integration details
    #And click on the "Done" button
    #And Integration "my-integration" is present in integrations list
    # wait for integration to get in active state
    And navigate to the "Integrations" page

    Then wait until integration "my-integration" gets into "Published" state

    #TODO: create integration - use rest steps so it is easier?
    When navigate to the "Support" page in help menu

    And download diagnostics for "my-integration" integration

