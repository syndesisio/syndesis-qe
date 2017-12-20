@tech-extension-CRUD-test
Feature: Upload tech extension and add it to integration

  @tech-extension-CRUD-clean-application-state
  Scenario: Clean application state
    Given "Camilla" logs into the Syndesis
    Given clean application state

  @tech-extension-CRUD-navigate-to-technical-extensions-page
  Scenario: Navigate to technical extensions page
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"
    
  @tech-extension-CRUD-import-new-tech-extension
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
    
  @tech-extension-CRUD-update-tech-extension
  Scenario: Update technical extensions
    When Camilla choose "Update" action on "Syndesis Extension" technical extension
    Then she is presented with the Syndesis page "Import Extension"

    When Camilla upload extension
    Then she see details about imported extension

    When she clicks on the "Import" button
    Then Camilla is presented with the Syndesis page "Extension Details"

    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then Camilla is presented with the Syndesis page "Extensions"
    And technical extension "Syndesis Extension" is present in technical extensions list
    
  @tech-extension-CRUD-delete-tech-extension
  Scenario: Delete technical extensions
    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"

    When Camilla choose "Delete" action on "Syndesis Extension" technical extension
    Then she is presented with dialog page "Warning!"

    When she clicks on the modal dialog "Delete" button
    Then Camilla can not see "Syndesis Extension" technical extension anymore