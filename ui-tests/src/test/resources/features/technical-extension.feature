@tech-extension
Feature: Upload tech extension and add it to integration

  @tech-extension-clean-application-state
  Scenario: Clean application state
    Given "Camilla" logs into the Syndesis
    Given clean application state

  @tech-extension-navigate-to-technical-extensions-page
  Scenario: Navigate to technical extensions page
    When "Camilla" navigates to the "Customizations" page
    And click on the "Technical Extensions" tab #new step
    Then she is presented with the Syndesis page "Technical extensions"
    
  @tech-extension-import-new-tech-extension
  Scenario: Import new technical extensions
    When Camilla clicks on the "Import technical extension" button
    Then she is presented with the Syndesis page "Import Technical Extensions"
    
    When Camilla clicks on the "Choose File" button
    Then she is presented with dialog page "Select a file to upload" #new step
    
    When Camilla choose a file "Tech extension"
    And clicks on the "Open" button
    Then she is presented with file "Tech extension" being apploaded #new step
    And she can review uploaded technical extension #new step
    
    When she clicks on the "Import" button
    Then Camilla is presented with the Syndesis page "Technical extensions"
    And technical extension "Tech extension" is present in technical extensions list #new step
    
  @tech-extension-update-tech-extension
  Scenario: Update technical extensions
    When Camilla click on the "Update" button of "Techn extension" technical extension
    Then she is presented with the Syndesis page "Update Technical Extensions"

    When Camilla clicks on the "Choose File" button
    Then she is presented with dialog page "Select a file to upload" #new step
    
    When Camilla choose a file "Tech extension"
    And clicks on the "Open" button
    Then she is presented with file "Tech extension" being apploaded #new step
    And she can review uploaded technical extension #new step

    When she clicks on the "Import" button
    Then Camilla is presented with the Syndesis page "Technical extensions"
    And technical extension "Tech extension" is present in technical extensions list #new step
  	
  @tech-extension-create-integration-with-new-tech-extension
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

    # add tech extension step
    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the "Add a step" link

    When she selects "Custom steps" from "steps" dropdown
    Then she can see "Custom step" in step list #new step
    
    Then she selects "Custom step" integration step
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
    And click on the "Technical Extensions" tab #new step
    Then she is presented with the Syndesis page "Technical extensions"

    When Camilla click on the "Delete" button of "Tech extension" technical extension
    Then she is presented with dialog page "Warning!" #new step
    And she can see integrations "Twitter to Salesforce E2E" in which is tech extension used #new step

    When she clicks on the "Delete" button
    Then Camilla can not see "Tech extension" technical extension anymore #new step