@file-transfer
Feature: File transfer

  Background: Prepare
    Given clean application state

  @integrations-ftp-dropbox
  Scenario: FTP to Dropbox
    Given clean FTP server
    Given deploy FTP server
#    todo(sveres): this upload does not work, to be investigated where is the problem
#    @wip Then puts "test_ftp.txt" file with content "Hello from FTP!" in the FTP directory: "download"
    When create the FTP connection using "ftp" template
    When create Dropbox connection

    And create start FTP download action with values
      | fileName | directoryName | initialDelay | delay | delete |
      | test.txt | download      | 1000         | 500   | true   |

    And create Dropbox upload FINISH action step with file path: "/test.txt"

    Then create integration with name: "FTP to Dropbox rest test"
    And wait for integration with name: "FTP to Dropbox rest test" to become active

    Then check that file with path "/test.txt" exists on Dropbox
    Then delete file with path "/test.txt" from Dropbox

  @integrations-s3-ftp
  Scenario: S3 to FTP
#    preparation:
    Given clean FTP server
    Given deploy FTP server
    When create the FTP connection using "ftp" template
    And create sample buckets on S3 with name "syndesis-server-bucket-from"
    Then create a new text file in bucket "syndesis-server-bucket-from" with name "test_aws.txt" and text "Hello from AWS!"
    And create S3 connection using "syndesis-server-bucket-from" bucket

#    integration:
    And create S3 polling START action step with bucket: "syndesis-server-bucket-from"
    And create finish FTP upload action with values
      | fileName     | directoryName   | fileExist | tempPrefix    | tempFileName     |
      | test_aws.txt | upload_aws      | Override  | copyingprefix | copying_test_out |
    When create integration with name: "S3 to FTP rest test"
    Then wait for integration with name: "S3 to FTP rest test" to become active

#    validation & clean:
    And validate that file "test_aws.txt" has been transfered to "/upload_aws" FTP directory
    Then clean S3, remove sample bucket with name: "syndesis-server-bucket-from"

  @integrations-dropbox-s3
  Scenario: Dropbox to S3

#    preparation:
    When create Dropbox connection
    When upload file with path "/test_dbx.txt" and content "Hello from Dropbox!" on Dropbox
    Then check that file with path "/test_dbx.txt" exists on Dropbox

    And create sample buckets on S3 with name "syndesis-server-bucket-to"
    And create S3 connection using "syndesis-server-bucket-to" bucket

#    integration:
    And create Dropbox download START action step with file path: "/test_dbx.txt"
    And create S3 copy FINISH action step with bucket: "syndesis-server-bucket-to" and filename: "test_dbx.txt"

    And create integration with name: "Dropbox to S3 rest test"
    Then wait for integration with name: "Dropbox to S3 rest test" to become active

#    validation & clean:
    Then validate bucket with name "syndesis-server-bucket-to" contains file with name "test_dbx.txt" and text "Hello from Dropbox!"
    Then clean S3, remove sample bucket with name: "syndesis-server-bucket-to"
    Then delete file with path "/test_dbx.txt" from Dropbox
