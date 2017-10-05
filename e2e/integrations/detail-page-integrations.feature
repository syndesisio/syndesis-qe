@integrations-detail-page
Feature: Test to verify integration detail page functionality

  Scenario: Create integration as draft and delete it on detail page
    Given clean application state

    When "Camilla" logs into the Syndesis
    And "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    And she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "HTTP Example" connection
    Then she is presented with an actions list
    And she selects "HTTP POST" integration action
    Then she fills "httpUri" action configure component input with "mock" value
    And click on the "Done" button
    Then she is presented with the "Add a Step" button
    And click on the integration save button

    And she defines integration name "Integration to delete"
    And click on the "Save as Draft" button
    Then Camilla is presented with "Integration to delete" integration details

    When Camilla deletes the integration on detail page
    Then she can see success notification
    Then Camilla can not see "Integration to delete" integration anymore

  Scenario: Get integration from list by status and check it on detail
    When "Camilla" navigates to the "Integrations" page
    Then she clicks on integration in "Active" status and check on detail if status match and appropriate actions are available
    Then she clicks on integration in "Inactive" status and check on detail if status match and appropriate actions are available
    Then she clicks on integration in "Deleted" status and check on detail if status match and appropriate actions are available
    Then she clicks on integration in "Draft" status and check on detail if status match and appropriate actions are available
    Then she clicks on integration in "In Progress" status and check on detail if status match and appropriate actions are available

Scenario: Go trough integrations on list get its status and check it on detail
    When "Camilla" navigates to the "Integrations" page
    Then she go trough whole list of integrations and check on detail if status match and appropriate actions are available