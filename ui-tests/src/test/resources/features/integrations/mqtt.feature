@integrations-mqtt
Feature: Integration - MQTT to MQTT

  Background: Clean application state and prepare what is needed
    Given clean application state
    Given deploy AMQ broker and add accounts
    Given log into the Syndesis
    Given created connections
      | MQTT Message Broker | QE MQTT | MQTT test connection | some description |


#
#  1. publish-subscribe-request
#
  @mqtt-connection-publish-subscribe-request
  Scenario: Publish subscribe on topic
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor

    # select connection as 'start' point
    And check that position of connection to fill is "Start"
    When select the "MQTT test connection" connection

    And select "Subscribe" integration action
    And fill in "topic" action configure component input with "news" value

    And click on the "Next" button
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # select connection as 'finish' point
    When select the "MQTT test connection" connection
    And select "Publish" integration action
    And fill in "topic" action configure component input with "olds" value

    And click on the "Next" button
    And click on the "Done" button

    # final steps
    When click on the "Publish" button
    And set integration name "MQTT publish-subscribe-request E2E"
    And click on the "Publish" button
    Then check visibility of "MQTT publish-subscribe-request E2E" integration details
    Then navigate to the "Integrations" page
    Then wait until integration "MQTT publish-subscribe-request E2E" gets into "Published" state

    Then verify that when message is sent to "news" topic it is redirected to "olds" topic via integration
