@integration-sf-producers

Feature: Integration - SalesForce producers
  Background:
#    Given deploy Syndesis from template
#    And wait for Syndesis to become ready
    Given clean SF, removes all leads with email: "jdoe@acme.com"
      And deploy AMQ broker if it doesnt exist
      And create the AMQ connection
      And create SF connection
      And create SF lead with first name: "John", last name: "Doe", email: "jdoe@acme.com" and company: "ACME"
      And create AMQ "subscribe" action step with destination type "queue" and destination name "sf-producers-input"

  @integration-sf-producers-delete-record
  Scenario: Delete record
    When create SF "delete-sobject" action step on field: "Lead"
      And create integration with name: "AMQ-SF delete record"
    Then wait for integration with name: "AMQ-SF delete record" to become active
    When publish message with content '{"Id":"LEAD_ID"}' to queue "sf-producers-input"
    Then verify that lead was deleted

  @integration-sf-producers-delete-record-external-id
  Scenario: Delete record with external id
    When create SF "delete-sobject-with-id" action step with properties
      | sObjectName   | Lead  |
      | sObjectIdName | Email |
      And create integration with name: "AMQ-SF delete record with external id"
    Then wait for integration with name: "AMQ-SF delete record with external id" to become active
    When publish message with content '{"Id":"jdoe@acme.com"}' to queue "sf-producers-input"
    Then verify that lead was deleted

  @integration-sf-producers-fetch-record
  Scenario: Fetch record
    When create SF "get-sobject" action step on field: "Lead"
      And create AMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
      And create integration with name: "AMQ-SF-AMQ fetch record"
    Then wait for integration with name: "AMQ-SF-AMQ fetch record" to become active
    When publish message with content '{"Id":"LEAD_ID"}' to queue "sf-producers-input"
    Then verify that lead json object was received from queue "sf-producers-output"

  @integration-sf-producers-fetch-record-external-id
  Scenario: Fetch record with external id
    When create SF "get-sobject-with-id" action step with properties
      | sObjectName   | Lead  |
      | sObjectIdName | Email |
    And create AMQ "publish" action step with destination type "queue" and destination name "sf-producers-output"
    And create integration with name: "AMQ-SF-AMQ fetch record"
    Then wait for integration with name: "AMQ-SF-AMQ fetch record" to become active
    When publish message with content '{"Id":"jdoe@acme.com"}' to queue "sf-producers-input"
    Then verify that lead json object was received from queue "sf-producers-output"
