# @sustainer: mastepan@redhat.com

@ui
@ftp
@integrations-ftp-to-ftp
Feature: Integration - FTP to FTP

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    Given deploy FTP server
    Given created connections
      | FTP | FTP | FTP | FTP on OpenShift |
#
#  1. download - upload
#
  @ftp-download-ftp-upload
  Scenario: Create
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "FTP" connection
    And select "download" integration action
    And fill in values
      | File name expression               | ui-ftp-ftp.txt |
      | FTP directory                      | download       |
      | Interval before polling starts     | 1000           |
      | Time interval before the next poll | 500            |
      | Delete file after download         | Yes            |

    And click on the "Next" button
    And click on the "Done" button

    Then check visibility of page "Choose a Finish Connection"
    When select the "FTP" connection
    And select "Upload" integration action
    And fill in values
      | File name expression                | ui-ftp-ftp.txt   |
      | FTP directory                       | upload           |
      | If file exists                      | Override         |
      | Temporary file prefix while copying | copyingprefix    |
      | Temporary file name while copying   | copying_test_out |

    And click on the "Next" button

    And click on the "Done" button

    Then check visibility of page "Add to Integration"
    And publish integration
    And set integration name "ftp-to-ftp E2E"
    And publish integration
    Then Integration "ftp-to-ftp E2E" is present in integrations list
    And wait until integration "ftp-to-ftp E2E" gets into "Running" state
    When put "ui-ftp-ftp.txt" file with content "Hello" in the FTP directory: "download"
    Then validate that file "ui-ftp-ftp.txt" has been transfered from "/download" to "/upload" FTP directory
