# @sustainer: avano@redhat.com

@box
@file-transfer
Feature: Integration - File transfer

  Background: Prepare
    Given clean application state
      And deploy ActiveMQ broker
      And create Box connection
      And create ActiveMQ connection
      And remove all files from Box

  @activemq
  @integration-box-amq
  Scenario: Box download to AMQ
    Given upload file with name "syndesis-integration.txt" and content "Hello integration!" to Box
    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 1000   |
      And create Box download action step
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "box-out"
    When create integration with name: "BOX-AMQ"
    Then wait for integration with name: "BOX-AMQ" to become active
      And verify that JMS message with content 'Hello integration!' was received from "queue" "box-out"
