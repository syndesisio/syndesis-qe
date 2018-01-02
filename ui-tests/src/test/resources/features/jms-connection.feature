@tp3
@jms-connection
Feature: Test functionality of DB connection

  @jms-connection-clean-application-state
  Scenario: Clean application state
    Given "Camilla" logs into the Syndesis
    Given clean application state
    Given deploy AMQ broker
    And she fills "AMQ" connection details

  Scenario: Create amq connection
    When "Camilla" navigates to the "Connections" page
    And clicks on the "Create Connection" button
    And Camilla selects the "ActiveMQ" connection
  # fill salesforce connection details:
    Then Camilla is presented with the Syndesis page "ActiveMQ Configuration"
    Then she is presented with the "Validate" button
    And she fills "Active MQ" connection details
    Then click on the "Validate" button
    Then she can see "Active MQ has been successfully validated" in alert-success notification
    And click on the "Next" button
    Then Camilla is presented with the Syndesis page "Add Connection Details"
    And type "Active MQ" into connection name
    And type "Active MQ connection is awesome" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"

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
    When Camilla selects the "Active MQ" connection
    Then Camilla is presented with the Syndesis page "Active MQ - Choose an Action"
    And she selects "Subscribe for Messages" integration action
    And sets jms subscribe inputs source data
      | destinationName | destinationType | durable | destinationSubscriptionId | messageSelector |
      | cheese          | Topic           | true    | exampleSubscriber         | exampleSelector |
    And clicks on the "Done" button

    # select connection as 'finish' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "Active MQ" connection
    And she selects "Publish Messages" integration action
    Then Camilla is presented with the Syndesis page "Publish Messages"
    And sets jms publish inputs source data
      | destinationName | destinationType | persistent |
      | apple           | Topic           | true       |
    And clicks on the "Done" button

    # select connection as 'step' point
    Then Camilla is presented with the Syndesis page "Add to Integration"
    When Camilla clicks on the "Add a Connection" button
    Then Camilla selects the "Active MQ" connection
    And she selects "Request response using Messages" integration action
    Then Camilla is presented with the Syndesis page "Request response using Messages"
    And sets jms request inputs source data
      | destinationName | destinationType | messageSelector  | namedReplyTo        | persistent | responseTimeOut |
      | decorate.cheese | Topic           | exampleSelector2 | exampleNamedReplyTo | true       | 5000            |
    And clicks on the "Done" button

    # final steps
    Then Camilla is presented with the Syndesis page "Add to Integration"
    And clicks on the "Publish" button
    And she defines integration name "JMS publish-subscribe-request E2E"
    And clicks on the "Publish" button
    #@wip there is no more h1 label with integration name there, syndesis #430
    Then Camilla is presented with "JMS publish-subscribe-request E2E" integration details
    And she clicks on the "Done" button
    Then she wait until integration "JMS publish-subscribe-request E2E" get into "Active" state
