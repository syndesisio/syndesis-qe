# @sustainer: avano@redhat.com

@rest
@integration-ftp-ftp
@ftp
Feature: Integration - FTP to FTP

  Background: Prepare
    Given clean application state
      And deploy FTP server
      And delete file "/download/test-ftp.txt" from FTP
      And delete file "/upload/test-ftp.txt" from FTP

  Scenario: Download upload
    When create the FTP connection using "FTP" template
      And create start FTP download action with values
        | fileName      | directoryName | initialDelay | delay | delete |
        | test-ftp.txt  | download      | 1000         | 500   | true   |
      And create finish FTP upload action with values
        | fileName      | directoryName | fileExist | tempPrefix    | tempFileName        |
        | test-ftp.txt  | upload        | Override  | copyingprefix | copying_test_out    |
    When create integration with name: "FTP to FTP rest test"
    Then wait for integration with name: "FTP to FTP rest test" to become active
    When put "test-ftp.txt" file with content "Hello FTP" in the FTP directory: "/download"
      And validate that file "test-ftp.txt" has been transfered from "/download" to "/upload" FTP directory
