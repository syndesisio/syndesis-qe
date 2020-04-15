# @sustainer: sveres@redhat.com

@ui
@sftp
@integrations-sftp-to-sftp
Feature: Integration - SFTP to SFTP

  Background: Clean application state
    Given clean application state
    And log into the Syndesis
    And deploy SFTP server
    And prepare SFTP server
    And put "ui-sftp-sftp.txt" file with content "Hello-SFTP" in the directory: "/test/download" using SFTP
    And check that "ui-sftp-sftp.txt" file in "/test/download" directory has content "Hello-SFTP" using SFTP
    And created connections
      | SFTP | SFTP | SFTP | SFTP on OpenShift |
#
#  1. download - upload
#
  @sftp-download-sftp-upload
  Scenario: Create
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "SFTP" connection
    And select "download" integration action
    And fill in values by element data-testid
      | filename              | ui-sftp-sftp.txt |
      | directoryname         | test/download    |
      | initialdelay          | 1000             |
      | initialdelay-duration | Milliseconds     |
      | delay                 | 500              |
      | delay-duration        | Milliseconds     |
      | delete                | Yes              |

    And click on the "Next" button
    And click on the "Next" button

    Then check visibility of page "Choose a Finish Connection"
    When select the "SFTP" connection
    And select "Upload" integration action
    And fill in values by element data-testid
      | filename      | ui-sftp-sftp.txt |
      | directoryname | test/upload      |
      | exists        | Override         |
      | tempfilename  | copyingprefix    |
      | tempprefix    | copying_test_out |

    And click on the "Next" button
    And click on the "Next" button

    And publish integration
    And set integration name "sftp-to-sftp E2E"
    And publish integration
    Then Integration "sftp-to-sftp E2E" is present in integrations list
    And wait until integration "sftp-to-sftp E2E" gets into "Running" state
    And wait until integration sftp-to-sftp E2E processed at least 1 message
    Then validate that file "ui-sftp-sftp.txt" has been transfered from "/test/download" to "/test/upload" directory using SFTP
