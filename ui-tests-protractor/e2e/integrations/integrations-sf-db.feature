# Created by sveres at 12.10.17
@integrations-sf-db-test
Feature: Test to verify correct function of connections kebab menu
https://app.zenhub.com/workspace/o/syndesisio/syndesis-qe/issues/102
https://drive.google.com/file/d/0B_udTBpEdqO8WUxzTWVKX1NsME0/view

    Background:
        Given clean application state
        #   Given "QE Salesforce" connection # - this does not work. step methods cannot be called as methods, only via annotations. 
        # # Given "PostgresDB" connection #it is supposed, this connection is prepared by default

    Scenario: Create salesforce connection
        # create salesforce connection
        When "Camilla" navigates to the "Connections" page
        And clicks on the "Create Connection" button
        And Camilla selects the "Salesforce" connection
        Then she is presented with the "Validate" button
        # fill salesforce connection details
        When she fills "QE Salesforce" connection details
        And scroll "top" "right"
        And clicks on the "Next" button
        And type "QE Salesforce" into connection name
        And type "SyndesisQE salesforce test" into connection description
        And clicks on the "Create" button
        Then Camilla is presented with the Syndesis page "Connections"

    @create-sf-db-integration
    Scenario: Create integration from salesforce to postgresDB
        When "Camilla" navigates to the "Home" page 
        And clicks on the "Create Integration" button to create a new integration. 
        Then she is presented with a visual integration editor 
        And she is prompted to select a "Start" connection from a list of available connections 

        # select salesforce connection as 'from' point
        When Camilla selects the "QE Salesforce" connection 
        And she selects "On create" integration action 
        Then she fills "sObjectNameundefined" action configure component input with "Lead" value
        And click "Done" button

        # select postgresDB connection as 'to' point
        Then she is presented with a "Choose a Finish Connection" page
        When Camilla selects the "PostrgresDB" connection  
        And she selects "Invoke SQL stored procedure" integration action 
        Then she fills "sObjectNameundefined" action configure component input with "add_todo" value
        And click "Done" button 
        Then she is presented with "Specify SQL stored procedure parameters" editor
        # keeps default, i.e. do nothing.
        And clicks on the "Done" button 

        # add data mapper step
        Then she is presented with "Add to Integration" page
        When Camilla clicks on the "Add a Step" button 
        And she selects "Data Mapper" integration step 
        Then she is presented with data mapper ui 

        When she creates mapping from "LastName" to "task"
        And scroll "top" "right"
        And clicks on the "Done" button

        # finish and save integration
        Then click on the integration save button
        And she defines integration name "Salesforce to PostresDB E2E"  
        And clicks on the "Publish" button 
        # assert integration is present in list
        Then Camilla is presented with "Salesforce to PostresDB E2E" integration details 
        And Camilla clicks on the "Done" button  
