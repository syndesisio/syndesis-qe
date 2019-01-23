# @sustainer: mcada@redhat.com

@ui
@openwire
@activemq
@integrations-openwire-to-openwire
Feature: Integration - Openwire to Openwire

  Background:
    Given clean application state
    Given deploy AMQ broker and add accounts
    Given log into the Syndesis
    Given created connections
      | AMQ Message Broker | AMQ | AMQ | AMQ connection is awesome |

  Scenario: Publish subscribe on queue
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor

    # select connection as 'start' point
    And check that position of connection to fill is "Start"
    When select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values
      | Destination Name | cheese |
      | Destination Type | Queue  |
    And click on the "Next" button
    # skip custom DataShape definition
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # select connection as 'finish' point
    When select the "AMQ" connection
    And select "Publish Messages" integration action
    And fill in values
      | Destination Name | apple |
      | Destination Type | Queue |
    And click on the "Next" button
    # skip custom DataShape definition
    And click on the "Done" button

    # final steps
    When click on the "Publish" button
    And set integration name "JMS publish-subscribe-request E2E"
    And click on the "Publish" button
    Then wait until integration "JMS publish-subscribe-request E2E" gets into "Running" state

    And verify that JMS message using "openwire" protocol, published on "queue" named "cheese" has arrived to "queue" named "apple" consumer
