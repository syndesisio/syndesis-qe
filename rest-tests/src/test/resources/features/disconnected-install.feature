# @sustainer: avano@redhat.com

# Meant to be run on disconnected environment with test support enabled
# Tries to build integrations with all the connectors that are not touching external services when creating steps in the integration
# Required parameter for mongo deployment is "mongo.docker.registry" that should point to the docker registry visible from the cluster

@rest
@disconnected-install
@ignore
Feature: Disconnected install
  Background:
    Given clean application state

  Scenario: Amazon S3
    Given create S3 connection using "asd" bucket
    When create S3 polling START action step with bucket: "asd"
    And add log step
    And create integration with name: "s3" and without validating connections
    Then verify that integration "s3" build is successful

  Scenario: AMQP Message Broker
    Given create ActiveMQ accounts
      And create AMQP connection
    When create AMQP "subscribe" action step with properties:
      | destinationName | test  |
      | destinationType | queue |
      And add log step
      And create integration with name: "amqp" and without validating connections
    Then verify that integration "amqp" build is successful

  Scenario: Apache Kudu
    Given create Kudu account
      And create Kudu connection
    When create Kudu "scan" step with table "test"
      And add log step
      And create integration with name: "kudu" and without validating connections
    Then verify that integration "kudu" build is successful

  Scenario: Box
    Given create Box connection
    When create Box download action step without fileId
    And add log step
    And create integration with name: "box" and without validating connections
    Then verify that integration "box" build is successful

  Scenario: Database
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 10000 ms
    And add log step
    And create integration with name: "db" and without validating connections
    Then verify that integration "db" build is successful

  Scenario: Dropbox
    Given create Dropbox connection
    When create Dropbox "download" action step with file path: "test.txt"
    And add log step
    And create integration with name: "dropbox" and without validating connections
    Then verify that integration "dropbox" build is successful

  Scenario: Email
    Given create Email SMTP SSL connection
    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 10000  |
      And create Email "send" action with properties:
      | from | test@example.com |
    And create integration with name: "email" and without validating connections
    Then verify that integration "email" build is successful

  Scenario: FHIR
    Given create FHIR connection
    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 10000  |
      And create FHIR "read" action with resource type "Account"
      And create integration with name: "fhir" and without validating connections
    Then verify that integration "fhir" build is successful

  Scenario: FTP
    Given create FTP connection
    When create FTP "download" action with values
      | fileName         | directoryName | initialDelay | delay | delete |
      | test_dropbox.txt | download      | 1000         | 500   | true   |
      And add log step
      And create integration with name: "ftp" and without validating connections
    Then verify that integration "ftp" build is successful

  Scenario: HTTP
    Given create HTTP accounts
      And create HTTP connection
    When create HTTP "GET" step
      And add log step
      And create integration with name: "http" and without validating connections
    Then verify that integration "http" build is successful

  Scenario: HTTPS
    Given create HTTP accounts
      And create HTTPS connection
    When create HTTPS "GET" step
      And add log step
      And create integration with name: "https" and without validating connections
    Then verify that integration "https" build is successful

  Scenario: IRC
    Given create IRC account
      And create IRC connection
    When create IRC "privmsg" step with nickname "listener" and channels "#test"
      And add log step
      And create integration with name: "irc" and without validating connections
    Then verify that integration "irc" build is successful

  Scenario: Jira
    Given create Jira connection
    When create JIRA "retrieve" step with JQL 'PROJECT = "test"'
      And add log step
      And create integration with name: "jira" and without validating connections
    Then verify that integration "jira" build is successful

  Scenario: Kafka Message Broker
    Given create Kafka accounts
      And create Kafka connection
    When create Kafka "subscribe" step with topic "test"
      And add log step
      And create integration with name: "kafka" and without validating connections
    Then verify that integration "kafka" build is successful

  Scenario: MongoDB
    Given deploy MongoDB 3.6 database
      And create MongoDB account
      And create MongoDB connection
    When create MongoDB "consumer-tail" with collection "test"
      And add log step
      And create integration with name: "mongo" and without validating connections
    Then verify that integration "mongo" build is successful

  Scenario: Red Hat AMQ
    Given create ActiveMQ accounts
      And create ActiveMQ connection
    When create ActiveMQ "subscribe" action step with destination type "queue" and destination name "test"
      And add log step
      And create integration with name: "amq" and without validating connections
    Then verify that integration "amq" build is successful

  Scenario: SFTP
    Given create SFTP connection
    When create SFTP "download" action with values
      | fileName     | directoryName  | initialDelay | delay | delete |
      | test_box.txt | /test/download | 1000         | 500   | true   |
      And add log step
      And create integration with name: "sftp" and without validating connections
    Then verify that integration "sftp" build is successful

  Scenario: Telegram
    Given create Telegram connection
    When create telegram receive action
      And add log step
      And create integration with name: "telegram" and without validating connections
    Then verify that integration "telegram" build is successful

  Scenario: Datamapper
    Given create ActiveMQ accounts
      And create ActiveMQ connection
    When create ActiveMQ "subscribe" action step with destination type "queue" and destination name "in"
      And change "out" datashape of previous step to "JSON_INSTANCE" type with specification '{"msg":"hello"}'
      And start mapper definition with name: "sqs-amq"
      And MAP using Step 1 and field "/msg" to "/output"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "out"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"output":"hi"}'
      And create integration with name: "datamapper" and without validating connections
    Then verify that integration "datamapper" build is successful

  Scenario: Split/Aggregate
    Given create ActiveMQ accounts
      And create ActiveMQ connection
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 10000 ms
      And add a split step
      And create basic filter step for "last_name" with word "Doe" and operation "contains"
      And add an aggregate step
      And add log step
      And create integration with name: "split-aggregate" and without validating connections
    Then verify that integration "split-aggregate" build is successful
