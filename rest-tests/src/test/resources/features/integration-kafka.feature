# @sustainer: avano@redhat.com

@rest
@integration-kafka
@kafka
Feature: Integration - Kafka
  Background:
    Given clean application state
      And deploy Kafka broker

  @salesforce
  @datamapper
  @amqbroker
  @activemq
  @integration-kafka-to-sf
  Scenario: SalesForce to Kafka to AMQ
    Given clean SF, removes all leads with email: "test@integration-kafka.feature"
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And create SalesForce connection
      And create Kafka connection
    When create SF "on-create" action step with properties
      | sObjectName | Lead |
      # filter, due to other test can create Lead in the same time
    And create basic filter step for "Email" with word "test@integration-kafka.feature" and operation "contains"
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
    When create SF lead with first name: "John", last name: "Doe", email: "test@integration-kafka.feature" and company: "ACME"
    Then verify that lead json object was received from queue "sf-leads"

  @ENTESB-13846
  @kafka-extra-options
  Scenario: Kafka Extra Options
    When create Kafka connection with extra options
      | clientId         | myclient |
      | sessionTimeoutMs | 66666    |
      And create Kafka "subscribe" step with topic "test"
      And add log step
    When create integration with name: "kafka-extra-options"
    Then wait for integration with name: "kafka-extra-options" to become active
      And check that kafka option "client.id" is set to "myclient" in "kafka-extra-options" integration
      And check that kafka option "session.timeout.ms" is set to "66666" in "kafka-extra-options" integration
