# @sustainer: avano@redhat.com

@rest
@integration-kafka
@kafka
@salesforce
@datamapper
@activemq
Feature: Integration - Kafka producer / consumer
  Background:
    Given clean SF, removes all leads with email: "jdoe@acme.com"
      And deploy Kafka broker and add account
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And create SalesForce connection
      And create Kafka connection
      And create SF "salesforce-on-create" action step on field: "Lead"
      And start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/Id" to "/Id"
      And create Kafka publish step with topic "sf-leads"
      And change datashape of previous step to "in" direction, "JSON_SCHEMA" type with specification '{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"Id":{"type":"string"}},"required":["Id"]}'
    When create integration with name: "SF-Kafka"
    Then wait for integration with name: "SF-Kafka" to become active
    Given create Kafka subscribe step with topic "sf-leads"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-leads"
    When create integration with name: "Kafka-AMQ"
    Then wait for integration with name: "Kafka-AMQ" to become active

  @integration-kafka-to-sf
  Scenario: SalesForce to Kafka to AMQ
    When create SF lead with first name: "John", last name: "Doe", email: "jdoe@acme.com" and company: "ACME"
    Then verify that lead json object was received from queue "sf-leads"
