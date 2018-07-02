##################################################
# This is disabled as second import always failed due to openshift resource not being properly deleted on time
# and investigation wasn't successful so far - TODO
# instead it is all in one scenario but if investigating bug it may be useful to run just one of them
# so I did not delete these. Both scenarios are fully functional if run separately via their tag.
##################################################

@wip
@integration-import-export-backup
Feature: Integration - Import Export

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce    | QE Salesforce    | SyndesisQE salesforce test          |

    # create integration
    And navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action
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

    Then create data mapper mappings
      | user.screenName | TwitterScreenName__c |
      | text            | Description          |
      | user.name       | FirstName; LastName  |

    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Integration_import_export_test"
    And click on the "Publish" button
    # assert integration is present in list
    Then check visibility of "Integration_import_export_test" integration details
    And navigate to the "Integrations" page
    And Integration "Integration_import_export_test" is present in integrations list
    # wait for integration to get in active state
    Then wait until integration "Integration_import_export_test" gets into "Running" state

    # export the integration for import tests
    When select the "Integration_import_export_test" integration
    Then check visibility of "Integration_import_export_test" integration details
    And export the integraion

    # now we have exported integration, we can clean state and try to import
    Given clean application state

    Then Wait until there is no integration pod with name "integrationimportexporttest"

    Given log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" button

#
#  1. integration-import classic method
#
  @integration-import-export-classic
  Scenario: Import classic flow

    Then import integration "Integration_import_export_test"

    Then navigate to the "Integrations" page

    # check draft status after import
    When select the "Integration_import_export_test" integration
    And check visibility of "Stopped" integration status on Integration Detail page

    And sleep for "20000" ms

    # start integration and wait for published state
    And start integration "Integration_import_export_test"

    And navigate to the "Integrations" page
    Then wait until integration "Integration_import_export_test" gets into "Running" state

#
#  2. integration-import with drag'n'drop
#
  @integration-import-export-with-drag-and-drop
  Scenario: Import drag&drop

    And drag exported integration "Integration_import_export_test" file to drag and drop area

    Then navigate to the "Integrations" page

    # check draft status after import
    When select the "Integration_import_export_test" integration
    And check visibility of "Stopped" integration status on Integration Detail page

    And sleep for "20000" ms

    # start integration and wait for active state
    And start integration "Integration_import_export_test"

    And navigate to the "Integrations" page
    Then wait until integration "Integration_import_export_test" gets into "Running" state
