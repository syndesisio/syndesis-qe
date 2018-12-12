# @sustainer: mmelko@redhat.com

@servicenow
Feature: Integration - ServiceNow-amq/log

  Background: Clean application state
    Given deploy AMQ broker and add accounts
    Given clean application state
    Given log into the Syndesis
    Given clean destination type "queue" with name "incidents"
    Given delete incidents with "QACUSTOM4,QACREATED1" number
    Given clean "TODO" table
    Given created connections
      | ServiceNow         | Servicenow | ServiceNow | Service-Now connection |
      | AMQ Message Broker | AMQ        | AMQ        | AMQ connection         |


  Scenario: Get incident from service now
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor

    And check that position of connection to fill is "Start"

    When select the "ServiceNow" connection
    And select "Retrieve Record" integration action
    And .*fill in values
      | Limit of elements per page              | 1                |
      | The query used to filter the result set | number=QACUSTOM4 |
    And select "Incident" from "table" dropdown
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "AMQ" connection
    And select "Publish Messages" integration action

    And fill in values
      | Destination Name | incidents |
      | Destination Type | Queue     |
    And click on the "Next" button
    And click on the "Done" button

    When click on the "Publish" button
    And set integration name "service-now-2-amq"
    And click on the "Publish" button
    And create incident with "QACUSTOM4" number
    And navigate to the "Integrations" page
    And wait until integration "service-now-2-amq" gets into "Running" state
    Then verify that received incident from "incidents" queue contains "QACUSTOM4"

  Scenario: Create incident from syndesis integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "AMQ" connection
    And select "Subscribe for messages" integration action
    And fill in values
      | Destination Name | incidents-create |
      | Destination Type | Queue            |
    And click on the "Next" button
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "ServiceNow" connection
    And select "Create Record" integration action
    And select "Add new Incident" from "table" dropdown
    And click on the "Next" button
    When click on the "Publish" button
    And set integration name "amq-2-snow"
    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "amq-2-snow" gets into "Running" state

    Then send "QACREATED1" incident to "incidents-create" queue and verify it was created in SN

