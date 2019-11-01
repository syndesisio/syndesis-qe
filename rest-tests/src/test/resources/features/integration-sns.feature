# @sustainer: avano@redhat.com

@rest
@integration-sns
@sns
Feature: Integration - SNS
  Background:
    Given clean application state
      And purge SQS queues:
        | syndesis-sns-out |
      And create SNS connection
      And deploy ActiveMQ broker
      And create ActiveMQ connection
      And clean destination type "queue" with name "amq-sns-in"

  @amqbroker
  @activemq
  @datamapper
  @integration-amq-sns
  Scenario Outline: AMQ to SNS <topicName>
    When create ActiveMQ "subscribe" action step with destination type "queue" and destination name "amq-sns-in"
    And change "out" datashape of previous step to "JSON_INSTANCE" type with specification '{"header":"a", "text":"b"}'
      And start mapper definition with name: "amq-sns"
      And MAP using Step 1 and field "/header" to "/subject"
      And MAP using Step 1 and field "/text" to "/message"
      And create SNS publish action step with topic "<topicName>"
      And create integration with name: "AMQ-SNS"
    Then wait for integration with name: "AMQ-SNS" to become active
    When publish message with content '{"header":"Hello", "text":"First message!"}' to queue "amq-sns-in"
      And publish message with content '{"header":"Hi", "text":"Second message."}' to queue "amq-sns-in"
    Then verify that the SQS queue "syndesis-sns-out" has 2 messages after 10 seconds
      And verify that the SQS queue "syndesis-sns-out" contains notifications related to
        | Hello | First message!  |
        | Hi    | Second message. |

    Examples:
      | topicName        |
      | syndesis-out     |
      | arn:syndesis-out |
