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
    Given delete incidents with "QACUSTOM4,QACREATED1" number
    Given created connections
      | ServiceNow  | Servicenow | ServiceNow | Service-Now connection |
      | Red Hat AMQ | AMQ        | AMQ        | AMQ connection         |


  Scenario: Get incident from service now
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor

    And check that position of connection to fill is "Start"

    When select the "ServiceNow" connection
    And select "Retrieve Record" integration action
    And fill in values by element ID
      | limit | 1                |
      | query | number=QACUSTOM4 |
      | table | Incident         |

    And click on the "Next" button
    And sleep for jenkins delay or "10" seconds
    Then check that position of connection to fill is "Finish"

    When select the "AMQ" connection
    And select "Publish Messages" integration action

    And fill in values by element ID
      | destinationName | incidents |
      | destinationType | Queue     |
    And click on the "Next" button
  #  And sleep for jenkins delay or "10" seconds
    And click on the "Done" button
    And sleep for jenkins delay or "30" seconds

    When publish integration
    And set integration name "service-now-2-amq"
    And publish integration
    And clicks on the modal dialog "Confirm" button
    And create incident with "QACUSTOM4" number

    And navigate to the "Integrations" page
    And wait until integration "service-now-2-amq" gets into "Running" state
    Then verify that received incident from "incidents" queue contains "QACUSTOM4"


  Scenario: Create incident from syndesis integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "AMQ" connection
    And select "Subscribe for messages" integration action
    And fill in values by element ID
      | destinationName | incidents-create |
      | destinationType | Queue            |
    And click on the "Next" button
    And click on the "Done" button
    When select the "ServiceNow" connection
    And select "Add Record" integration action
    And select "Add new Incident" from "table" dropdown
    And click on the "Next" button
    And sleep for jenkins delay or "35" seconds
    When publish integration
    And set integration name "amq-2-snow"
    And publish integration
    And clicks on the modal dialog "Confirm" button
    And navigate to the "Integrations" page
    And wait until integration "amq-2-snow" gets into "Running" state
    Then send "QACREATED1" incident to "incidents-create" queue and verify it was created in SN

