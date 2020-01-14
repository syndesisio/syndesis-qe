# @sustainer: avano@redhat.com

@box
@file-transfer
Feature: Integration - File transfer

  Background: Prepare
    Given clean application state
      And create Box connection
      And remove all files from Box

  @activemq
  @datamapper
  @integration-box-amq
  Scenario: Box download to AMQ
    Given deploy ActiveMQ broker
      And clean destination type "queue" with name "box-out"
      And create ActiveMQ connection
      And upload file with name "syndesis-integration.txt" and content "Hello integration!" to Box
    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 10000  |
      And create Box download action step with fileId
      And start mapper definition with name: "box-amq"
      And COMBINE using Step 2 and strategy "Dash" into "/text" and sources
        | /content | /id | /size |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "box-out"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"text":"a"}'
    When create integration with name: "BOX-AMQ"
    Then wait for integration with name: "BOX-AMQ" to become active
      And verify the Box AMQ response from queue "box-out" with text "Hello integration!"

  @database
  @activemq
  @datamapper
  @integration-sql-box-amq
  Scenario: SQL to Box download to AMQ
    Given deploy ActiveMQ broker
      And clean destination type "queue" with name "box-out"
      And create ActiveMQ connection
      And upload file with name "file1.txt" and content "Hello from file1.txt!" to Box
      And upload file with name "file2.txt" and content "Hello from file2.txt!" to Box
      And clean "BOX_IDS" table
      And execute SQL command "CREATE TABLE BOX_IDS(id varchar)"
      And insert box file ids to box id table
    When create start DB periodic sql invocation action step with query "SELECT * FROM BOX_IDS" and period "600000" ms
      And add a split step
      And start mapper definition with name: "sql-split-box"
      And MAP using Step 2 and field "/id" to "/fileId"
      And create Box download action step without fileId
      And start mapper definition with name: "box-download-amq"
      And MAP using Step 4 and field "/content" to "/text"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "box-out"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"text":"a"}'
      And create integration with name: "SQL-BOX-AMQ"
    Then wait for integration with name: "SQL-BOX-AMQ" to become active
      And verify that all box messages were received from "box-out" queue:
        | {"text":"Hello from file1.txt!"} |
        | {"text":"Hello from file2.txt!"} |

  @database
  @datamapper
  @integration-sql-box
  Scenario: SQL to Box upload
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "60000" ms
      And start mapper definition with name: "sql-box"
      And MAP using Step 1 and field "/<>/last_name" to "/text"
      And create Box upload action step with file name "box-upload.json"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"text":"a"}'
      And create integration with name: "SQL-BOX"
    Then wait for integration with name: "SQL-BOX" to become active
      And verify that file "box-upload.json" with content '{"text":"Jackson"}' is present in Box
    When execute SQL command "UPDATE CONTACT SET LAST_NAME='Doe' WHERE LAST_NAME='Jackson'"
      And sleep for jenkins delay or "60" seconds
    Then verify that file "box-upload.json" with content '{"text":"Doe"}' is present in Box

  @ENTESB-12553
  @dropbox
  @integration-dropbox-box
  Scenario: Dropbox to Box upload
    Given delete file with path "/box/test_box.txt" from Dropbox
    When create Dropbox connection
      And create Dropbox "download" action step with file path: "/box"
      And create Box upload action step with file name ""
      And create integration with name: "Dropbox-Box"
      And upload file with path "/box/test_box.txt" and content "Hello from Dropbox!" on Dropbox
    Then wait for integration with name: "Dropbox-Box" to become active
      And verify that file "test_box.txt" with content 'Hello from Dropbox!' is present in Box

  @ENTESB-12553
  @s3
  @integration-s3-box
  Scenario: S3 to Box upload
    When create sample buckets on S3 with name "syndesis-box-upload"
      And create S3 connection using "syndesis-box-upload" bucket
      And create S3 polling START action step with bucket: "syndesis-box-upload"
      And create Box upload action step with file name ""
      And create integration with name: "S3-Box"
    Then wait for integration with name: "S3-Box" to become active
    When create a new text file in bucket "syndesis-box-upload" with name "test_box.txt" and text "Hello from AWS!"
      And verify that file "test_box.txt" with content 'Hello from AWS!' is present in Box

  @ftp
  @integration-ftp-box
  Scenario: FTP to Box
    Given deploy FTP server
      And delete file "/download/test_box.txt" from FTP
      And put "test_box.txt" file with content "Hello from FTP!" in the FTP directory: "download"
    When create FTP connection
      And create FTP "download" action with values
        | fileName     | directoryName | initialDelay | delay | delete |
        | test_box.txt | download      | 1000         | 500   | true   |
      And create Box upload action step with file name ""
      And create integration with name: "FTP to Dropbox rest test"
    Then wait for integration with name: "FTP to Dropbox rest test" to become active
      And verify that file "test_box.txt" with content 'Hello from FTP!' is present in Box
