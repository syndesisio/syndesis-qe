@screenshots
Feature: Take screenshots of all pages in various resolutions 
  https://github.com/syndesisio/syndesis-e2e-tests/issues/42

Scenario: Screenshots at various resolutions
  Given "1280" x "720" resolution is set for screenshots  
  And "1920" x "1080" resolution is set for screenshots 
  And "800" x "600" resolution is set for screenshots

  #Dasboard 
  When "Camilla" logs into the Syndesis
  Then Camilla is presented with the Syndesis page "Home"
  And she takes a screenshot of "Dashboard" 

  #Create integration
  When "Camilla" navigates to the "Home" page
  And clicks on the "Create Integration" button to create a new integration.
  Then she is presented with a visual integration editor
  And she takes a screenshot of "CreateIntegration"

  #Create connection
  When "Camilla" navigates to the "Home" page
  And clicks on the "Create Connection" button to create a new integration.
  Then Camilla can see "Facebook" connection
  And she takes a screenshot of "CreateConnection"

  #Integrations 
  When "Camilla" navigates to the "Home" page
  And "Camilla" navigates to the "Integrations" page
  Then Camilla is presented with the Syndesis page "Integrations" 
  And she takes a screenshot of "Integrations"

  #Connections
  When "Camilla" navigates to the "Connections" page
  Then Camilla is presented with the Syndesis page "Connections"
  And she takes a screenshot of "Connections"

  #Twitter connection
  When clicks on the "Create Connection" button to create a new connection.
  And Camilla selects the "Twitter" connection
  Then she is presented with the "Validate" button
  And she takes a screenshot of "TwitterConnection"
  When clicks on the "Cancel" button 
  Then Camilla is presented with the Syndesis page "Connections"

  #Salesforce
  When clicks on the "Create Connection" button to create a new connection.
  And Camilla selects the "Salesforce" connection
  Then she is presented with the "Validate" button
  And she takes a screenshot of "SalesforceConnection"
  And clicks on the "Cancel" button 

  #Settings
  When "Camilla" navigates to the "Settings" page
  Then Camilla is presented with the Syndesis page "Settings"
  And she takes a screenshot of "Settings"





