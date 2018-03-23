@integrations-openwire-to-openwire
Feature: Integration - Openwire to Openwire

  Background:
    Given clean application state
    Given deploy AMQ broker and add accounts
    Given "Camilla" logs into the Syndesis
    Given created connections
      | AMQ | AMQ | AMQ | AMQ connection is awesome |

  Scenario: Publish Subscribe - Queue
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor

    # select connection as 'start' point
    And she is prompted to select a "Start" connection from a list of available connections
    When Camilla selects the "AMQ" connection
    And she selects "Subscribe for Messages" integration action
    And sets jms subscribe inputs source data
      | destinationName | destinationType |
      | cheese          | Queue           |
    And clicks on the "Next" button
    # skip custom DataShape definition
    And clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select connection as 'finish' point
    When Camilla selects the "AMQ" connection
    And she selects "Publish Messages" integration action
    And sets jms publish inputs source data
      | destinationName | destinationType | persistent |
      | apple           | Queue           | true       |
    And click on the "Next" button
    # skip custom DataShape definition
    And click on the "Done" button

    # final steps
    When clicks on the "Publish" button
    And she sets the integration name "JMS publish-subscribe-request E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "JMS publish-subscribe-request E2E" integration details
    Then "Camilla" navigates to the "Integrations" page
    Then she waits until integration "JMS publish-subscribe-request E2E" gets into "Published" state

    Then verify that JMS message using "openwire" protocol, published on "queue" named "cheese" has arrived to "queue" named "apple" consumer
