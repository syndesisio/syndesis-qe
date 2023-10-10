# @sustainer: avano@redhat.com

@rest
@integration-sf-producers
@salesforce
@amqbroker
@activemq
@long-running
Feature: Integration - Salesforce
  Background:
    Given clean SF, removes all leads with email: "jdoeprod@sf-producers.feature,joedoeprod@sf-producers.feature,test@integration-sf-db.feature,test@salesfoce-to-database.feature"
      And deploy ActiveMQ broker
      And clean destination type "queue" with name "sf-producers-input"
      And clean destination type "queue" with name "sf-producers-output"
      And create ActiveMQ connection
      And create SalesForce connection
    And create SF lead with first name: "John", last name: "Doe", email: "jdoeprod@sf-producers.feature" and company: "ACME"
      And create ActiveMQ "subscribe" action step with destination type "queue" and destination name "sf-producers-input"
      And change "out" datashape of previous step to "JSON_INSTANCE" type with specification '{"Id":"leadid"}'

  @integration-sf-producers-delete-record
  Scenario: AMQ to Delete record
    When start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/Id" to "/id"
      And create SF "delete-sobject" action step with properties
      | sObjectName | Lead |
      And create integration with name: "AMQ-SF delete record"
    Then wait for integration with name: "AMQ-SF delete record" to become active
    When publish message with content '{"Id":"LEAD_ID"}' to queue "sf-producers-input"
    Then verify that lead was deleted

  @integration-sf-producers-delete-record-external-id
  Scenario: AMQ to Delete record with external id
    When start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/Id" to "/id"
      And create SF "delete-sobject-with-id" action step with properties
      | sObjectName   | Lead  |
      | sObjectIdName | Email |
      And create integration with name: "AMQ-SF delete record with external id"
    Then wait for integration with name: "AMQ-SF delete record with external id" to become active
    When publish message with content '{"Id":"jdoeprod@sf-producers.feature"}' to queue "sf-producers-input"
    Then verify that lead was deleted

  @integration-sf-producers-fetch-record
  Scenario: AMQ to Fetch record to AMQ
    When start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/Id" to "/id"
      And create SF "get-sobject" action step with properties
        | sObjectName | Lead |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
      And create integration with name: "AMQ-SF-AMQ fetch record"
    Then wait for integration with name: "AMQ-SF-AMQ fetch record" to become active
    When publish message with content '{"Id":"LEAD_ID"}' to queue "sf-producers-input"
    Then verify that lead json object was received from queue "sf-producers-output"

  @integration-sf-producers-fetch-record-external-id
  Scenario: AMQ to Fetch record with external id to AMQ
    When start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/Id" to "/id"
      And create SF "get-sobject-with-id" action step with properties
      | sObjectName   | Lead  |
      | sObjectIdName | Email |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
      And create integration with name: "AMQ-SF-AMQ fetch record with external id"
    Then wait for integration with name: "AMQ-SF-AMQ fetch record with external id" to become active
    When publish message with content '{"Id":"jdoeprod@sf-producers.feature"}' to queue "sf-producers-input"
    Then verify that lead json object was received from queue "sf-producers-output"

  @integration-sf-producers-create-record
  @datamapper
  Scenario: AMQ to Create a new record to AMQ
    When create SF "create-sobject" action step with properties
      | sObjectName | Lead |
      And start mapper definition with name: "integration-sf-producers-create-record"
      And MAP using Step 2 and field "/id" to "/id"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"id":"abc"}'
      And create integration with name: "AMQ-SF-AMQ new record"
    Then wait for integration with name: "AMQ-SF-AMQ new record" to become active
    When publish message with content '{"FirstName":"Joe", "LastName":"Doe","Email":"joedoeprod@sf-producers.feature","Company":"XYZ"}' to queue "sf-producers-input"
    Then verify that lead with email "joedoeprod@sf-producers.feature" was created
    And verify that lead creation response with email "joedoeprod@sf-producers.feature" was received from queue "sf-producers-output"

  @integration-sf-producers-update-record
  Scenario: AMQ to Update record
    When create SF "update-sobject" action step with properties
      | sObjectName | Lead |
      And create integration with name: "AMQ-SF update record"
    Then wait for integration with name: "AMQ-SF update record" to become active
    When publish message with content '{"Id":"LEAD_ID", "Email":"joedoeprod@sf-producers.feature"}' to queue "sf-producers-input"
    Then verify that leads email was updated to "joedoeprod@sf-producers.feature"

  @integration-sf-producers-upsert-record-insert
  @datamapper
  Scenario: AMQ to Upsert record insert to AMQ
    When create SF "upsert-sobject" action step with properties
      | sObjectName   | Lead  |
      | sObjectIdName | Email |
      And start mapper definition with name: "integration-sf-producers-upsert-record-insert"
      And MAP using Step 2 and field "/id" to "/id"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"id":"abc"}'
      And create integration with name: "AMQ-SF-AMQ upsert insert record"
    Then wait for integration with name: "AMQ-SF-AMQ upsert insert record" to become active
    # This user does not exist, will be created
    When publish message with content '{"Email":"joedoeprod@sf-producers.feature", "Firstname":"Joe", "Lastname":"Doe", "Company":"XYZ"}' to queue "sf-producers-input"
    Then verify that lead with email "joedoeprod@sf-producers.feature" was created
    And verify that lead creation response with email "joedoeprod@sf-producers.feature" was received from queue "sf-producers-output"

  @integration-sf-producers-upsert-record-update
  Scenario: AMQ to Upsert record update to AMQ
    When create SF "upsert-sobject" action step with properties
      | sObjectName    | Lead  |
      | sObjectIdName  | Email |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
      And create integration with name: "AMQ-SF-AMQ upsert update record"
    Then wait for integration with name: "AMQ-SF-AMQ upsert update record" to become active
    # This user does exist, but with first name John
    When publish message with content '{"Email":"jdoeprod@sf-producers.feature", "Firstname":"Joe", "Lastname":"Doe", "Company":"XYZ"}' to queue "sf-producers-input"
    Then verify that lead name was updated
