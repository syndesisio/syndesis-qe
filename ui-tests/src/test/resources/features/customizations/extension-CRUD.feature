@extension-CRUD-test
Feature: Customization - Extensions CRUD

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis


  @extension-CRUD-import-new
  Scenario: Import
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"

    When Camilla clicks on the "Import Extension" button
    Then she is presented with the Syndesis page "Import Extension"

    When She uploads extension with name "syndesis-extension-log-body" from syndesis-extensions dir
    Then She sees details about imported extension

    When she clicks on the "Import Extension" button
    Then Camilla is presented with the Syndesis page "Extension Details"

    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then Camilla is presented with the Syndesis page "Extensions"
    And extension "Log Message Body" is present in list

  @extension-CRUD-update
  Scenario: Update
    Given import extensions from syndesis-extensions folder
      | syndesis-extension-log-body |

    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"

    When Camilla choose "Update" action on "Log Message Body" technical extension
    Then she is presented with the Syndesis page "Import Extension"

    When Camilla upload extension "syndesis-extension-log-body"
    Then she see details about imported extension

    When she clicks on the "Update" button
    Then Camilla is presented with the Syndesis page "Extension Details"

    When "Camilla" navigates to the "Customizations" page
    And clicks on the "Extensions" link
    Then Camilla is presented with the Syndesis page "Extensions"
    And extension "Log Message Body" is present in list

  @extension-CRUD-delete
  Scenario: Delete

    Given import extensions from syndesis-extensions folder
      | syndesis-extension-log-body |

    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "Extensions" link
    Then she is presented with the Syndesis page "Extensions"

    When Camilla choose "Delete" action on "Log Message Body" technical extension
    Then she is presented with dialog page "Warning!"

    When she clicks on the modal dialog "Delete" button
    Then Camilla can not see "Log Message Body" technical extension anymore