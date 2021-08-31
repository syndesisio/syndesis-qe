# @sustainer: avano@redhat.com

@rest
@integration-sqs
@sqs
@long-running
Feature: Integration - SQS
  Background:
    Given clean application state
      And purge SQS queues:
        | syndesis-in                     |
        | syndesis-out                    |
        | syndesis-in.fifo                |
        | syndesis-out.fifo               |
        | syndesis-out-content-based.fifo |
      And remove all records from table "CONTACT"
      And create SQS connection

  ############
  # Consumer #
  ############
  @integration-sqs-sqs-arn
  Scenario: SQS to SQS ARN
    When create SQS "polling" action step with properties
      | queueNameOrArn | arn:syndesis-in |
      And create SQS "send-object" action step with properties
      | queueNameOrArn | arn:syndesis-out |
      And create integration with name: "SQS-SQS-arn"
    Then wait for integration with name: "SQS-SQS-arn" to become active
    When send SQS messages to "syndesis-in" with content
      | Message 1 |
      | Message 2 |
      | Message 3 |
      | Message 4 |
    Then verify that the SQS queue "syndesis-out" has 4 messages after 30 seconds

  @integration-sqs-sqs-delete-after-read
  Scenario: SQS to SQS delete after read
    When create SQS "polling" action step with properties
      | queueNameOrArn  | syndesis-in |
      | deleteAfterRead | true        |
      And create SQS "send-object" action step with properties
        | queueNameOrArn | syndesis-out |
      And create integration with name: "SQS-SQS-delete-after-read"
    Then wait for integration with name: "SQS-SQS-delete-after-read" to become active
    When send SQS messages to "syndesis-in" with content
      | Message 1 |
      | Message 2 |
      | Message 3 |
      | Message 4 |
    Then verify that the SQS queue "syndesis-out" has 4 messages after 60 seconds
      And verify that the SQS queue "syndesis-in" has 0 messages after 0 seconds

  @amqbroker
  @activemq
  @integration-sqs-amq-dont-delete-after-read
  Scenario: SQS to AMQ don't delete after read
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-out"
    # Publish the message so that it will be processed right at the start of the integration
    When send SQS messages to "syndesis-in" with content
      | Message 1 |
      And create SQS "polling" action step with properties
        | queueNameOrArn  | syndesis-in |
        | delay           | 600000      |
        | deleteAfterRead | false       |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sqs-out"
      And create integration with name: "SQS-AMQ-dont-delete-after-read"
    Then wait for integration with name: "SQS-AMQ-dont-delete-after-read" to become active
      And wait until integration SQS-AMQ-dont-delete-after-read processed at least 1 messages
      And verify that 1 message was received from JMS queue "sqs-out"
      And verify that the SQS queue "syndesis-in" has 1 messages after 0 seconds

  @amqbroker
  @activemq
  @integration-sqs-sqs-delete-when-filtered
  Scenario: SQS to AMQ delete when filtered out
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-out"
    When create SQS "polling" action step with properties
      | queueNameOrArn   | syndesis-in |
      | deleteAfterRead  | false       |
      | deleteIfFiltered | true        |
      And create basic filter step for "message" with word "ok" and operation "contains"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sqs-out"
      And create integration with name: "SQS-SQS-delete-when-filtered"
    Then wait for integration with name: "SQS-SQS-delete-when-filtered" to become active
    When send SQS messages to "syndesis-in" with content
      | this should be ok |
    Then verify that 1 message was received from JMS queue "sqs-out"
      And verify that the SQS queue "syndesis-in" has 0 messages after 0 seconds

  @amqbroker
  @activemq
  @integration-sqs-sqs-dont-delete-when-filtered
  Scenario: SQS to AMQ don't delete when filtered out
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-out"
    # Publish the message so that it will be processed right at the start of the integration
    When send SQS messages to "syndesis-in" with content
      | filter me out |
    When create SQS "polling" action step with properties
      | queueNameOrArn   | syndesis-in |
      | delay            | 600000      |
      | deleteAfterRead  | false       |
      | deleteIfFiltered | false       |
      And create basic filter step for "message" with word "ok" and operation "contains"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sqs-out"
      And create integration with name: "SQS-SQS-dont-delete-when-filtered"
    Then wait for integration with name: "SQS-SQS-dont-delete-when-filtered" to become active
      And verify that the JMS queue "sqs-out" is empty
      And verify that the SQS queue "syndesis-in" has 1 messages after 0 seconds

  @integration-sqs-sqs-max-objects
  Scenario: SQS to SQS Max objects to poll
    When create SQS "polling" action step with properties
      | queueNameOrArn     | syndesis-in |
      | maxMessagesPerPoll | 1           |
      | delay              | 30000       |
      And create SQS "send-object" action step with properties
        | queueNameOrArn | syndesis-out |
      And create integration with name: "SQS-SQS-max-objects"
    Then wait for integration with name: "SQS-SQS-max-objects" to become active
    When send SQS messages to "syndesis-in" with content
      | Calibrate timeout |
    Then wait until the message appears in SQS queue "syndesis-out"
    When purge SQS queues:
      | syndesis-in  |
      | syndesis-out |
      And send SQS messages to "syndesis-in" with content
      | Message 1 |
      | Message 2 |
      | Message 3 |
      | Message 4 |
      | Message 5 |
    Then verify that the SQS queue "syndesis-out" has max 1 message after 45 seconds
      And verify that the SQS queue "syndesis-out" has max 2 messages after 30 seconds
      And verify that the SQS queue "syndesis-out" has max 3 messages after 30 seconds

  @amqbroker
  @activemq
  @datamapper
  @integration-sqs-amq-poll-delay
  Scenario: SQS to AMQ poll delay
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-out"
    When create SQS "polling" action step with properties
      | queueNameOrArn | syndesis-in |
      | delay          | 90000       |
      And start mapper definition with name: "sqs-amq"
      And MAP using Step 1 and field "/message" to "/body"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sqs-out"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"body":"msg"}'
      And create integration with name: "SQS-AMQ-poll-delay"
    Then wait for integration with name: "SQS-AMQ-poll-delay" to become active
    When send SQS message to "syndesis-in" with content
      | Calibrate timeout |
      # This ends as soon as it receives a message
    Then verify that JMS queue "sqs-out" received a message in 300 seconds
    When send SQS message to "syndesis-in" with content
      | Hello from SQS! |
    # This waits for 60 seconds for the message, should be empty as the poll interval is 90s
    Then verify that the JMS queue "sqs-out" is empty
      # According to the sqs docs, when there are extreme low amount of messages in the queue, it may not return anything and the request should be retried
      And verify that JMS queue "sqs-out" received a message in 300 seconds

  @amqbroker
  @activemq
  @integration-amq-sqs-send-delay
  Scenario: AMQ to SQS send delay
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-in"
    When create ActiveMQ "subscribe" action step with destination type "queue" and destination name "sqs-in"
      And create SQS "send-object" action step with properties
      | queueNameOrArn | syndesis-out |
      | delaySeconds   | 45           |
      And create integration with name: "AMQ-SQS-send-delay"
    Then wait for integration with name: "AMQ-SQS-send-delay" to become active
    When publish message with content "Hello!" to "queue" with name "sqs-in"
    Then verify that the SQS queue "syndesis-out" has 0 messages after 35 seconds
      And verify that the SQS queue "syndesis-out" has 1 message after 30 seconds

  @amqbroker
  @activemq
  @datamapper
  @integration-sqs-fifo-amq
  Scenario: SQS FIFO to AMQ
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-out-fifo"
    When create SQS "polling" action step with properties
      | queueNameOrArn | syndesis-in.fifo |
      And start mapper definition with name: "sqs-fifo-amq"
      And MAP using Step 1 and field "/message" to "/body"
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "sqs-out-fifo"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"body":"asdf"}'
      And create integration with name: "SQSfifo-AMQ"
    Then wait for integration with name: "SQSfifo-AMQ" to become active
    When send 20 ordered messages to "syndesis-in.fifo"
    Then verify that 20 messages were received from AMQ "queue" "sqs-out-fifo" and are in order

  ############
  # Producer #
  ############
  @http
  @datamapper
  @integration-http-sqs-batch
  Scenario: HTTP to SQS Batch
    Given deploy HTTP endpoints
      And create HTTP connection
    When create HTTP "GET" step with path "/api/getJsonArray" and period "300" "seconds"
      And change "out" datashape of previous step to "JSON_INSTANCE" type with specification '[{"key":"value"}]'
      And add a split step
      And start mapper definition with name: "http-sqs"
      And MAP using Step 2 and field "/key" to "/<>/message"
      And add an aggregate step
      And create SQS "send-batch" action step with properties
        | queueNameOrArn | syndesis-out |
      And create integration with name: "HTTP-SQS-send-batch"
    Then wait for integration with name: "HTTP-SQS-send-batch" to become active
      And verify that the SQS queue "syndesis-out" has 10 messages after 10 seconds

  @database
  @datamapper
  @integration-db-sqs-fifo-deduplication-exchangeid
  Scenario: DB to SQS FIFO ExchangeID deduplication
    Given insert into "CONTACT" table
      | X1 | Y1 | Z1 | db |
      | X2 | Y2 | Z2 | db |
      | X3 | Y3 | Z3 | db |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 300000 ms
      And add a split step
      And start mapper definition with name: "http-sqs"
      And MAP using Step 2 and field "/first_name" to "/<>/message"
      And add an aggregate step
      And create SQS "send-batch" action step with properties
        | queueNameOrArn                           | syndesis-out.fifo |
        | messageDeduplicationIdStrategy           | useExchangeId     |
        | messageGroupIdStrategy                   | useConstant       |
      And create integration with name: "DB-SQSfifo-deduplication-exchangeid"
    Then wait for integration with name: "DB-SQSfifo-deduplication-exchangeid" to become active
      And verify that the SQS queue "syndesis-out.fifo" has 1 message after 30 seconds
      And verify that the message from SQS queue "syndesis-out.fifo" has content 'X1'

  @database
  @integration-db-sqs-fifo-deduplication-content-based
  Scenario: DB to SQS FIFO ContentBased deduplication
    Given insert into "CONTACT" table
      | X1 | Y1 | Z1 | db |
      | X1 | Y1 | Z1 | db |
      | X1 | Y1 | Z1 | db |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 60000 ms
      And add a split step
      And create SQS "send-object" action step with properties
        | queueNameOrArn                           | syndesis-out-content-based.fifo |
        | messageDeduplicationIdStrategy           | useContentBasedDeduplication    |
        | messageGroupIdStrategy                   | useConstant                     |
      And create integration with name: "DB-SQSfifo-deduplication-content-based"
    Then wait for integration with name: "DB-SQSfifo-deduplication-content-based" to become active
      And verify that the SQS queue "syndesis-out-content-based.fifo" has 1 message after 10 seconds
      And verify that the message from SQS queue "syndesis-out-content-based.fifo" has content '{"last_name":"Y1","company":"Z1","create_date":null,"first_name":"X1","lead_source":"db"}'

  @amqbroker
  @activemq
  @integration-db-sqs-fifo-groupid-constant
  Scenario: DB to SQS FIFO Constant group id
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-in"
    When create ActiveMQ "subscribe" action step with destination type "queue" and destination name "sqs-in"
      And create SQS "send-object" action step with properties
        | queueNameOrArn                           | syndesis-out.fifo |
        | messageDeduplicationIdStrategy           | useExchangeId     |
        | messageGroupIdStrategy                   | useConstant       |
      And create integration with name: "DB-SQSfifo-groupid-constant"
    Then wait for integration with name: "DB-SQSfifo-groupid-constant" to become active
    When publish message with content "message1" to "queue" with name "sqs-in"
      And publish message with content "message2" to "queue" with name "sqs-in"
    Then verify that the SQS queue "syndesis-out.fifo" has 2 message after 30 seconds
      And verify that all messages in SQS queue "syndesis-out.fifo" have groupId "CamelSingleMessageGroup"

  # This test needs an extension that will set the group id property on the route
  # The source is located at https://github.com/avano/SQSExtension
  # And if it disappears for some reason, everything what it does is:
  # public Optional<ProcessorDefinition<?>> configure(CamelContext camelContext, ProcessorDefinition<?> processorDefinition, Map<String, Object> map) {
  #     processorDefinition.setProperty("CamelAwsMessageGroupId", simple(groupId));
  # }
  @amqbroker
  @activemq
  @integration-db-sqs-fifo-groupid-property-value
  Scenario: DB to SQS FIFO property value group id
    Given deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "sqs-in"
      And import extension from path "./src/test/resources/extensions/set-sqs-group-id-extension-1.0-SNAPSHOT.jar"
    When create ActiveMQ "subscribe" action step with destination type "queue" and destination name "sqs-in"
      And add "set-sqs-group-id-extension" extension step with "setGroupId" action with properties:
        | groupId | testGroup |
      And create SQS "send-object" action step with properties
        | queueNameOrArn                           | syndesis-out.fifo |
        | messageDeduplicationIdStrategy           | useExchangeId     |
        | messageGroupIdStrategy                   | usePropertyValue  |
      And create integration with name: "DB-SQSfifo-groupid-property-value"
    Then wait for integration with name: "DB-SQSfifo-groupid-property-value" to become active
    When publish message with content "message1" to "queue" with name "sqs-in"
      And publish message with content "message2" to "queue" with name "sqs-in"
    Then verify that the SQS queue "syndesis-out.fifo" has 2 message after 30 seconds
      And verify that all messages in SQS queue "syndesis-out.fifo" have groupId "testGroup"
