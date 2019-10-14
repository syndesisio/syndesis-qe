# @sustainer: mmelko@redhat.com

@ui
@servicenow
@activemq
@amqbroker
@database
@integrations-servicenow
Feature: Integration - ServiceNow-amq/log

  Background: Clean application state
    Given deploy ActiveMQ broker
    Given clean application state
    Given log into the Syndesis
    Given clean destination type "queue" with name "incidents"
    Given delete incidents with "{number1},{number2}" number
    Given created connections
      | ServiceNow  | Servicenow | ServiceNow | Service-Now connection |
      | Red Hat AMQ | AMQ        | AMQ        | AMQ connection         |


  Scenario: Get incident from service now
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor

    And check that position of connection to fill is "Start"

    When select the "ServiceNow" connection
    And select "Retrieve Record" integration action
    And select "Incident" from "table" dropdown
    And fill in and modify values by element ID
      | limit | 1                |
      | query | number={number1} |
    And click on the "Next" button
    And sleep for jenkins delay or "10" seconds
    Then check that position of connection to fill is "Finish"

    When select the "AMQ" connection
    And select "Publish Messages" integration action

    And fill in values by element data-testid
      | destinationname | incidents |
      | destinationtype | Queue     |
    And click on the "Next" button
    And click on the "Done" button
    And sleep for jenkins delay or "30" seconds

    When publish integration
    And set integration name "service-now-2-amq"
    And publish integration
    And create incident with "{number1}" number

    And navigate to the "Integrations" page
    And wait until integration "service-now-2-amq" gets into "Running" state
    Then verify that received incident from "incidents" queue contains "{number1}"


  Scenario: Create incident from syndesis integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "AMQ" connection
    And select "Subscribe for messages" integration action
    And fill in values by element data-testid
      | destinationname | incidents-create |
      | destinationtype | Queue            |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"description":"d","impact":1,"number":"n","urgency":1,"short_description":"d","user_input":"ui"} |
    Then click on the "Done" button

    When select the "ServiceNow" connection
    And select "Add Record" integration action
    And select "Qa Create Incident" from "table" dropdown
    Then click on the "Next" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And create data mapper mappings
      | number            | u_number            |
      | description       | u_description       |
      | impact            | u_impact            |
      | urgency           | u_urgency           |
      | short_description | u_short_description |
      | user_input        | u_user_input        |
    Then click on the "Done" button

    When publish integration
    And set integration name "amq-2-snow"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "amq-2-snow" gets into "Running" state
    Then send "{number2}" incident to "incidents-create" queue and verify it was created in SN

