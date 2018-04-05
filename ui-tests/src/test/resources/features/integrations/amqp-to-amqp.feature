@wip
@integrations-amqp-to-amqp
Feature: Integration - AMQP to AMQP

  Background: Clean application state and prepare what is needed
    Given clean application state
    Given deploy AMQ broker and add accounts
    Given "Camilla" logs into the Syndesis
    Given created connections
      | AMQP | AMQP | AMQP | AMQP on OpenShift |

#
#  1. publish-subscribe-request
#
  @amqp-connection-publish-subscribe-request
  Scenario: Publish subscribe on topic
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor

    # select connection as 'start' point
    And she is prompted to select a "Start" connection from a list of available connections
    When Camilla selects the "AMQP" connection
    And she selects "Subscribe" integration action
    And sets jms subscribe inputs source data
      | destinationName | destinationType | durableSubscriptionId | messageSelector |
      | cheese          | Topic           | exampleSubscriber     | exampleSelector |
    And clicks on the "Next" button
#    TODO(sveres) specify datatype
    And clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select connection as 'finish' point
    When Camilla selects the "AMQP" connection
    And she selects "Publish" integration action
    # TODO: there's a mismatch between persistent on ActiveMQ and deliveryPersistent property on AMQP
    And sets jms publish inputs source data
      | destinationName | destinationType | deliveryPersistent |
      | apple           | Topic           | true               |
    And clicks on the "Next" button
#    TODO(sveres) specify datatype
    And clicks on the "Done" button

    # final steps
    When clicks on the "Publish" button
    And she sets the integration name "AMQP publish-subscribe-request E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "AMQP publish-subscribe-request E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "AMQP publish-subscribe-request E2E" gets into "Published" state

    Then verify that JMS message using "openwire" protocol, published on "topic" named "cheese" has arrived to "topic" named "apple" consumer