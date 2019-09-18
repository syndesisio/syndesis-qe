# @sustainer: mcada@redhat.com

@ui
@datamapper
@salesforce
@twitter
@support-page
Feature: Support page

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    And navigate to the "Support" page in help menu

#
#  1. version check
#
  @gh-5960
  @support-page-version-check
  Scenario: Version
    When navigates to the "About" page in help menu
    Then check version string in about page
    And check that commit id exists in about page
    And check that build id exists in about page



#
#  2. download all diagnostic info
#
  @support-page-download-diagnostic
  Scenario: Export diagnostic of all
    And download diagnostics for all integrations

#
#  3. download specific diagnostic info
#
  @oauth
  @support-page-download-specific-diagnostic
  Scenario: Export diagnostic of single integration
    Given remove file "syndesis.zip" if it exists
    And navigate to the "Settings" page
    And fill all oauth settings

    Given create connections using oauth
      | Twitter    | Twitter Listener |
      | Salesforce | QE Salesforce    |

    # create integration
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    # select salesforce connection
    When select the "QE Salesforce" connection
    And select "Create or update record" integration action
    And fill in values by element data-testid
      | sobjectname      | Contact |
    And click on the "Next" button
    And force fill in values by element data-testid
      | sobjectidname    | TwitterScreenName__c |
    And click on the "Done" button

    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then create data mapper mappings
      | user.screenName | TwitterScreenName__c |
      | text            | Description          |
      | user.name       | FirstName; LastName  |

    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    When click on the "Save" link
    And set integration name "my-integration"
    And publish integration
    Then wait until integration "my-integration" gets into "Running" state

    When navigate to the "Support" page in help menu

    And download diagnostics for "my-integration" integration

