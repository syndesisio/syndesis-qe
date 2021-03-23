# @sustainer: mkralik@redhat.com

@ui
@doc-tutorial
@activemq
@amqbroker
@todo-app
@extension
@api-connector
@datamapper
@integrations-amq-to-rest
Feature: Integration - AMQ to REST

  Background: Clean application state and prepare what is needed
    Given log into the Syndesis
    And clean application state
    Then wait for Todo to become ready

    When reset content of "todo" table
    And Set Todo app credentials
    Then deploy ActiveMQ broker

    When import extensions
      | sample-damage-reporter-extension-1.4.8.jar | ./src/test/resources/extensions/ |

    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload TODO API swagger from URL

    When click on the "Next" button
    Then check visibility of page "Review Actions"

    When click on the "Next" link
    Then check visibility of page "Specify Security"
    #fill in dummy credentials
    And fill in values by element ID
      | username | dummy |
      | password | dummy |

    And click on the "Next" button
    And fill in values by element data-testid
      | name     | Todo connector |
      | basepath | /api           |
    And fill in TODO API host URL
    And click on the "Save" button

    And created connections
      | Red Hat AMQ    | AMQ_PROD | AMQ             | AMQ on OpenShift |
      | Todo connector | todo     | TODO connection | no validation    |

  @integrations-amq-to-rest-xml-message
  Scenario: Publish subscribe on amq topic
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor

    # start point
    And check that position of connection to fill is "Start"
    When select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | inventoryReceived |
      | destinationtype | Queue             |
    And click on the "Next" button
    And click on the "Next" button

    # finish point
    Then check that position of connection to fill is "Finish"
    Then select the "TODO connection" connection
    And select "Create new task" integration action
    And click on the "Next" button

    # add custom step
    # Then check visibility of page "Add to Integration"
    When add integration step on position "0"
    And select "Damage Reporter" integration step
    And click on the "Next" button

    # add data mapper
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | task | body.task |
    And click on the "Done" button

    Then publish integration
    And set integration name "AMQ to TODO integration"
    Then publish integration

    When navigate to the "Integrations" page
    Then wait until integration "AMQ to TODO integration" gets into "Running" state

    When navigate to Todo app
    And publish JMS message on Todo app page from resource "tutorialAmqToRestMessage1.xml"
    Then check Todo list has "1" items
    And check that "1". task on Todo app page contains text "Task: Contact Joe Doe, 987 654 321. Damaged items: ABC789."

    When publish JMS message on Todo app page from resource "tutorialAmqToRestMessage2.xml"
    Then check Todo list has "2" items
    And check that "1". task on Todo app page contains text "Task: Contact Joe Doe, 987 654 321. Damaged items: ABC789."
    And check that "2". task on Todo app page contains text "Task: Contact John Smith, 123 456 789. Damaged items: XYZ123. Contact Joe Doe, 987 654 321. Damaged items: ABC789."

    When publish JMS message on Todo app page from resource "tutorialAmqToRestMessage3.xml"
    Then check Todo list has "3" items
    And check that "1". task on Todo app page contains text "Task: Contact Joe Doe, 987 654 321. Damaged items: ABC789."
    And check that "2". task on Todo app page contains text "Task: Contact John Smith, 123 456 789. Damaged items: XYZ123. Contact Joe Doe, 987 654 321. Damaged items: ABC789."
    And check that "3". task on Todo app page contains text "Task: No contact found. Damaged items: ABC789."