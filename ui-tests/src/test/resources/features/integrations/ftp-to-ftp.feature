@integrations-ftp-to-ftp
Feature: Test functionality of FTP connection

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis
    Given clean FTP server
    Given created connections
      | Ftp | Ftp | Ftp | Ftp on local minishift |
#
#  1. download - upload
#
  @ftp-download-ftp-upload
  Scenario: Create integration to test DB connector for read and update operations

#     -- TBD. for the time being, no need for adding file to FTP server:
#    Then puts "1MB.zip" file in the FTP 'from' "directoryName"

    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

      # select salesforce connection as 'from' point
    When Camilla selects the "Ftp" connection
    And she selects "download" integration action
    And she fills ftp download form with values
      | fileName | directoryName | initialDelay | delay | delete |
      | 1MB.zip  | /             | 1000         | 500   | Yes    |
    And clicks on the "Done" button
    And she fills specify output data type form with values
      | kind       | specification      | name                | description      |
      | 'Any Type' | some_specification | data_type_some_name | some_description |
    And clicks on the "Next" button

    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "Ftp" connection
    And she selects "Upload" integration action
    And she fills ftp upload form with values
      | fileName    | directoryName | fileExist | tempPrefix    | tempFileName    |
      | 1MB_out.zip | /upload       | Override  | copyingprefix | copying_1MB_out |
    And clicks on the "Done" button
    And she fills specify output data type form with values
      | kind       | specification      | name                | description      |
      | 'Any Type' | some_specification | data_type_some_name | some_description |
    And clicks on the "Next" button

    Then Camilla is presented with the Syndesis page "Add to Integration"
    And clicks on the "Publish" button
    And she sets the integration name "ftp-to-ftp E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "ftp-to-ftp E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "ftp-to-ftp E2E" gets into "Active" state

#    to be done:
    Then validate that file "1MB.zip" was deleted and file "1MB_out.zip" is in "/upload" directory
