# @sustainer: mkralik@redhat.com

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
#    Then check that settings item "Salesforce" has button "Register"
    When fill all oauth settings
    Then create connections using oauth
#    Until the issue with @concur support is resolved, concur testing will be disabled.
#      | SAP Concur      | Test-Concur-connection          |
      | Gmail           | Test-Gmail-connection           |
      | Salesforce      | Test-Salesforce-connection      |
      | Google Sheets   | Test-Google-Sheets-connection   |
      | Google Calendar | Test-Google-Calendar-connection |

  @oauth-gmail
  Scenario: Testing Gmail OAuth connector
    Given delete emails from "QE Google Mail" with subject "syndesis-tests"
    When navigate to the "Settings" page
    And fill "Gmail" oauth settings "QE Google Mail"
    And navigate to the "Connections" page
    And create connections using oauth
      | Gmail | Test-Gmail |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    When select the "Test-Gmail" connection
    And select "Receive Email" integration action
    And fill in values by element data-testid
      | labels | syndesis-test |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"
    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And click on the "Save" link
    And set integration name "OAuth-gmail-test"
    And publish integration

    When navigate to the "Integrations" page
    Then Integration "OAuth-gmail-test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "OAuth-gmail-test" gets into "Running" state

    #give gmail time to receive mail
    When send an e-mail

    And wait until integration OAuth-gmail-test processed at least 1 message
    Then validate that logs of integration "OAuth-gmail-test" contains string "syndesis-tests"
    And delete emails from "QE Google Mail" with subject "syndesis-tests"

  @ENTESB-11282
  @ENTESB-13204
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
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    When select the "Gcalendar-test" connection
    And select "Get Events" integration action
    And fill in aliased calendar values by data-testid
      | consumefromnow     | false          |
      | considerlastupdate | false          |
      | calendarid         | syndesis-test1 |

    And click on the "Next" button

    Then check that position of connection to fill is "Finish"
    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And click on the "Save" link
    And set integration name "OAuth-Gcalendar-test"
    And publish integration

    When navigate to the "Integrations" page
    Then Integration "OAuth-Gcalendar-test" is present in integrations list
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary     | start_date | start_time | end_date   | end_time | description  | attendees              |
      | past_event1 | 2018-10-01 | 10:00:00   | 2018-10-01 | 11:00:00 | An old event | jbossqa.fuse@gmail.com |
    And wait until integration "OAuth-Gcalendar-test" gets into "Running" state

    And wait until integration OAuth-Gcalendar-test processed at least 1 message
    Then validate that logs of integration "OAuth-Gcalendar-test" contains string "past_event1"

  @ENTESB-11282
  @oauth-salesforce
  Scenario: Testing Salesforce OAuth connector
    Given delete lead from SF with email: "k1stieranka1@istrochem.sk"

    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Salesforce | Salesforce-test |

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration
    And check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    When select the "Salesforce-test" connection
    And select "On create" integration action
#    test fails on this line, which is ENTESB-11822 issue (field is input, but should be select)
    And select "Lead" from "sobjectname" dropdown
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"
    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And click on the "Save" link
    And set integration name "OAuth-Salesforce-test"
    And publish integration

    When navigate to the "Integrations" page
    Then Integration "OAuth-Salesforce-test" is present in integrations list

    And wait until integration "OAuth-Salesforce-test" gets into "Running" state
    And create SF lead with first name: "Karol1", last name: "Stieranka1", email: "k1stieranka1@istrochem.sk" and company: "Istrochem"
    And wait until integration OAuth-Salesforce-test processed at least 1 message

    Then validate that logs of integration "OAuth-Salesforce-test" contains string "k1stieranka1@istrochem.sk"
    Then delete lead from SF with email: "k1stieranka1@istrochem.sk"

  @oauth-gsheets
  Scenario: Testing Google Sheets OAuth connector
    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Google Sheets | Gsheets-test |
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration
    Then check visibility of visual integration editor

    Then check that position of connection to fill is "Start"
    When select the "Gsheets-test" connection
    And select "Get spreadsheet properties" integration action
    And fill in values by element data-testid
      | spreadsheetid | 1_OLTcj_y8NwST9KHhg8etB10xr6t3TrzaFXwW2dhpXw |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"
    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And click on the "Save" link
    And set integration name "OAuth-gsheets-test"
    And publish integration

    When navigate to the "Integrations" page
    Then Integration "OAuth-gsheets-test" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "OAuth-gsheets-test" gets into "Running" state

    And wait until integration OAuth-gsheets-test processed at least 1 message

    Then validate that logs of integration "OAuth-gsheets-test" contains string "title=Test-Data"

  @reproducer
  @ENTESB-11447
  @twitter-oauth-error-msg
  Scenario: Testing Twitter OAuth error message
    When navigate to the "Settings" page
#    preliminary solution:
    And click on element with id "app-item-toggle-twitter"

    And fill in values by element data-testid
      | consumerkey    | invalidValue |
      | consumersecret | invalidValue |
    And click on the "Save" button
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Twitter" connection type
    And click on the "Connect Twitter" button
    Then check that main alert dialog contains text "Couldn't connect, check your credentials and try again."

  @reproducer
  @ENTESB-12005
  @oauth-description
  Scenario: Testing oauth description
    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Salesforce | Salesforce-test |
    And navigate to the "Connections" page
    And select the "Salesforce-test" connection
    Then check that connection description "Manage customer relations in the cloud."
    And change connection description to "updatedDescription"
    Then check that connection description "updatedDescription"
