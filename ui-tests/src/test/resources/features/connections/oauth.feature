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
#      | Gmail           | Test-Gmail-connection           |
      | Salesforce      | Test-Salesforce-connection      |
#      | Google Sheets   | Test-Google-Sheets-connection   |
#      | Google Calendar | Test-Google-Calendar-connection |

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
