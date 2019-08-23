# @sustainer: avano@redhat.com

@box
@file-transfer
Feature: Integration - File transfer

  Background: Prepare
    Given clean application state
      And deploy ActiveMQ broker
      And clean destination type "queue" with name "box-out"
      And create Box connection
      And create ActiveMQ connection
      And remove all files from Box

  @activemq
  @integration-box-amq
  Scenario: Box download to AMQ
    Given upload file with name "syndesis-integration.txt" and content "Hello integration!" to Box
    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 10000  |
      And create Box download action step
      And start mapper definition with name: "box-amq"
      And COMBINE using Step 2 and strategy "Dash" into "/text" and sources
        | /content | /id | /size |
      And create ActiveMQ "publish" action step with destination type "queue" and destination name "box-out"
      And change "in" datashape of previous step to "JSON_INSTANCE" type with specification '{"text":"a"}'
    When create integration with name: "BOX-AMQ"
    Then wait for integration with name: "BOX-AMQ" to become active
      And verify the Box AMQ response from queue "box-out" with text "Hello integration!"
