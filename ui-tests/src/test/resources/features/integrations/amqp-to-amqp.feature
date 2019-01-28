# @sustainer: mcada@redhat.com

@ui
@amqp
@amqbroker
@integrations-amqp-to-amqp
Feature: Integration - AMQP to AMQP

  Background: Clean application state and prepare what is needed
    Given clean application state
    Given deploy ActiveMQ broker
    Given log into the Syndesis
    Given created connections
      | AMQP Message Broker | AMQP | AMQP | AMQP on OpenShift |

#
#  1. publish-subscribe-request
#
  @amqp-connection-publish-subscribe-request
  Scenario: Publish subscribe on topic
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor

    # select connection as 'start' point
    And check that position of connection to fill is "Start"
    When select the "AMQP" connection
    And select "Subscribe for Messages" integration action
    And fill in values
      | Destination Name | cheese |
      | Destination Type | Queue  |
    And click on the "Next" button
#    TODO(sveres) specify datatype
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # select connection as 'finish' point
    When select the "AMQP" connection
    And select "Publish messages" integration action
    # TODO: there's a mismatch between persistent on ActiveMQ and deliveryPersistent property on AMQP
    And fill in values
      | Destination Name | apple |
      | Destination Type | Queue |
    And click on the "Next" button
#    TODO(sveres) specify datatype
    And click on the "Done" button

    # final steps
    When click on the "Publish" button
    And set integration name "AMQP publish-subscribe-request E2E"
    And click on the "Publish" button
    Then wait until integration "AMQP publish-subscribe-request E2E" gets into "Running" state

    Then verify that JMS message using "amqp" protocol, published on "queue" named "cheese" has arrived to "queue" named "apple" consumer
