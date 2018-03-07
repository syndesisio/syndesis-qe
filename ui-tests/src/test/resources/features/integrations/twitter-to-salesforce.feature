@integrations-twitter-to-salesforce
Feature: Create integration with datamapper step

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis
    Given created connections
      | Twitter | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce | QE Salesforce | SyndesisQE salesforce test |

  Scenario: Create integration from twitter to salesforce
    # create integration
    When "Camilla" logs into the Syndesis
    And "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections
    # select twitter connection
    When Camilla selects the "Twitter Listener" connection
    And she selects "Mention" integration action
    And Camilla clicks on the "Done" button
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

    # add basic filter step
    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the "Add a step" link
    And she selects "Basic Filter" integration step
    And she is presented with "Basic Filter" step configuration page
    And she checks that basic filter step path input options contains "text" option
    Then she fills the configuration page for "Basic Filter" step with "ANY of the following, text, contains, #syndesis4ever" parameter
    And click on the "Next" button

     # add advanced filter step
    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the "Add a step" link
    And she selects "Advanced Filter" integration step
    And she is presented with "Advanced Filter" step configuration page
    Then she fills the configuration page for "Advanced Filter" step with "${body.text} contains '#e2e'" parameter
    And click on the "Next" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Twitter to Salesforce E2E"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Twitter to Salesforce E2E" integration details
    And "Camilla" navigates to the "Integrations" page
    And Integration "Twitter to Salesforce E2E" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "Twitter to Salesforce E2E" gets into "Published" state
    And verify s2i build of integration "Twitter to Salesforce E2E" was finished in duration 1 min
