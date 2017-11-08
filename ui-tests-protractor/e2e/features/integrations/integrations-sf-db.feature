@tp2
@integrations-sf-db-test
Feature: Test to verify correct function of connections kebab menu
https://app.zenhub.com/workspace/o/syndesisio/syndesis-qe/issues/102
https://drive.google.com/file/d/0B_udTBpEdqO8WUxzTWVKX1NsME0/view

    Scenario: Clean application state
        Given clean application state

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
    # And she stays there for "3000" ms

    @create-sf-db-integration
    Scenario: Create integration from salesforce to postgresDB
        When "Camilla" navigates to the "Home" page 
        And clicks on the "Create Integration" button to create a new integration. 
        Then she is presented with a visual integration editor 
        And she is prompted to select a "Start" connection from a list of available connections 

        # select salesforce connection as 'from' point
        When Camilla selects the "QE Salesforce" connection 
        And she selects "On create" integration action 
        And she selects "Lead" from "sObjectName" dropdown
        And clicks on the "Done" button

        # select postgresDB connection as 'to' point
        Then she is presented with "Choose a Finish Connection" page
        When Camilla selects the "PostgresDB" connection
        And she selects "Invoke SQL stored procedure" integration action 
        And she selects "add_lead" from "procedureName" dropdown
        And clicks on the "Done" button 

        # add data mapper step
        Then she is presented with "Add to Integration" page
        When Camilla clicks on the "Add a Step" button
        And she selects "Data Mapper" integration step
        Then she is presented with data mapper ui
        When she creates mapping from "Company" to "company"
        And she creates mapping from "Email" to "email"
        And she creates mapping from "LeadSource" to "lead_source"
        And she creates mapping from "Phone" to "phone"
        And she creates mapping from "Status" to "lead_status"
        And she creates mapping from "Rating" to "rating"

        #   A. ONE STEP:        
        And she combines "FirstName" as "2" with "LastName" as "1" to "first_and_last_name" using "Space" separator

        #   B. Many steps: --START
        # # And she creates mapping from "FirstName" to "first_and_last_name"
        # Then she fills "FirstCombine" selector-input with "FirstName" value
        # And she selects "Combine" from "ActionSelect" selector-dropdown
        # And she selects "Space" from "SeparatorSelect" selector-dropdown
        # # Then she is presented with the "Add Source" button #this 'button' is 'link' in fact, see issue: 1156.
        # # And clicks on the "Add Source" button #for the time being keep 'link', see issue 1156
        # And clicks on the "Add Source" link 
        # Then she fills "SecondCombine" selector-input with "LastName" value
        # And she fills "FirstCombinePosition" selector-input with "2" value
        # And she fills "SecondCombinePosition" selector-input with "1" value
        # Then she fills "TargetCombine" selector-input with "first_and_last_name" value
        #   B. Many steps: --END

        # And she stays there for "12000" ms
        And scroll "top" "right"
        And clicks on the "Done" button
        # finish and save integration
        Then clicks on the integration save button

        And she defines integration name "Salesforce to PostresDB E2E" 
        And clicks on the "Publish" button 
        # assert integration is present in list
        Then Camilla is presented with "Salesforce to PostresDB E2E" integration details
        And she clicks on the "Done" button
        # wait for integration to get in active state
        Then she wait until integration "Salesforce to PostresDB E2E" get into "Active" state
