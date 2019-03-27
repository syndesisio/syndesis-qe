# @sustainer: mcada@redhat.com

@ui
@oauth
Feature: Connections - OAuth

  Background: Clean application state
    Given clean application state
    And log into the Syndesis

#
#  1. Create connectors which allow OAuth
#
  @oauth-validate-connectors
  Scenario: Create integration using connections with OAuth
    When navigate to the "Settings" page
    Then check that settings item "Salesforce" has button "Register"
    When fill all oauth settings
    Then create connections using oauth
#    Until the issue with @concur support is resolved, concur testing will be disabled.
#      | SAP Concur      | Test-Concur-connection          |
      | Gmail           | Test-Gmail-connection           |
      | Salesforce      | Test-Salesforce-connection      |
      | Google Sheets   | Test-Google-Sheets-connection   |
      | Google Calendar | Test-Google-Calendar-connection |
      | Twitter         | Test-Twitter-connection         |

  @oauth-gmail
  Scenario: Testing Gmail OAuth connector
    Then delete emails from "jbossqa.fuse@gmail.com" with subject "syndesis-tests"
    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Gmail | Test-Gmail |
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration
    And check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    And select the "Test-Gmail" connection
    And select "Receive Email" integration action
    And fill in values 
      | labels | syndesis-test |
    And click on the "Done" button

    And check that position of connection to fill is "Finish"
    And select "Log" integration step  
    And fill in values
      | Message Body | checked |
    And click on the "Done" button

    And click on the "Save" button
    And set integration name "OAuth-gmail-test"
    And publish integration

    Then navigate to the "Integrations" page
    And Integration "OAuth-gmail-test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "OAuth-gmail-test" gets into "Running" state

    #give gmail time to receive mail
    When send an e-mail

    Then sleep for "6000" ms
    And validate that logs of integration "OAuth-gmail-test" contains string "syndesis-tests"
    And delete emails from "jbossqa.fuse@gmail.com" with subject "syndesis-tests"
  
  @oauth-twitter
  Scenario: Testing Twitter OAuth connector
    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Twitter | Twitter-test |
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration
    And check visibility of visual integration editor
    
    Then check that position of connection to fill is "Start"
    And select the "Twitter-test" connection
    And select "Search" integration action
    And fill in values 
      | Ignore tweets previously found | false |
      | Keywords                       | test  |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"
    And select "Log" integration step
    And fill in values 
      | Message Body | checked |
    And click on the "Done" button

    And click on the "Save" button
    And set integration name "OAuth-twitter-test"
    And publish integration

    Then navigate to the "Integrations" page
    And Integration "OAuth-twitter-test" is present in integrations list
    And wait until integration "OAuth-twitter-test" gets into "Running" state

    And sleep for "2000" ms
    #Something was returned -> Twitter was connected via OAuth successfully
    And validate that logs of integration "OAuth-twitter-test" contains string "Body"

  @oauth-gcalendar
  Scenario: Testing Google calendar OAuth connector
    Given renew access token for "QE Google Calendar" google account
    And create calendars
      | google_account     | calendar_summary | calendar_description                      |
      | QE Google Calendar | syndesis-test1   | short-lived calendar for integration test |

    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Google Calendar | Gcalendar-test |
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration
    And check visibility of visual integration editor
    
    Then check that position of connection to fill is "Start"
    And select the "Gcalendar-test" connection
    And select "Get Events" integration action
    And fill in values 
      | Calendar name                                            | syndesis-test1 |
      | Consume from the current date ahead                      | false          |
      | Consume from the last event update date on the next poll | false          |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"
    And select "Log" integration step
    And fill in values 
      | Message Body | checked |
    And click on the "Done" button

    And click on the "Save" button
    And set integration name "OAuth-Gcalendar-test"
    And publish integration

    Then navigate to the "Integrations" page
    And Integration "OAuth-Gcalendar-test" is present in integrations list
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary     | start_date | start_time | end_date   | end_time | description  | attendees              |
      | past_event1 | 2018-10-01 | 10:00:00   | 2018-10-01 | 11:00:00 | An old event | jbossqa.fuse@gmail.com |
    And wait until integration "OAuth-Gcalendar-test" gets into "Running" state

    And sleep for "2000" ms
    #Something was returned -> Google calendar was connected via OAuth successfully
    Then validate that logs of integration "OAuth-Gcalendar-test" contains string "Body"
  
  @oauth-salesforce
  Scenario: Testing Salesforce OAuth connector
    Then delete lead from SF with email: "k1stieranka1@istrochem.sk"

    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Salesforce | Salesforce-test |
    
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration
    And check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    And select the "Salesforce-test" connection

    And select "On create" integration action
    And select "Lead" from "sObjectName" dropdown
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"
    And select "Log" integration step
    And fill in values 
      | Message Body | checked |
    And click on the "Done" button

    And click on the "Save" button
    And set integration name "OAuth-Salesforce-test"
    And publish integration

    Then navigate to the "Integrations" page
    And Integration "OAuth-Salesforce-test" is present in integrations list

    And wait until integration "OAuth-Salesforce-test" gets into "Running" state
    And create SF lead with first name: "Karol1", last name: "Stieranka1", email: "k1stieranka1@istrochem.sk" and company: "Istrochem"
    And sleep for "2000" ms
    Then validate that logs of integration "OAuth-Salesforce-test" contains string "k1stieranka1@istrochem.sk"
    Then delete lead from SF with email: "k1stieranka1@istrochem.sk"
  
  @oauth-gsheets
  Scenario: Testing Google Sheets OAuth connector
    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Google Sheets | Gsheets-test |
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration
    And check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    And select the "Gsheets-test" connection
    And select "Get spreadsheet properties" integration action
    And fill in values 
      | SpreadsheetId | 1_OLTcj_y8NwST9KHhg8etB10xr6t3TrzaFXwW2dhpXw |
    And click on the "Done" button

    And check that position of connection to fill is "Finish"
    And select "Log" integration step  
    And fill in values
      | Message Body | checked |
    And click on the "Done" button

    And click on the "Save" button
    And set integration name "OAuth-gsheets-test"
    And publish integration

    Then navigate to the "Integrations" page
    And Integration "OAuth-gsheets-test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "OAuth-gsheets-test" gets into "Running" state

    Then sleep for "6000" ms

    And validate that logs of integration "OAuth-gsheets-test" contains string "Body:"
