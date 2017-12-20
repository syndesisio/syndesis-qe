@tech-extension-integration-test
Feature: Upload tech extension and add it to integration

  @tech-extension-clean-application-state
  Scenario: Clean application state
    Given "Camilla" logs into the Syndesis
   	Given clean application state

  @tech-extension-create-twitter-connection
  Scenario: Create Twitter connection
    # create twitter connection
    When "Camilla" navigates to the "Connections" page
    And click on the "Create Connection" button
    And Camilla selects the "Twitter" connection
    Then she is presented with the "Validate" button

    # fill twitter connection details
    When she fills "Twitter Listener" connection details
    And scroll "top" "right"
    And click on the "Next" button
    And type "Twitter Listener" into connection name
    And type "SyndesisQE Twitter listener account" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"

  @tech-extension-navigate-to-technical-extensions-page
  Scenario: Navigate to technical extensions page
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"
    
  @tech-extension-import-new-tech-extension
  Scenario: Import new technical extensions
    When Camilla clicks on the "Import Extension" button
    Then she is presented with the Syndesis page "Import Extension"

    When Camilla upload extension
    Then she see details about imported extension
    
    When she clicks on the "Import" button
    Then Camilla is presented with the Syndesis page "Extension Details"
    
    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then Camilla is presented with the Syndesis page "Extensions"
    And technical extension "Syndesis Extension" is present in technical extensions list

  @tech-extension-create-integration-with-new-tech-extension
  Scenario: Create integration from twitter to salesforce
    When "Camilla" logs into the Syndesis
    And "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # select postgresDB connection as 'from' point
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    Then she fills periodic query input with "SELECT * FROM CONTACT" value
    Then she fills period input with "5000" value
    And clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select twitter connection
    When Camilla selects the "Twitter Listener" connection
    And she selects "Mention" integration action
    Then she is presented with the "Add a Step" button

    # add tech extension step
    When Camilla clicks on the "Add a Step" button
    Then she selects "simple-log" integration step
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
    
  @tech-extension-delete-tech-extension
  Scenario: Delete technical extensions
    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"

    When Camilla choose "Delete" action on "Syndesis Extension" technical extension
    Then she is presented with dialog page "Warning!"
    And she can see notification about integrations "Twitter to Salesforce E2E" in which is tech extension used

    Then she clicks on the modal dialog "Cancel" button