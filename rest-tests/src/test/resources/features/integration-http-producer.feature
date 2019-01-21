# @sustainer: avano@redhat.com

@rest
@integration-http
@integration-http-producer
@http
@activemq
Feature: Integration - HTTP producer
  Background:
    Given clean application state
      And deploy HTTP endpoints
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And create HTTP connection

  @integration-http-producer-get
  Scenario: Integration - HTTP producer GET
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-get-input"
      And create HTTP "GET" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-get-output"
    When create integration with name: "AMQ-HTTP-GET-AMQ"
    Then wait for integration with name: "AMQ-HTTP-GET-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "http-producer-get-input"
    Then verify that endpoint "GET" was executed once
      And verify that JMS message with content 'get' was received from "queue" "http-producer-get-output"

  @integration-http-producer-post
  Scenario: Integration - HTTP producer POST
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-post-input"
      And create HTTP "POST" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-post-output"
    When create integration with name: "AMQ-HTTP-POST-AMQ"
    Then wait for integration with name: "AMQ-HTTP-POST-AMQ" to become active
    When clear endpoint events
      And publish message with content "postbody" to "queue" with name "http-producer-post-input"
    Then verify that endpoint "POST" was executed once
      And verify that JMS message with content 'postbody' was received from "queue" "http-producer-post-output"

  @integration-http-producer-put
  Scenario: Integration - HTTP producer PUT
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-put-input"
      And create HTTP "PUT" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-put-output"
    When create integration with name: "AMQ-HTTP-PUT-AMQ"
    Then wait for integration with name: "AMQ-HTTP-PUT-AMQ" to become active
    When clear endpoint events
      And publish message with content "putbody" to "queue" with name "http-producer-put-input"
    Then verify that endpoint "PUT" was executed once
      And verify that JMS message with content 'putbody' was received from "queue" "http-producer-put-output"

  @integration-http-producer-delete
  Scenario: Integration - HTTP producer DELETE
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-delete-input1"
      And create HTTP "DELETE" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-delete-output1"
    When create integration with name: "AMQ-HTTP-DELETE-AMQ"
    Then wait for integration with name: "AMQ-HTTP-DELETE-AMQ" to become active
    When clear endpoint events
      # DELETE may have body, but it shouldn't be used
      And publish message with content "" to "queue" with name "http-producer-delete-input1"
    Then verify that endpoint "DELETE" was executed once
      And verify that JMS message with content 'delete' was received from "queue" "http-producer-delete-output1"

  @integration-http-producer-patch
  Scenario: Integration - HTTP producer PATCH
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-patch-input"
      And create HTTP "PATCH" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-patch-output"
    When create integration with name: "AMQ-HTTP-PATCH-AMQ"
    Then wait for integration with name: "AMQ-HTTP-PATCH-AMQ" to become active
    When clear endpoint events
      And publish message with content "patchbody" to "queue" with name "http-producer-patch-input"
    Then verify that endpoint "PATCH" was executed once
      And verify that JMS message with content 'patchbody' was received from "queue" "http-producer-patch-output"

  @integration-http-producer-options
  Scenario: Integration - HTTP producer OPTIONS
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-options-input"
      And create HTTP "OPTIONS" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-options-output"
    When create integration with name: "AMQ-HTTP-OPTIONS-AMQ"
    Then wait for integration with name: "AMQ-HTTP-OPTIONS-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "http-producer-options-input"
    Then verify that endpoint "OPTIONS" was executed once
      And verify that JMS message with content 'options' was received from "queue" "http-producer-options-output"

  @integration-http-producer-trace
  Scenario: Integration - HTTP producer TRACE
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-trace-input"
      And create HTTP "POST" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-trace-output"
    When create integration with name: "AMQ-HTTP-TRACE-AMQ"
    Then wait for integration with name: "AMQ-HTTP-TRACE-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "http-producer-trace-input"
    Then verify that endpoint "TRACE" was executed once

  @integration-http-producer-head
  Scenario: Integration - HTTP producer HEAD
    Given create AMQ "subscribe" action step with destination type "queue" and destination name "http-producer-head-input"
      And create HTTP "HEAD" step
      And create AMQ "publish" action step with destination type "queue" and destination name "http-producer-head-output"
    When create integration with name: "AMQ-HTTP-HEAD-AMQ"
    Then wait for integration with name: "AMQ-HTTP-HEAD-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "http-producer-head-input"
    Then verify that endpoint "HEAD" was executed once
