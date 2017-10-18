Feature: Salesforce Lead to DB store procedure integration

    Given user have access to Syndesis 
    Given user have access to SF account
    Given user have access to sample DB
  
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

  Scenario: Validate DB connection
    # DB connection is pre-configured by backend
    When "Camilla" navigates to the "Connections"
    And she selects "PostgresDB" connection
    Then she is presented with "Connection Details"
    When "Camilla" clicks on "Edit" button
    Then she is presented with "Validate"
    
    When "Camilla" clicks on "Validate" button
    Then connection detail must have alert with text "PostgresDB has been successfully validated"
  
  Scenario: Create integration from Salesforce to DB
    # create integration
    When "Camilla" navigates to the "Home" page
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections
    # salesforce
    When Camilla selects the "Salesforce QE" connection
    And she selects "On create" integration action
    And she selects "Lead" from "Object name" dropdown
    And Camilla clicks on the "Done" button
    Then she is prompted to select a "Finish" connection from a list of available connections
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL stored procedure" integration action
    And she selects "add_lead" from "Procedure Name" dropdown
    And Camilla clicks on the "Done" button
    Then she is presented with the "Add a Step" button
    
    # add data mapper step
    When Camilla click on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    # mapping details TBD
    # create mapping to fill-in fields in stored procedure
    
    # finish and save integration
    When she defines integration name "Salesforce to DB E2E"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Salesforce to DB E2E" integration details
    And Camilla clicks on the "Done" button
    And Integration "Salesforce to DB E2E" is present in integrations list

  Scenario: Verify the integration
    When "Camilla" navigates to the "Integration List"
    Then she waits until "Salesforce to DB E2E" is in "Active" state
    
    When "Camilla" navigates to the "Salesforce Lead"
    And she creates new lead
    Then "Camilla" navitages to the "TODO app"
    And she verifies that new lead is present

    When "Camilla" navigates to the "OpenShift console"
    Then she verifies integrations is "Running"
    Then she verifies integrations has no errors in log


    
