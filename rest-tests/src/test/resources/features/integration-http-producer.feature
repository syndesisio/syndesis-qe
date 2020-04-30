# @sustainer: avano@redhat.com

@rest
@integration-http
@integration-http-producer
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
      And create HTTPS connection

  @integration-http-producer-get
  Scenario Outline: AMQ to GET
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-get-input"
      And create <protocol> "GET" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-get-output"
    When create integration with name: "AMQ-<protocol>-GET-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-GET-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "<protocol>-producer-get-input"
    Then verify that endpoint "GET" was executed once
      And verify that JMS message with content 'get' was received from "queue" "<protocol>-producer-get-output"
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-post
  Scenario Outline: AMQ to POST
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-post-input"
      And create <protocol> "POST" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-post-output"
    When create integration with name: "AMQ-<protocol>-POST-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-POST-AMQ" to become active
    When clear endpoint events
      And publish message with content "postbody" to "queue" with name "<protocol>-producer-post-input"
    Then verify that endpoint "POST" was executed once
      And verify that JMS message with content 'postbody' was received from "queue" "<protocol>-producer-post-output"
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-put
  Scenario Outline: AMQ to PUT
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-put-input"
      And create <protocol> "PUT" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-put-output"
    When create integration with name: "AMQ-<protocol>-PUT-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-PUT-AMQ" to become active
    When clear endpoint events
      And publish message with content "putbody" to "queue" with name "<protocol>-producer-put-input"
    Then verify that endpoint "PUT" was executed once
      And verify that JMS message with content 'putbody' was received from "queue" "<protocol>-producer-put-output"
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-delete
  Scenario Outline: AMQ to DELETE
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-delete-input1"
      And create <protocol> "DELETE" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-delete-output1"
    When create integration with name: "AMQ-<protocol>-DELETE-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-DELETE-AMQ" to become active
    When clear endpoint events
      # DELETE may have body, but it shouldn't be used
      And publish message with content "" to "queue" with name "<protocol>-producer-delete-input1"
    Then verify that endpoint "DELETE" was executed once
      And verify that JMS message with content 'delete' was received from "queue" "<protocol>-producer-delete-output1"
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-patch
  Scenario Outline: AMQ to PATCH
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-patch-input"
      And create <protocol> "PATCH" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-patch-output"
    When create integration with name: "AMQ-<protocol>-PATCH-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-PATCH-AMQ" to become active
    When clear endpoint events
      And publish message with content "patchbody" to "queue" with name "<protocol>-producer-patch-input"
    Then verify that endpoint "PATCH" was executed once
      And verify that JMS message with content 'patchbody' was received from "queue" "<protocol>-producer-patch-output"
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-options
  Scenario Outline: AMQ to OPTIONS
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-options-input"
      And create <protocol> "OPTIONS" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-options-output"
    When create integration with name: "AMQ-<protocol>-OPTIONS-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-OPTIONS-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "<protocol>-producer-options-input"
    Then verify that endpoint "OPTIONS" was executed once
      And verify that JMS message with content 'options' was received from "queue" "<protocol>-producer-options-output"
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-trace
  Scenario Outline: AMQ to TRACE
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-trace-input"
      And create <protocol> "POST" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-trace-output"
    When create integration with name: "AMQ-<protocol>-TRACE-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-TRACE-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "<protocol>-producer-trace-input"
    Then verify that endpoint "TRACE" was executed once
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @integration-http-producer-head
  Scenario Outline: AMQ to HEAD
    Given create ActiveMQ "subscribe" action step with destination type "queue" and destination name "<protocol>-producer-head-input"
      And create <protocol> "HEAD" step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "<protocol>-producer-head-output"
    When create integration with name: "AMQ-<protocol>-HEAD-AMQ"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "AMQ-<protocol>-HEAD-AMQ" to become active
    When clear endpoint events
      And publish message with content "" to "queue" with name "<protocol>-producer-head-input"
    Then verify that endpoint "HEAD" was executed once
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |

  @gh-5093
  @integration-http-sql-split
  Scenario Outline: <protocol> to SQL with split
    Given clean "TODO" table
    When create <protocol> "GET" step with path "/api/getJsonArray" and period "1" "MINUTES"
      And change "out" datashape of previous step to "JSON_INSTANCE" type with specification '[{"key":"value"}]'
      And add a split step
      And start mapper definition with name: "mapping"
      And MAP using Step 2 and field "/key" to "/<>/task"
      And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 0)"
      And create integration with name: "<protocol>-SQL-SPLIT"
      And configure keystore in <protocol> integration dc
    Then wait for integration with name: "<protocol>-SQL-SPLIT" to become active
      And check rows number of table "TODO" is greater than 9
    Examples:
      | protocol |
      | HTTP     |
      | HTTPS    |
