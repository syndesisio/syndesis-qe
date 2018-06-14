@integration-ftp-ftp
Feature: Integration - FTP to FTP

  Background: Prepare
    Given clean application state
    Given clean FTP server
    Given deploy FTP server

  Scenario: Download upload
    When create the FTP connection using "FTP" template
    And create start FTP download action with values
      | fileName | directoryName | initialDelay | delay | delete |
      | test.txt | download      | 1000         | 500   | true   |
    And create finish FTP upload action with values
      | fileName  | directoryName | fileExist | tempPrefix    | tempFileName        |
      | test.txt  | upload        | Override  | copyingprefix | copying_test_out    |
    Then create integration with name: "FTP to FTP rest test"
    And wait for integration with name: "FTP to FTP rest test" to become active
    And validate that file "test.txt" has been transfered from "/download" to "/upload" FTP directory
