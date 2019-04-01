# @sustainer: avano@redhat.com

@rest
@integration-http
@integration-http-consumer
@http
@amqbroker
@activemq
Feature: Integration - HTTP
  Background:
    Given clean application state
      And deploy HTTP endpoints
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And create HTTP connection

  @integration-http-consumer-get
  Scenario: GET to AMQ
    Given create HTTP "GET" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-get"
    When create integration with name: "HTTP-GET-AMQ"
    Then wait for integration with name: "HTTP-GET-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "GET" was executed
      And verify that JMS message with content 'get' was received from "queue" "http-get"

  @integration-http-consumer-post
  Scenario: POST to AMQ
    Given create HTTP "POST" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-post"
    When create integration with name: "HTTP-POST-AMQ"
    Then wait for integration with name: "HTTP-POST-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "POST" was executed
      And verify that JMS message with content 'post' was received from "queue" "http-post"

  @integration-http-consumer-put
  Scenario: PUT to AMQ
    Given create HTTP "PUT" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-put"
    When create integration with name: "HTTP-PUT-AMQ"
    Then wait for integration with name: "HTTP-PUT-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "PUT" was executed
      And verify that JMS message with content 'put' was received from "queue" "http-put"

  @integration-http-consumer-delete
  Scenario: DELETE to AMQ
    Given create HTTP "DELETE" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-delete"
    When create integration with name: "HTTP-DELETE-AMQ"
    Then wait for integration with name: "HTTP-DELETE-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "DELETE" was executed
      And verify that JMS message with content 'delete' was received from "queue" "http-delete"

  @integration-http-consumer-patch
  Scenario: PATCH to AMQ
    Given create HTTP "PATCH" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-patch"
    When create integration with name: "HTTP-PATCH-AMQ"
    Then wait for integration with name: "HTTP-PATCH-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "PATCH" was executed
      And verify that JMS message with content 'patch' was received from "queue" "http-patch"

  @integration-http-consumer-options
  Scenario: OPTIONS to AMQ
    Given create HTTP "OPTIONS" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-options"
    When create integration with name: "HTTP-OPTIONS-AMQ"
    Then wait for integration with name: "HTTP-OPTIONS-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "OPTIONS" was executed
      And verify that JMS message with content 'options' was received from "queue" "http-options"

  @integration-http-consumer-trace
  Scenario: TRACE to AMQ
    Given create HTTP "TRACE" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-trace"
    When create integration with name: "HTTP-TRACE-AMQ"
    Then wait for integration with name: "HTTP-TRACE-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "TRACE" was executed

  @integration-http-consumer-head
  Scenario: HEAD to AMQ
    Given create HTTP "HEAD" step with period "5" "SECONDS"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-head"
    When create integration with name: "HTTP-HEAD-AMQ"
    Then wait for integration with name: "HTTP-HEAD-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "HEAD" was executed

  @integration-http-datamapper
  @datamapper
  Scenario: HTTP Response Datamapper
    Given create HTTP "GET" step with path "/api/getXml" and period "5" "SECONDS"
      And change "out" datashape of previous step to "XML_INSTANCE" type with specification '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><xmlResponse><dummyField1>x</dummyField1><dummyField2>y</dummyField2><method>get</method><dummyField3>z</dummyField3></xmlResponse>'
      And start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/xmlResponse/method" to "/response/executedMethod"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "http-datamapper"
      And change "in" datashape of previous step to "XML_INSTANCE" type with specification '<response><executedMethod>TEST</executedMethod></response>'
    When create integration with name: "HTTP-GET-AMQ-Datamapper"
    Then wait for integration with name: "HTTP-GET-AMQ-Datamapper" to become active
    When clear endpoint events
    Then verify that endpoint "GET" was executed
      And verify that JMS message with content '<?xml version="1.0" encoding="UTF-8" standalone="no"?><response><executedMethod>get</executedMethod></response>' was received from "queue" "http-datamapper"
