Feature: Twitter Mention to Salesforce Contact integration

    Given user have access to Syndesis 
    Given user have access to Twitter Listener account
    Given user have access to Twitter Talky account
    Given user have access to SF account

  Scenario: Create Twitter connection
    When "Camilla" navigates to the "Twitter App Manager"
    And creates new OAuth application
    Then "Camilla" is able to retriev "ConsumerKey, ConsumerSecret"

    When "Camilla" navigates to the "Settings" page
    Then "Camilla" is presented with "OAuth Client Management" settings tab
    And settings item "Twitter" has button "Register"

    When "Camilla" clicks to the "Twitter" item "Register" button
    And fill form in "Twitter" settings item
    And "Camilla" clicks to the "Twitter" item "Save" button
    Then settings item "Twitter" must have alert with text "Registration successful!"
    
    When "Camilla" clicks to the "create connection" link
    Then she is presented with "Create connection" page
    When Camilla selects the "Twitter" connection
    And she clicks "Connect" button
    And she confirms the "Twitter" oauth dialog
    Then Camilla is presented with "Connection detail" form
    And type "Twitter QE" into connection name
    And type "SyndesisQE Twitter account" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"

  Scenario: Create Salesforce connection
    #Note: Firt login from new browser requires numeric code sent to email
    When "Camilla" navigates to the "Salesforce App Manager"
    And creates new OAuth application
    Then "Camilla" is able to retriev "ConsumerKey, ConsumerSecret"

    When "Camilla" navigates to the "Settings" page
    Then "Camilla" is presented with "OAuth Client Management" settings tab
    And settings item "Salesforce" has button "Register"

    When "Camilla" clicks to the "Salesforce" item "Register" button
    And fill form in "Salesforce" settings item
    And "Camilla" clicks to the "Salesforce" item "Save" button
    Then settings item "Salesforce" must have alert with text "Registration successful!"

    When "Camilla" clicks to the "create connection" link
    Then she is presented with "Create connection" page
    When Camilla selects the "Salesforce" connection
    And she clicks "Connect" button
    And she confirms the "Twitter" oauth dialog
    Then Camilla is presented with "Connection detail" form
    And type "Salesforce QE" into connection name
    And type "SyndesisQE Salesforce account" into connection description
    And click on the "Create" button
    Then Camilla is presented with the Syndesis page "Connections"
  
  Scenario: Create integration from Twitter to Salesforce
    # create integration
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections
    # select twitter connection
    When Camilla selects the "Twitter QE" connection
    And she selects "Mention" integration action
    Then she is prompted to select a "Finish" connection from a list of available connections
    # select salesforce connection
    When Camilla selects the "Salesforce QE" connection
    And she selects "Create or update record" integration action
    And she selects "Contact" from "Object name" dropdown
    And Camilla clicks on the "Next" button
    And she selects "TwitterScreenName" from "Indetifier field" dropdown
    And Camilla clicks on the "Done" button
    Then she is presented with the "Add a Step" button

    # add data mapper step
    When Camilla click on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    When she creates mapping to split from "name" to "FirstName,LastName" 
    When she creates mapping from "user.screenName" to "TwitterScreenName__c"
    When she creates mapping from "text" to "Description"
    And scroll "top" "right"
    And click on the "Done" button

    # add a filter step
    When Camilla click on the "Add a Step" button
    And she selects "Basic Filter" integration step
    And she is presented with a "Basic Filter" step configure page
    Then she fill paremeter "text" for "Basic Filter" step
    And she selects "Contains" condition for "Basic Filter" step
    And she fill parameter "#salute"
    And click on the "Next" button

    # finish and save integration
    When she defines integration name "Twitter to Salesforce E2E"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Twitter to Salesforce E2E" integration details
    And Camilla clicks on the "Done" button
    And Integration "Twitter to Salesforce E2E" is present in integrations list

  Scenario: Verify the integration
    When "Camilla" navigates to the "Integration List"
    Then she waits until "Twitter to Salesforce E2E" is in "Active" state
    
    When "Camilla" navigates to the "Twitter Talky Account"
    And she creates new Tweet to "@syndesis_listener" with text "@syndesis_lister I want to appear in Contacts! #salute"
    Then "Camilla" navitages to the "Salesforce Contact"
    And she verifies that new contact is present with name "Syndesis Talky"

    When "Camilla" navigates to the "OpenShift console"
    Then she verifies integrations is "Running"
    Then she verifies integrations has no errors in log


    
