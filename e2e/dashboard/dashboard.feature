@dashboard
Feature: Test to verify Dashboard links
  https://issues.jboss.org/browse/IPAAS-289
  
  Scenario: First pass at login, homepage
    Given clean application state
    When "Camilla" logs into the Syndesis URL for her installation (e.g. rh-syndesis.[openshift online domain].com)
    Then Camilla is presented with the Syndesis page "Home"
    
  Scenario: Verify dashboard cards existence
    When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    Then she is presented with the "Top 5 Integrations, Integration Board, Recent Updates" tables

  Scenario: Verify dashboard links existence
    When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    Then she is presented with the "Create Integration" button
    And she is presented with the "Create Connection" button
    And Camilla is presented with the "View All Integrations" link
    And Camilla is presented with the "View All Connections" link

  Scenario: Verify View "View All Connections" link
  	When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    Then Camilla clicks on the "View All Connections" link
    And Camilla is presented with the Syndesis page "Connections"

  Scenario: Verify "Create Connection" button
    When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    And Camilla clicks on the "Create Connection" button
    Then she is presented with a connection create page

  Scenario: Create connection happy path and verify its presence on dashboard
    When "Camilla" navigates to the "Home" page  
    Then Camilla click on the "Create Connection" button
    And Camilla selects the "Twitter" connection
    Then she is presented with the "Validate" button

    When she fills "Twitter" connection details
    And scroll "top" "right"
    And click on the "Next" button
    And type "dashboard verification connection" into connection name
    And type "connection for dashboard verification" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"
    
    When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    Then Camilla can see "dashboard verification connection" connection on dashboard page
    
    When Camilla clicks on the "View All Connections" link
    Then Camilla deletes the "dashboard verification connection" connection
    Then she can see success notification
    Then Camilla can not see "dashboard verification connection" connection anymore
    
  Scenario: Verify View "View All Integrations" link
  	When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    Then Camilla clicks on the "View All Integrations" link
    And Camilla is presented with the Syndesis page "Integrations"

  Scenario: Verify "Create integration" button
    When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    And Camilla clicks on the "Create Integration" button
    Then she is presented with a visual integration editor
  
  Scenario: Create integration and verify its presence on dashboard tables
    When "Camilla" navigates to the "Home" page
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

    When Camilla clicks on the "Save as Draft" button
    And she defines integration name "dashboard verification integration"
    Then Camilla clicks on the "Save as Draft" button
    And Camilla is presented with "dashboard verification integration" integration details
    Then Camilla clicks on the "Done" button
    And Integration "dashboard verification integration" is present in integrations list
    
    When "Camilla" navigates to the "Home" page to see what's available in the Syndesis
    Then Integration "dashboard verification integration" is present in top 5 integrations
    And Camilla is presented with the "dashboard verification integration" link
    
    When Camilla deletes the "dashboard verification integration" integration in top 5 integrations
    Then she can see success notification
    Then Camilla can not see "dashboard verification integration" integration in top 5 integrations anymore