@twitter-search-test
Feature: Test to verify that as a citizen user, search for Tweets using Twitter connection has been added.
  https://app.zenhub.com/workspace/o/syndesisio/syndesis-project/issues/3

  Scenario: First pass at login, homepage
    When "Camilla" logs into the Syndesis URL for her installation (e.g. rh-syndesis.[openshift online domain].com)
    Then Camilla is presented with the Syndesis page "Dashboard"

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

    When click on the "Save" button
    And she defines integration name "Twitter search integration"
    And click on the "Save as Draft" button
    Then Camilla is presented with the Syndesis page "Integrations"
    And Integration "Twitter search integration" is present in integrations list

    When Camilla deletes the "Twitter search integration" integration
    Then Camilla can not see "Twitter search integration" integration anymore
