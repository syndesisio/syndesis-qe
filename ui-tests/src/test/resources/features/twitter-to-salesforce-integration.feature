@tp2
@integrations-twitter-to-salesforce
Feature: Create integration with datamapper step

  @integrations-twitter-to-salesforce-clean-application-state
  Scenario: Clean application state
#    Given user cleans default namespace
#    When user deploys Syndesis from template
#    Then user waits for Syndesis to become ready
    Given "Camilla" logs into the Syndesis
    Given clean application state

  @integrations-twitter-to-salesforce-create-twitter-connection
  Scenario: Create Twitter connection
    # create twitter connection
    When "Camilla" navigates to the "Connections" page
    And click on the "Create Connection" button
    And Camilla selects the "Twitter" connection
    Then she is presented with the "Validate" button
    # fill twitter connection details
    When she fills "Twitter Listen" connection details
    And scroll "top" "right"
    And click on the "Next" button
    And type "Twitter Listener" into connection name
    And type "SyndesisQE Twitter listener account" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"

  @integrations-twitter-to-salesforce-create-salesforce-connection
  Scenario: Create Salesforce connection
    # create salesforce connection
    When "Camilla" navigates to the "Connections" page
    And click on the "Create Connection" button
    And Camilla selects the "Salesforce" connection
    Then she is presented with the "Validate" button
    # fill salesforce connection details
    When she fills "QE Salesforce" connection details
    And scroll "top" "right"
    And click on the "Next" button
    And type "QE Salesforce" into connection name
    And type "SyndesisQE salesforce test" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"

  @integrations-twitter-to-salesforce-create-integration
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
    And she separates "user.name" into "FirstName" as "1" and "LastName" as "2" using "Comma" separator
    And scroll "top" "right"
    And click on the "Done" button

    # add basic filter step
    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the "Add a step" link
    And she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "ANY of the following, text, contains, #syndesis4ever" parameter
    And click on the "Next" button

     # add advanced filter step
    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the "Add a step" link
    And she selects "Advanced Filter" integration step
    And she is presented with a "Advanced Filter" step configure page
    Then she fill configure page for "Advanced Filter" step with "${body.text} contains '#e2e'" parameter
    And click on the "Next" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she defines integration name "Twitter to Salesforce E2E"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Twitter to Salesforce E2E" integration details
    And Camilla clicks on the "Done" button
    And Integration "Twitter to Salesforce E2E" is present in integrations list
    # wait for integration to get in active state
    Then she wait until integration "Twitter to Salesforce E2E" get into "Active" state
    And verify s2i build of integration "Twitter to Salesforce E2E" was finished in duration 1 min
