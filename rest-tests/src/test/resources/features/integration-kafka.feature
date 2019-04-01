# @sustainer: avano@redhat.com

@rest
@integration-kafka
@kafka
@salesforce
@datamapper
@amqbroker
@activemq
Feature: Integration - Kafka
  Background:
    Given clean SF, removes all leads with email: "jdoe@acme.com"
      And deploy Kafka broker and add account
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And create SalesForce connection
      And create Kafka connection
    When create SF "on-create" action step with properties
      | sObjectName | Lead |
      And start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/Id" to "/Id"
      And create Kafka "publish" step with topic "sf-leads"
      And change "in" datashape of previous step to "JSON_SCHEMA" type with specification '{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"Id":{"type":"string"}},"required":["Id"]}'
    When create integration with name: "SF-Kafka"
    Then wait for integration with name: "SF-Kafka" to become active
    Given create Kafka "subscribe" step with topic "sf-leads"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sf-leads"
    When create integration with name: "Kafka-AMQ"
    Then wait for integration with name: "Kafka-AMQ" to become active

  @integration-kafka-to-sf
  Scenario: SalesForce to Kafka to AMQ
    When create SF lead with first name: "John", last name: "Doe", email: "jdoe@acme.com" and company: "ACME"
    Then verify that lead json object was received from queue "sf-leads"
