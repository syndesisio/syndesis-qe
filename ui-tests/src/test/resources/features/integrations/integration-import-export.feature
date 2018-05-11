@integration-import-export
Feature: Integration - Import Export

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis

#
#  1. integration-import both methods, for investigation one of them use integration-import-export_backup.feature
#
  @integration-import-export-classic-and-dnd
  Scenario: Import and export both flows

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
    And she separates "user.name" into "FirstName" as "1" and "LastName" as "2" using "Space [ ]" separator
    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Integration_import_export_test"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Integration_import_export_test" integration details
    And "Camilla" navigates to the "Integrations" page
    And Integration "Integration_import_export_test" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "Integration_import_export_test" gets into "Published" state

    # export the integration for import tests
    When Camilla selects the "Integration_import_export_test" integration
    Then Camilla is presented with "Integration_import_export_test" integration details
    And Camilla exports this integraion

    # now we have exported integration, we can clean state and try to import
    Given clean application state

    Given "Camilla" logs into the Syndesis
    And "Camilla" navigates to the "Integrations" page
    And Camilla clicks on the "Import" button

    Then Camilla imports integration "Integration_import_export_test"

    Then "She" navigates to the "Integrations" page

    Then she waits until integration "Integration_import_export_test" gets into "Unpublished" state

    # check draft status after import
    When Camilla selects the "Integration_import_export_test" integration
    And she is presented with "Not Published" integration status on Integration Detail page

    And she stays there for "1000" ms

    # start integration and wait for published state
    And Camilla starts integration "Integration_import_export_test"

    And "Camilla" navigates to the "Integrations" page
    Then she waits until integration "Integration_import_export_test" gets into "Published" state

#
#  2. integration-import with drag'n'drop
#

    When Camilla deletes the "Integration_import_export_test" integration
    #wait for pod to be deleted
    And she stays there for "15000" ms

    And "Camilla" navigates to the "Integrations" page
    And Camilla clicks on the "Import" button

    And Camilla drags exported integration "Integration_import_export_test" file to drag and drop area

    Then "She" navigates to the "Integrations" page

    Then she waits until integration "Integration_import_export_test" gets into "Unpublished" state

    # check draft status after import
    When Camilla selects the "Integration_import_export_test" integration
    And she is presented with "Not Published" integration status on Integration Detail page

    And she stays there for "1000" ms

    # start integration and wait for active state
    And Camilla starts integration "Integration_import_export_test"

    And "Camilla" navigates to the "Integrations" page
    Then she waits until integration "Integration_import_export_test" gets into "Published" state


#
#  2. integration-import from different syndesis instance
#
  @integration-import-from-different-instance
  Scenario: Import from different syndesis instance

    And "Camilla" navigates to the "Integrations" page
    And Camilla clicks on the "Import" button

    # import from resources TODO
    Then Camilla imports integration from relative file path "src/test/resources/integrations/Imported-integration-another-instance-export.zip"


    # check that connections need credentials update TODO
    Then "She" navigates to the "Connections" page
    And she can see alert notification

    # update connections credentials
    When clicks on the "View" kebab menu button of "Imported salesforce connection"
    Then Camilla is presented with "Imported salesforce connection" connection details
    And she can see alert notification

    Then click on the "Edit" button
    And she fills "Salesforce" connection details from connection edit page
    And click on the "Validate" button
    And she can see success notification
    And click on the "Save" button



    Then "She" navigates to the "Integrations" page

    #should be unpublished after import
    Then she waits until integration "Imported-integration-another-instance" gets into "Unpublished" state

    # check draft status after import
    When Camilla selects the "Imported-integration-another-instance" integration


    And she is presented with "Not Published" integration status on Integration Detail page

    And she stays there for "1000" ms

    # start integration and wait for published state:


    And Camilla starts integration "Imported-integration-another-instance"

    # TODO: following steps are workaround - we have to edit and publish integration so encryption stuff
    # TODO: is taken from current syndesis instance, should be deleted changed back after fix
    Then click on the "Edit Integration" button
    Then click on the "Publish" button

    And she stays there for "3000" ms

    And "Camilla" navigates to the "Integrations" page
    Then she waits until integration "Imported-integration-another-instance" gets into "Published" state