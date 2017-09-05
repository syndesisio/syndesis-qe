@twitter-search-test
Feature: Test to verify that as a citizen user, search for Tweets using Twitter connection has been added.
  https://app.zenhub.com/workspace/o/syndesisio/syndesis-project/issues/3

  # we need to create Twitter Connection first
  Scenario: Create Twitter connection  
    Given clean application state
    
    # create salesforce connection
    When "Camilla" navigates to the "Connections" page
    And click on the "Create Connection" button
    And Camilla selects the "Salesforce" connection
    Then she is presented with the "Validate" button
    # fill salesforce connection details
    When she fills "QE Salesforce" connection details
    And scroll "top" "right"
    And click on the "Next" button
    And type "QE Salesforce" into connection name
    And type "SyndesisQE salesforce test" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"
    
    # create twitter connection
    When "Camilla" logs into the Syndesis
    And "Camilla" navigates to the "Connections" page
    And click on the "Create Connection" button
    And Camilla selects the "Twitter" connection
    Then she is presented with the "Validate" button
    # fill twitter connection details
    When she fills "Twitter Listener" connection details
    And scroll "top" "right"
    And click on the "Next" button
    And type "Twitter Listener" into connection name
    And type "SyndesisQE Twitter listener account" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"

  Scenario: Create integration with twitter and search settings

    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    When Camilla selects the "Twitter Listener" connection
    And she selects "Search" integration action
    #twitter search test itself:
    Then she fills keywords field with random text to configure search action
    And click on the "Next" button

    Then she is prompted to select a "Finish" connection from a list of available connections
    When Camilla selects the "QE Salesforce" connection
    And she selects "Create Opportunity" integration action

    Then click on the integration save button
    And she defines integration name "Twitter search integration"
    And click on the "Save as Draft" button
    Then Camilla is presented with "Twitter search integration" integration details
    And Camilla clicks on the "Done" button
    And Integration "Twitter search integration" is present in integrations list

    When Camilla deletes the "Twitter search integration" integration
    Then Camilla can not see "Twitter search integration" integration anymore
