# @sustainer: mcada@redhat.com

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
    And log into the Syndesis
    And navigate to the "Settings" page
    And fill "Twitter" oauth settings "Twitter Listener"
    And fill "Salesforce" oauth settings "QE Salesforce"
    And create connections using oauth
      | Twitter    | Twitter Listener |
      | Salesforce | QE Salesforce    |

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    # select salesforce connection
    When select the "QE Salesforce" connection
    And select "Create or update record" integration action
    And select "Contact" from "sObjectName" dropdown
    And click on the "Next" button
    And select "TwitterScreenName" from "sObjectIdName" dropdown
    And click on the "Done" button

    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | user.screenName | TwitterScreenName__c |
      | text            | Description          |
      | user.name       | FirstName; LastName  |
    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When publish integration
    And set integration name "Integration_import_export_test"
    And publish integration
    Then Integration "Integration_import_export_test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "Integration_import_export_test" gets into "Running" state

    # export the integration for import tests
    When select the "Integration_import_export_test" integration
    Then check visibility of "Integration_import_export_test" integration details
    And export the integraion

    # now we have exported integration, we can clean state and try to import
    Given clean application state

    Then Wait until there is no integration pod with name "integrationimportexporttest"

    Given log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" link

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
  Scenario: Import drag and drop

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
