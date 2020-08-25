# @sustainer: mkralik@redhat.com

@ui
@extension
@extension-CRUD
Feature: Customization - Extensions CRUD

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis


  @extension-CRUD-import-new
  Scenario: Import
    When click on the "Customizations" link

    When navigate to the "Extensions" page
    Then check visibility of page "Extensions"

    When click on the "Import Extension" link
    Then check visibility of page "Import Extension"

    When upload extension with name "syndesis-extension-log-body" from syndesis-extensions dir
    Then check visibility of details about imported extension

    When click on the "Import Extension" button
    Then check visibility of page "Extensions"

    When navigate to the "Extensions" page
    Then check visibility of page "Extensions"
    And extension "Log Message Body" is present in list

  @extension-CRUD-update
  Scenario: Update
    Given import extensions from syndesis-extensions folder
      | syndesis-extension-log-body |

    When navigate to the "Extensions" page
    Then check visibility of page "Extensions"

    When select "Update" action on "Log Message Body" technical extension
    Then check visibility of page "Import Extension"

    When upload extension "syndesis-extension-log-body"
    Then check visibility of details about imported extension

#    When click on the "Update" link
    When click on the "Import Extension" button
    Then check visibility of page "Extensions"

    When navigate to the "Extensions" page
    Then check visibility of page "Extensions"
    And extension "Log Message Body" is present in list


  @extension-CRUD-delete
  Scenario: Delete

    Given import extensions from syndesis-extensions folder
      | syndesis-extension-log-body |

    When navigate to the "Extensions" page
    Then check visibility of page "Extensions"

    When select "Delete" action on "Log Message Body" technical extension
    Then check visibility of dialog page "Are you sure you want to delete the "Log Message Body" extension?"

    When click on the modal dialog "Delete" button
    And remove all "success" alerts
    And sleep for jenkins delay or 3 seconds
    Then check that technical extension "Log Message Body" is not visible