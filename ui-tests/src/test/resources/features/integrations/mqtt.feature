@integrations-mqtt
Feature: Integration - MQTT to MQTT

  Background: Clean application state and prepare what is needed
    Given clean application state
    Given deploy AMQ broker and add accounts
    Given "Camilla" logs into the Syndesis
    Given created connections
      | MQTT | QE MQTT | MQTT test connection | some description |


#
#  1. publish-subscribe-request
#
  @mqtt-connection-publish-subscribe-request
  Scenario: Publish subscribe on topic
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor

    # select connection as 'start' point
    And she is prompted to select a "Start" connection from a list of available connections
    When Camilla selects the "MQTT test connection" connection

    And she selects "Subscribe" integration action
    And she fills "topic" action configure component input with "news" value

    And clicks on the "Next" button
    And clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select connection as 'finish' point
    When Camilla selects the "MQTT test connection" connection
    And she selects "Publish" integration action
    And she fills "topic" action configure component input with "olds" value

    And clicks on the "Next" button
    And clicks on the "Done" button

    # final steps
    When clicks on the "Publish" button
    And she sets the integration name "MQTT publish-subscribe-request E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "MQTT publish-subscribe-request E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "MQTT publish-subscribe-request E2E" gets into "Published" state

    Then verify that when message is sent to "news" topic it is redirected to "olds" topic via integration

