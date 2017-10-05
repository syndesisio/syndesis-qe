@integrations-basic-filter
Feature: Test to verify addition of basic filter step to integrations
  https://github.com/syndesisio/syndesis-e2e-tests/issues/13

  Scenario: Create integration with one basic filter step
    Given clean application state

    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    Then she is presented with an actions list
    When she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "HTTP Example" connection
    Then she is presented with an actions list
    And she selects "HTTP POST" integration action
    Then she fills "httpUri" action configure component input with "mock" value
    And click on the "Done" button
    Then she is presented with the "Add a Step" button

    When Camilla click on the "Add a Step" button
    And she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "ANY of the following, path, Does Not Match Regex, value, path1, Does Not Contain, value1" parameter
    And click on the "Next" button

    Then click on the integration save button
    And she defines integration name "One step integration"
    And click on the "Save as Draft" button
    Then Camilla is presented with "One step integration" integration details
    And click on the "Done" button
    Then Camilla is presented with the Syndesis page "Integrations"
    And Integration "One step integration" is present in integrations list

    When Camilla deletes the "One step integration" integration
    Then she can see success notification
    Then Camilla can not see "One step integration" integration anymore

  Scenario: Create integration with one basic filter step add and remove rules
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    Then she is presented with an actions list
    When she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "HTTP Example" connection
    Then she is presented with an actions list
    And she selects "HTTP POST" integration action
    Then she fills "httpUri" action configure component input with "mock" value
    And click on the "Done" button
    Then she is presented with the "Add a Step" button

    When Camilla click on the "Add a Step" button
    And she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "ANY of the following, path, Contains, value" parameter
    And add new basic filter rule with "path1, not equals, value1" parameters
    And add new basic filter rule with "path2, contains, value2" parameters
    And add new basic filter rule with "path3, equals, value3" parameters
    And delete basic filter rule on position "3"
    And delete basic filter rule on position "1"
    And add new basic filter rule with "path4, not equals, value4" parameters
    And add new basic filter rule with "path5, not equals, value5" parameters
    And add new basic filter rule with "path6, not equals, value6" parameters
    And delete basic filter rule on position "2"
    
    And click on the "Next" button