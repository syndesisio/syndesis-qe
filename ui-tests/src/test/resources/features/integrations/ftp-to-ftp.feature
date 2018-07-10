# @sustainer: mastepan@redhat.com

@integrations-ftp-to-ftp
Feature: Integration - FTP to FTP

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    Given clean FTP server
    Given deploy FTP server
    Given created connections
      | FTP | FTP | FTP | FTP on OpenShift |
#
#  1. download - upload
#
  @ftp-download-ftp-upload
  Scenario: Create

#     -- TBD. for the time being, no need for adding file to FTP server:
#    Then puts "1MB.zip" file in the FTP 'from' "directoryName"

    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "FTP" connection
    And select "download" integration action
    And fill in values
      | File name expression               | test.txt |
      | FTP directory                      | download |
      | Milliseconds before polling starts | 1000     |
      | Milliseconds before the next poll  | 500      |
      | Delete file after download         | Yes      |

    And click on the "Next" button
#    And fill in values
#      | Select Type           | JSON Schema        |
#      | Definition            | sample text        |
#      | Data Type Name        | sample name        |
#      | Data Type Description | sample description |
    And click on the "Done" button
#
    Then check visibility of page "Choose a Finish Connection"
    When select the "FTP" connection
    And select "Upload" integration action
    And fill in values
      | File name expression                | test.txt         |
      | FTP directory                       | upload           |
      | If file exists                      | Override         |
      | Temporary file prefix while copying | copyingprefix    |
      | Temporary file name while copying   | copying_test_out |

    And click on the "Next" button

#    And fill in values
#      | Select Type           | JSON Schema        |
#      | Definition            | sample text        |
#      | Data Type Name        | sample name        |
#      | Data Type Description | sample description |
    And click on the "Done" button

    Then check visibility of page "Add to Integration"
    And click on the "Publish" button
    And set integration name "ftp-to-ftp E2E"
    And click on the "Publish" button
    Then check visibility of "ftp-to-ftp E2E" integration details
    Then navigate to the "Integrations" page
    Then wait until integration "ftp-to-ftp E2E" gets into "Running" state
    Then sleep for jenkins delay or "5" seconds
#    to be done:
    Then validate that file "test.txt" has been transfered from "/download" to "/upload" FTP directory
