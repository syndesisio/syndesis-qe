# @sustainer: mkralik@redhat.com

@ui
@slack
@database
@datamapper
@import
@export
@integration-import-export
Feature: Integration - Import Export

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis

#
#  1. integration-import both methods, for investigation one of them use integration-import(visible)-export_backup.feature
#
  @integration-import-export-classic-input
  Scenario: Import and export classic-input

    Given created connections
      | Slack | QE Slack | QE Slack | SyndesisQE Slack test |
    # Indicate boundary that new test start because the last message from the last run could influence this run
    And send message "StartingNewTest" on channel "import_export_test"

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check visibility of visual integration editor
    Then check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    And fill in periodic query input with "SELECT company FROM CONTACT ORDER BY lead_source DESC limit(1)" value
    And fill in period input with "10" value
    And select "Seconds" from sql dropdown
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    # select slack connection
    When select the "QE Slack" connection
    And select "Channel" integration action
    And fill in values by element data-testid
      | channel | import_export_test |

    And click on the "Next" button

    And add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    # add data mapper step
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | company | message |
    And click on the "Done" button

    # finish and save integration
    And click on the "Save" link
    And set integration name "integration-import-export-classic-input"
    And publish integration
    Then Integration "integration-import-export-classic-input" is present in integrations list

    When insert into "CONTACT" table
      | Lorem | Ipsum | Red Hat | e_db |

    # wait for integration to get in active state
    And wait until integration "integration-import-export-classic-input" gets into "Running" state
    And wait until integration integration-import-export-classic-input processed at least 1 message

    Then check that last slack message equals "Red Hat" on channel "import_export_test"

    # Add a new contact
    When insert into "CONTACT" table
      | Fedora | 28 | RH | f_db |

    # export the integration for import tests
    And select the "integration-import-export-classic-input" integration
    Then check visibility of "integration-import-export-classic-input" integration details
    When clean webdriver download folder
    And export the integraion

    # now we have exported integration, we can clean state and try to import
    And clean application state
    And log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" link
    Then import integration "integration-import-export-classic-input"

    When navigate to the "Integrations" page
    Then Integration "integration-import-export-classic-input" is present in integrations list
    Then wait until integration "integration-import-export-classic-input" gets into "Stopped" state

    # check draft status after import
    When select the "integration-import-export-classic-input" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    # start integration and wait for published state
    When click on the "Edit Integration" link
    And click on the "Save" link
    And publish integration
    Then Integration "integration-import-export-classic-input" is present in integrations list
    And wait until integration "integration-import-export-classic-input" gets into "Running" state
    And wait until integration integration-import-export-classic-input processed at least 2 messages
    And check that last slack message equals "RH" on channel "import_export_test"


  #disabled due to bugged dnd selenide method and javascript is not working now - TODO
  @disabled
  @integration-import-export-dnd
  Scenario: Import and export drag and drop
    Given created connections
      | Slack | QE Slack | QE Slack | SyndesisQE Slack test |
    # Indicate boundary that new test start because the last message from the last run could influence this run
    And send message "StartingNewTest" on channel "import_export_test"

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check visibility of visual integration editor
    Then check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    And fill in periodic query input with "SELECT company FROM CONTACT ORDER BY lead_source DESC limit(1)" value
    And fill in period input with "60" value
    And select "Seconds" from sql dropdown
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    # select slack connection
    When select the "QE Slack" connection
    And select "Channel" integration action
    And fill in values by element data-testid
      | channel | import_export_test |

    And click on the "Next" button

    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | company | message |
    And click on the "Done" button

    # finish and save integration
    And click on the "Save" link
    And set integration name "integration-import-export-dnd"
    And publish integration
    Then Integration "integration-import-export-dnd" is present in integrations list

    When insert into "CONTACT" table
      | Lorem | Ipsum | Red Hat | e_db |

    # wait for integration to get in active state
    And wait until integration "integration-import-export-dnd" gets into "Running" state
    And wait until integration integration-import-export-dnd processed at least 1 message

    Then check that last slack message equals "Red Hat" on channel "import_export_test"

    # Add a new contact
    When insert into "CONTACT" table
      | Fedora | 28 | RH | f_db |

    # export the integration for import tests
    And select the "integration-import-export-dnd" integration
    Then check visibility of "integration-import-export-dnd" integration details
    And export the integraion
    #Add a new contact
    When insert into "CONTACT" table
      | RHEL | 7 | New RH | g_db |

#    And delete the "integration-import-export-dnd" integration
    #wait for pod to be deleted
#    And sleep for "15000" ms
    And navigate to the "Integrations" page
    And click on the "Import" link
    Then drag exported integration "integration-import-export-dnd" file to drag and drop area

    When navigate to the "Integrations" page
    Then Integration "integration-import-export-dnd" is present in integrations list
    And wait until integration "integration-import-export-dnd" gets into "Stopped" state

    # check draft status after import
    When select the "integration-import-export-dnd" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    And sleep for jenkins delay or 3 seconds
    # start integration and wait for active state
    When click on the "Edit Integration" link
    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then Integration "integration-import-export-dnd" is present in integrations list
    And wait until integration "integration-import-export-dnd" gets into "Running" state
    And wait until integration integration-import-export-dnd processed at least 2 messages
    And check that last slack message equals "New RH" on channel "import_export_test"

#
#  3. integration-import from different syndesis instance
#
  @integration-import-from-different-instance
  Scenario: Import from different syndesis instance

    #Add a new contact
    When insert into "CONTACT" table
      | RedHat | HatRed | RHEL | h_db |
    # Indicate boundary that new test start because the last message from the last run could influence this run
    And send message "StartingNewTest" on channel "import_export_test"

    And navigate to the "Integrations" page
    And click on the "Import" link
    # import from resources TODO
    Then import integration from relative file path "src/test/resources/integrations/Imported-integration-another-instance-export.zip"

    When navigate to the "Connections" page
    And sleep for jenkins delay or 5 seconds
    And click on the "Edit" kebab menu button of "QE Slack"
    Then check visibility of "QE Slack" connection details

    When fill in "QE Slack" connection details

    And click on the "Validate" button
    Then check visibility of "Slack has been successfully validated" in alert-info notification

    When click on the "Save" button
    And navigate to the "Integrations" page
    #should be unpublished after import
    And select the "Integration_import_export_test" integration
    Then check visibility of "Stopped" integration status on Integration Detail page

    When click on the "Edit Integration" link
    And click on the "Save" link
    And publish integration
    Then Integration "Integration_import_export_test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "Integration_import_export_test" gets into "Running" state
    And wait until integration Integration_import_export_test processed at least 1 message
    Then validate that logs of integration "Integration_import_export_test" contains string "Started Application in"
    And check that last slack message equals "RHEL" on channel "import_export_test"
