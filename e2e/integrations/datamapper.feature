@datamapper
Feature: Create integration with datamapper step

  Background:
    Given credentials for "Camilla"

  @datamapper-create-connections
  Scenario: Create Twitter and Salesforce connection
    Given clean application state

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


  @datamapper-create-integration
  Scenario: Create integration from twitter to salesforce
    # create integration
    When "Camilla" logs into the Syndesis
    And "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections
    # select twitter connection
    When Camilla selects the "Twitter Listener" connection
    And she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections
    # select salesforce connection
    When Camilla selects the "QE Salesforce" connection
    And she selects "Create/Update Contact" integration action
    And click on the "Next" button
    Then she is presented with the "Add a Step" button

#    # add data mapper step
    When Camilla click on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui
		
		# TODO: This part of scenario is currently blocked by issues related to Maven classpath inspection
		#
    # When she creates mapping from "User.ScreenName" to "TwitterScreenName__c"
    # When she creates mapping from "Text" to "Description"
    # And scroll "top" "right"
    # And click on the "Next" button
		#
    # # finish and save integration
    # When click on the "Save" button
    # And she defines integration name "twitter to salesforce"
    # And click on the "Publish" button
    # # assert integration is present in list
    # Then Camilla is presented with the Syndesis page "Integrations"
    # And Integration "twitter to salesforce" is present in integrations list
