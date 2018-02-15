@support-page
Feature: Test functionality of integration import export feature

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis
    And she navigates to the "Support" page in help menu

#
#  1. version check
#
  @support-page-version-check
  Scenario: Check version of syndesis on Support page

    And she checks version string

#
#  2. download all diagnostic info
#
  @support-page-download-diagnostic
  Scenario: Create integration and test export feature of single integration

    And she downloads diagnostics for all integrations

#
#  3. download specific diagnostic info
#
  @support-page-download-specific-diagnostic
  Scenario: Create integration and test export feature of single integration
    Given she removes file "syndesis.zip" if it exists

    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce    | QE Salesforce    | SyndesisQE salesforce test          |

    # create integration
    And "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # select twitter connection
    When Camilla selects the "Twitter Listener" connection
    And she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select salesforce connection
    When Camilla selects the "QE Salesforce" connection
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
    And she separates "user.name" into "FirstName" as "1" and "LastName" as "2" using "Space" separator
    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "my-integration"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "my-integration" integration details
    #And Camilla clicks on the "Done" button
    #And Integration "my-integration" is present in integrations list
    # wait for integration to get in active state
    And "Camilla" navigates to the "Integrations" page

    Then she waits until integration "my-integration" gets into "Published" state

    #TODO: create integration - use rest steps so it is easier?
    When she navigates to the "Support" page in help menu

    And she downloads diagnostics for "my-integration" integration

