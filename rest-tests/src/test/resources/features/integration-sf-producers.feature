@integration-sf-producers

Feature: Integration - SalesForce producers

  Background:
    Given clean SF, removes all leads with email: "jdoe@acme.com"
      And deploy AMQ broker and add accounts
      And create the AMQ connection
      And create SF connection
      And create SF lead with first name: "John", last name: "Doe", email: "jdoe@acme.com" and company: "ACME"
      And create AMQ "subscribe" action step with destination type "queue" and destination name "sf-producers-input"

  @integrations-sf-producers-delete-record
  Scenario: Delete record
    Given create SF "delete-sobject" action step on field: "Lead"
    When create integration with name: "AMQ-SF delete record"
    Then wait for integration with name: "AMQ-SF delete record" to become active
    When publish message with id to queue "sf-producers-input"
    Then verify that lead was deleted
