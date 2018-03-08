Feature: ftp scenario

  Background: Clean application state

  @integration-ftp-ftp
  Scenario: ftp - ftp integration
    Given clean application state
    Given clean FTP server
    And create the FTP connection using "ftp" template

    Then puts "1MB.zip" file in the FTP "/download" directory

    And creates start FTP download action with values
      | fileName | directoryName | initialDelay | delay | delete |
      | 1MB.zip  | /download     | 1000         | 500   | Yes    |
    Then creates finish FTP upload action with values
      | fileName     | directoryName | fileExist | tempPrefix       | tempFileName        |
      | 1MB_out.zip  | /upload       | Override  | copyingprefix    | copying_1MB_out     |
    Then create integration with name: "FTP to FTP rest test"
    Then wait for integration with name: "FTP to FTP rest test" to become active

    Then validate that file "1MB.zip" has been transfered from "/download" to "/upload" directory"
