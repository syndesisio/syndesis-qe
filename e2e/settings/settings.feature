@settings
Feature: Set oauth app credentials


  Scenario: Set twitter oauth app settings
    Given clean application state
    When "Camilla" navigates to the "Settings" page
    Then "Camilla" is presented with "OAuth Client Management" settings tab
    And settings item "Salesforce" has button "Register"
    And settings item "Twitter" has button "Register"

    When "Camilla" clicks to the "Twitter" item "Register" button
    And fill form in "Twitter" settings item
    And "Camilla" clicks to the "Twitter" item "Save" button
    Then settings item "Twitter" must have alert with text "Registration Successful!"
