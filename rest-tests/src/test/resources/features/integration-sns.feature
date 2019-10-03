# @sustainer: avano@redhat.com

@rest
@integration-sns
@sns
Feature: Integration - SNS
  Background:
    Given clean application state
      And purge SQS queues:
        | syndesis-sns-out |
      And remove all records from table "CONTACT"
      And create SNS connection

  @database
  @datamapper
  @integration-db-sns
  Scenario Outline: DB to SNS <topicName>
    Given inserts into "CONTACT" table
      | John | Doe | Red Hat | db |
      | Pete | Moe | IBM     | db |
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "600000" ms
      And add a split step
      And start mapper definition with name: "db-sns"
      And MAP using Step 2 and field "/company" to "/subject"
      And MAP using Step 2 and field "/last_name" to "/message"
      And create SNS publish action step with topic "<topicName>"
      And create integration with name: "DB-SNS"
    Then wait for integration with name: "DB-SNS" to become active
      And verify that the SQS queue "syndesis-sns-out" has 2 messages after 10 seconds
      And verify that the SQS queue "syndesis-sns-out" contains notifications related to
        | Doe | Red Hat |
        | Moe | IBM     |

    Examples:
      | topicName        |
      | syndesis-out     |
      | arn:syndesis-out |
