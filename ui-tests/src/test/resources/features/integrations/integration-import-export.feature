# @sustainer: mcada@redhat.com
@integration-import-export
Feature: Integration - Import Export

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis

#
#  1. integration-import both methods, for investigation one of them use integration-import-export_backup.feature
#
  @stage-smoke
  @integration-import-export-classic-and-dnd
  Scenario: Import and export both flows

    Given created connections
      | Salesforce | QE Salesforce | QE Salesforce | SyndesisQE salesforce test |

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    And check visibility of visual integration editor
    Then check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Done" button is "Disabled"
    And fill in periodic query input with "select * from contact" value
    And fill in period input with "1000" value
    And select "Seconds" from sql dropdown
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

    And create data mapper mappings
      | company    | TwitterScreenName__c |
      | last_name  | LastName             |
      | first_name | FirstName            |
      | company    | Description          |

    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    And click on the "Save as Draft" button
    And set integration name "Integration_import_export_test"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "Integration_import_export_test" integration details

    When navigate to the "Integrations" page
    Then Integration "Integration_import_export_test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "Integration_import_export_test" gets into "Running" state

    # validate salesforce contacts
    Then check that contact from SF with last name: "Jackson" has description "Red Hat"
    # clean-up in salesforce
    Then delete contact from SF with last name: "Jackson"

    # export the integration for import tests
    When select the "Integration_import_export_test" integration
    Then check visibility of "Integration_import_export_test" integration details
    And export the integraion

    # now we have exported integration, we can clean state and try to import
    Given clean application state
    And log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" button
    Then import integration "Integration_import_export_test"

    When navigate to the "Integrations" page
    Then Integration "Integration_import_export_test" is present in integrations list
    Then wait until integration "Integration_import_export_test" gets into "Stopped" state

    # check draft status after import
    When select the "Integration_import_export_test" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    And sleep for jenkins delay or "3" seconds
    # start integration and wait for published state
    And start integration "Integration_import_export_test"
    And navigate to the "Integrations" page
    Then wait until integration "Integration_import_export_test" gets into "Running" state

    # validate salesforce contacts
    Then check that contact from SF with last name: "Jackson" has description "Red Hat"
    # clean-up in salesforce
    Then delete contact from SF with last name: "Jackson"

#
#  2. integration-import with drag'n'drop
#

    When delete the "Integration_import_export_test" integration
    #wait for pod to be deleted
    And sleep for "15000" ms
    And navigate to the "Integrations" page
    And click on the "Import" button
    Then drag exported integration "Integration_import_export_test" file to drag and drop area

    When navigate to the "Integrations" page
    Then Integration "Integration_import_export_test" is present in integrations list
    And wait until integration "Integration_import_export_test" gets into "Stopped" state

    # check draft status after import
    When select the "Integration_import_export_test" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    And sleep for jenkins delay or "3" seconds
    # start integration and wait for active state
    And start integration "Integration_import_export_test"
    And navigate to the "Integrations" page
    Then wait until integration "Integration_import_export_test" gets into "Running" state

    # validate salesforce contacts
    Then check that contact from SF with last name: "Jackson" has description "Red Hat"
    # clean-up in salesforce
    Then delete contact from SF with last name: "Jackson"

#
#  2. integration-import from different syndesis instance
#
  @integration-import-from-different-instance
  Scenario: Import from different syndesis instance

    When navigate to the "Integrations" page
    And click on the "Import" button
    # import from resources TODO
    Then import integration from relative file path "src/test/resources/integrations/Imported-integration-another-instance-export.zip"

    # check that connections need credentials update
    When navigate to the "Connections" page
    And check visibility of alert notification
    # update connections credentials
    And click on the "View" kebab menu button of "QE Salesforce"
    And check visibility of "QE Salesforce" connection details
    Then check visibility of alert notification

    When click on the "Edit" button
    And fill in "QE Salesforce" connection details from connection edit page
    And click on the "Validate" button
    Then check visibility of success notification
    When click on the "Save" button
    And navigate to the "Integrations" page

    #should be unpublished after import
    When select the "Integration_import_export_test" integration
    Then check visibility of "Stopped" integration status on Integration Detail page

    When sleep for jenkins delay or "3" seconds
    # start integration and wait for published state:
    And start integration "Integration_import_export_test"

    When sleep for jenkins delay or "3" seconds
    And navigate to the "Integrations" page
    Then wait until integration "Integration_import_export_test" gets into "Running" state

    # validate salesforce contacts
    Then check that contact from SF with last name: "Jackson" has description "Red Hat"
    # clean-up in salesforce
    Then delete contact from SF with last name: "Jackson"