@tp3
@jms-connection
Feature: Test functionality of DB connection

  @jms-connection-clean-application-state
  Scenario: Clean application state
    Given "Camilla" logs into the Syndesis
    Given clean application state
    Given deploy AMQ broker
    Given created connections
      | AMQ | AMQ | AMQ | AMQ connection is awesome |

#
#  1. publish-subscribe-request
#
  @wip
  @jms-connection-publish-subscribe-request
  Scenario: Create integration to test JMS connector
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor

    # select connection as 'start' point
    And she is prompted to select a "Start" connection from a list of available connections
    When Camilla selects the "AMQ" connection
    And she selects "Subscribe for Messages" integration action
    And sets jms subscribe inputs source data
      | destinationName | destinationType | durableSubscriptionId | messageSelector |
      | cheese          | Topic           | exampleSubscriber     | exampleSelector |
    And clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections

    # select connection as 'finish' point
    When Camilla selects the "AMQ" connection
    And she selects "Publish Messages" integration action
    And sets jms publish inputs source data
      | destinationName | destinationType | persistent |
      | apple           | Topic           | true       |
    And clicks on the "Done" button

    # select connection as 'step' point
#    Then Camilla is presented with the Syndesis page "Add to Integration"
#    When Camilla clicks on the "Add a Connection" button
#    Then Camilla selects the "MQ" connection
#    And she selects "Request response using Messages" integration action
#    Then Camilla is presented with the Syndesis page "Request response using Messages"
#    And sets jms request inputs source data
#      | destinationName | destinationType | messageSelector  | namedReplyTo        | persistent | responseTimeOut |
#      | decorate.cheese | Topic           | exampleSelector2 | exampleNamedReplyTo | true       | 5000            |
#    And clicks on the "Done" button

    # final steps
    When clicks on the "Publish" button
    And she defines integration name "JMS publish-subscribe-request E2E"
    And clicks on the "Publish" button
    Then Camilla is presented with "JMS publish-subscribe-request E2E" integration details
    And she clicks on the "Done" button
    Then she wait until integration "JMS publish-subscribe-request E2E" get into "Active" state
