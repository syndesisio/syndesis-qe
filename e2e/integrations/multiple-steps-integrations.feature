@multiple-steps-integrations
Feature: Test to verify advanced integration with multiple steps
  https://issues.jboss.org/browse/IPAAS-287

  Scenario: First pass at login, homepage
    When "Camilla" logs into the Syndesis URL for her installation (e.g. rh-syndesis.[openshift online domain].com)
    Then Camilla is presented with the Syndesis page "Home"

  Scenario: Create integration with one step
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    Then she is presented with an actions list
    When she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "Salesforce Example" connection
    And she selects "Create Opportunity" integration action
    Then she is presented with the "Add a Step" button

    When Camilla click on the "Add a Step" button
    And she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path, Does Not Contain, value" parameter
    And click on the "Next" button

    When click on the "Save" button
    And she defines integration name "One step integration"
    And click on the "Save as Draft" button
    Then Camilla is presented with the Syndesis page "Integrations"
    And Integration "One step integration" is present in integrations list

    When Camilla deletes the "One step integration" integration
    Then Camilla can not see "One step integration" integration anymore
    
  Scenario: Create integration with two steps
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    Then she is presented with an actions list
    When she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "Salesforce Example" connection
    And she selects "Create Opportunity" integration action
    Then she is presented with the "Add a Step" button

    When Camilla click on the "Add a Step" button
    Then she is presented with a add step page
    And she selects "Log" integration step
    And she is presented with a "Log" step configure page
    Then she fill configure page for "Log" step with "log" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    And she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path1, Does Not Contain, value1" parameter
    And click on the "Next" button

    When click on the "Save" button
    And she defines integration name "Two steps integration"
    And click on the "Save as Draft" button
    Then Camilla is presented with the Syndesis page "Integrations"
    And Integration "Two steps integration" is present in integrations list

    When Camilla deletes the "Two steps integration" integration
    Then Camilla can not see "Two steps integration" integration anymore
    
  Scenario: Deleting steps on integration
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Example" connection
    And she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "Salesforce Example" connection
    And she selects "Create Opportunity" integration action
    Then she is presented with the "Add a Step" button

    When Camilla click on the "Add a Step" button
    Then she is presented with a add step page
    Then she selects "Log" integration step
    And she is presented with a "Log" step configure page
    Then she fill configure page for "Log" step with "log 1" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path1, Does Not Contain, value1" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path2, Does Not Contain, value2" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path3, Does Not Contain, value3" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Log" integration step
    And she is presented with a "Log" step configure page
    Then she fill configure page for "Log" step with "log 3" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Log" integration step
    And she is presented with a "Log" step configure page
    Then she fill configure page for "Log" step with "log 4" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path4, Does Not Contain, value4" parameter
    And click on the "Next" button

    When Camilla clicks on the "Add a Step" button
    Then Camilla is presented with the "Add a step" link
    And clicks on the random "Add a step" link
    Then she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill configure page for "Basic Filter" step with "Any, path5, Does Not Contain, value5" parameter
    And click on the "Next" button

    Then she delete "1" random steps and check rest

    Then she delete "3" random steps and check rest

    Then she adds "2" random steps and then check the structure

    Then "Camilla" navigates to the "Home" page