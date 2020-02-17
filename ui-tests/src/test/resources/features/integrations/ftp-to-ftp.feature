# @sustainer: mastepan@redhat.com

@ui
@ftp
@integrations-ftp-to-ftp
Feature: Integration - FTP to FTP

  Background: Clean application state
    Given clean application state
    And deploy FTP server
    And put "ui-ftp-ftp.txt" file with content "Hello-FTP" in the directory: "download" using FTP
    And log into the Syndesis
    And created connections
      | FTP | FTP | FTP | FTP on OpenShift |
#
#  1. download - upload
#
  @ftp-download-ftp-upload
  Scenario: Create
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "FTP" connection
    And select "download" integration action
    And fill in values by element data-testid
      | filename              | ui-ftp-ftp.txt |
      | directoryname         | download       |
      | initialdelay          | 1000           |
      | initialdelay-duration | Milliseconds   |
      | delay                 | 500            |
      | delay-duration        | Milliseconds   |
      | delete                | Yes            |

    And click on the "Next" button
    And click on the "Next" button

    Then check visibility of page "Choose a Finish Connection"
    When select the "FTP" connection
    And select "Upload" integration action
    And fill in values by element data-testid
      | filename      | ui-ftp-ftp.txt   |
      | directoryname | upload           |
      | exists        | Override         |
      | tempfilename  | copyingprefix    |
      | tempprefix    | copying_test_out |

    And click on the "Next" button
    And click on the "Next" button

    And publish integration
    And set integration name "ftp-to-ftp E2E"
    And publish integration
    Then Integration "ftp-to-ftp E2E" is present in integrations list
    And wait until integration "ftp-to-ftp E2E" gets into "Running" state

    Then validate that file "ui-ftp-ftp.txt" has been transfered from "/download" to "/upload" directory using FTP
    And check that "ui-ftp-ftp.txt" file in "/upload" directory has content "Hello-FTP" using FTP
