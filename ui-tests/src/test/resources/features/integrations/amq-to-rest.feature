@integrations-amq-to-rest
Feature: Integration - AMQ to REST

  Background: Clean application state and prepare what is needed
    Given log into the Syndesis
    And clean application state
    And reset content of "todo" table
    And Set Todo app credentials
    And deploy AMQ broker and add accounts
    And navigate to the "Customizations" page
    And click on the "Extensions" link
    And click on the "Import Extension" button
    And upload extension with name "sample-damage-reporter-extension-1.4.8.jar" from relative path "./src/test/resources/extensions/"
    And click on the "Import Extension" button
    # Same as 'create new API connector' but URL for t odo app is set in the code according to namespace and host
    And create new TODO API connector via URL
      | security | authType      | HTTP Basic Authentication |
      | details  | connectorName | Todo connector            |
      | details  | routeHost     | todo                      |
      | details  | baseUrl       | /api                      |
    And created connections
      | AMQ Message Broker | AMQ               | AMQ             | AMQ on OpenShift |
      | Todo connector     | todo              | TODO connection | no validation    |

  Scenario: Publish subscribe on topic
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor

    # start point
    And check that position of connection to fill is "Start"
    When select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values
      | Destination Name | inventoryReceived |
      | Destination Type | Queue  |
    And click on the "Next" button
    And click on the "Done" button

    # finish point
    Then check that position of connection to fill is "Finish"
    Then select the "TODO connection" connection
    And select "Create new task" integration action

    # add custom step
    Then check visibility of page "Add to Integration"
    When click on the "Add a Step" button
    And select "Damage Reporter" integration step

    # add data mapper
    When click on the "Add a Step" button
    Then check visibility of the "Add a step" link
    And clicks on the "2". "Add a step" link
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    And create data mapper mappings
      | task | body.task |
    And click on the "Done" button

    Then click on the "Publish" button
    And set integration name "AMQ to TODO integration"
    Then click on the "Publish" button

    When navigate to the "Integrations" page
    Then wait until integration "AMQ to TODO integration" gets into "Running" state

    # [WIP] - workaround for now due to https://github.com/syndesisio/todo-example/issues/16
    #  test should go to the Todo app and use JMS form instead of code (for real quickstart testing)
    #  e.g. When navigate to Todo app ; Then check Todo list grows in "15" second

    And publish JMS message from resource "tutorialAmqToRestMessage1.xml" to "queue" with name "inventoryReceived"
    And checks that query "SELECT * FROM todo WHERE task LIKE '%Contact Joe Doe%ABC789%'" has "1" output
    And checks that query "SELECT * FROM todo WHERE task LIKE '%Contact John Smith%XYZ123%'" has no output

    And publish JMS message from resource "tutorialAmqToRestMessage2.xml" to "queue" with name "inventoryReceived"
    And checks that query "SELECT * FROM todo WHERE task LIKE '%Contact Joe Doe%ABC789%'" has "2" output
    And checks that query "SELECT * FROM todo WHERE task LIKE '%Contact John Smith%XYZ123%Contact Joe Doe%ABC789%'" has "1" output

    And publish JMS message from resource "tutorialAmqToRestMessage3.xml" to "queue" with name "inventoryReceived"
    And checks that query "SELECT * FROM todo WHERE task LIKE '%Contact Joe Doe%ABC789%'" has "2" output
    And checks that query "SELECT * FROM todo WHERE task LIKE '%Contact John Smith%XYZ123%Contact Joe Doe%ABC789%'" has "1" output
    And checks that query "SELECT * FROM todo WHERE task LIKE '%No contact found%ABC789%'" has "1" output
    And checks that query "SELECT * FROM todo " has "3" output
