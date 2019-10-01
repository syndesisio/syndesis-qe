# @sustainer: avano@redhat.com

@box
@file-transfer
Feature: Integration - File transfer

  Background: Prepare
    Given clean application state
      And deploy ActiveMQ broker
      And clean destination type "queue" with name "box-out"
      And create Box connection
      And create ActiveMQ connection
      And remove all files from Box
      And clean "BOX_IDS" table

  @activemq
  @integration-box-amq
  Scenario: Box download to AMQ
    Given upload file with name "syndesis-integration.txt" and content "Hello integration!" to Box
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
    Given upload file with name "file1.txt" and content "Hello from file1.txt!" to Box
      And upload file with name "file2.txt" and content "Hello from file2.txt!" to Box
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
