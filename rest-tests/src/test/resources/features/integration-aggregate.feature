# @sustainer: avano@redhat.com

@rest
@database
@amqbroker
@activemq
@split
@aggregate
Feature: Integration - HTTP
  @delorean
  @integration-db-amq-aggregate
  Scenario: SQL to AMQ with aggregate
    Given clean application state
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean "CONTACT" table
      And inserts into "CONTACT" table
        | Tony  | Do  | Syndesis-qe | db |
        | John  | Doe | Syndesis-qe | db |
        | Jeff  | Doe | Syndesis-qe | db |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "10000" ms
      And add a split step
      And create basic filter step for "last_name" with word "Doe" and operation "contains"
      And add an aggregate step
      And start mapper definition with name: "datamapper"
      And MAP using Step 4 and field "/<>/first_name" to "/message"
      And add "concatenate" transformation on "source" field with id "/<>/first_name" with properties
        | delimiter | && |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "db-amq-aggregate"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"message":"test"}'
      And create integration with name: "DB-AMQ-Aggregate"
    Then wait for integration with name: "DB-AMQ-Aggregate" to become active
      And verify that JMS message with content '{"message":"John&&Jeff"}' was received from "queue" "db-amq-aggregate"
