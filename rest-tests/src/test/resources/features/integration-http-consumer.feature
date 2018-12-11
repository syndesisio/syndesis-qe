# @sustainer: avano@redhat.com

@integration-http
@integration-http-consumer
Feature: Integration - HTTP consumer
  Background:
    Given clean application state
      And deploy HTTP endpoints
      And deploy AMQ broker and add accounts
      And create AMQ connection
      And create HTTP connection

  @integration-http-consumer-get
  Scenario: Integration - HTTP consumer GET
    Given create HTTP "GET" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-get"
    When create integration with name: "HTTP-GET-AMQ"
    Then wait for integration with name: "HTTP-GET-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "GET" was executed
      And verify that JMS message with content 'get' was received from "queue" "http-get"

  @integration-http-consumer-post
  Scenario: Integration - HTTP consumer POST
    Given create HTTP "POST" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-post"
    When create integration with name: "HTTP-POST-AMQ"
    Then wait for integration with name: "HTTP-POST-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "POST" was executed
      And verify that JMS message with content 'post' was received from "queue" "http-post"

  @integration-http-consumer-put
  Scenario: Integration - HTTP consumer PUT
    Given create HTTP "PUT" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-put"
    When create integration with name: "HTTP-PUT-AMQ"
    Then wait for integration with name: "HTTP-PUT-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "PUT" was executed
      And verify that JMS message with content 'put' was received from "queue" "http-put"

  @integration-http-consumer-delete
  Scenario: Integration - HTTP consumer DELETE
    Given create HTTP "DELETE" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-delete"
    When create integration with name: "HTTP-DELETE-AMQ"
    Then wait for integration with name: "HTTP-DELETE-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "DELETE" was executed
      And verify that JMS message with content 'delete' was received from "queue" "http-delete"

  @integration-http-consumer-patch
  Scenario: Integration - HTTP consumer PATCH
    Given create HTTP "PATCH" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-patch"
    When create integration with name: "HTTP-PATCH-AMQ"
    Then wait for integration with name: "HTTP-PATCH-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "PATCH" was executed
      And verify that JMS message with content 'patch' was received from "queue" "http-patch"

  @integration-http-consumer-options
  Scenario: Integration - HTTP consumer OPTIONS
    Given create HTTP "OPTIONS" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-options"
    When create integration with name: "HTTP-OPTIONS-AMQ"
    Then wait for integration with name: "HTTP-OPTIONS-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "OPTIONS" was executed
      And verify that JMS message with content 'options' was received from "queue" "http-options"

  @integration-http-consumer-trace
  Scenario: Integration - HTTP consumer TRACE
    Given create HTTP "TRACE" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-trace"
    When create integration with name: "HTTP-TRACE-AMQ"
    Then wait for integration with name: "HTTP-TRACE-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "TRACE" was executed

  @integration-http-consumer-head
  Scenario: Integration - HTTP consumer
    Given create HTTP "HEAD" step with period "5" "SECONDS"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-head"
    When create integration with name: "HTTP-HEAD-AMQ"
    Then wait for integration with name: "HTTP-HEAD-AMQ" to become active
    When clear endpoint events
    Then verify that endpoint "HEAD" was executed

  @integration-http-datamapper
  Scenario: HTTP Response Datamapper
    Given create HTTP step with datashape
      And start mapper definition with name: "mapping 1"
      And MAP using Step 1 and field "/xmlResponse/method" to "/response/executedMethod"
      And create AMQ "publish" action step with destination type "queue" and destination name "http-datamapper" with datashape type "XML_INSTANCE" and specification '<response><executedMethod>TEST</executedMethod></response>'
    When create integration with name: "HTTP-GET-AMQ-Datamapper"
    Then wait for integration with name: "HTTP-GET-AMQ-Datamapper" to become active
    When clear endpoint events
    Then verify that endpoint "GET" was executed
      And verify that JMS message with content '<?xml version="1.0" encoding="UTF-8" standalone="no"?><response><executedMethod>get</executedMethod></response>' was received from "queue" "http-datamapper"
