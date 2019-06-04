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
  @support-page-version-check
  Scenario: Version
    When navigates to the "About" page in help menu
    And check version string

#
#  2. download all diagnostic info
#
  @support-page-download-diagnostic
  Scenario: Export diagnostic of all
    And download diagnostics for all integrations

#
#  3. download specific diagnostic info
#
  @support-page-download-specific-diagnostic
  Scenario: Export diagnostic of single integration
    Given remove file "syndesis.zip" if it exists

    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce    | QE Salesforce    | SyndesisQE salesforce test          |

    # create integration
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    Then check that position of connection to fill is "Finish"

    # select salesforce connection
    When select the "QE Salesforce" connection
    And select "Create or update record" integration action
    And select "Contact" from "sObjectName" dropdown
    And click on the "Next" button
    And select "TwitterScreenName" from "sObjectIdName" dropdown
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
    When click on the "Save" button
    And set integration name "my-integration"
    And publish integration
    Then wait until integration "my-integration" gets into "Running" state

    #TODO: create integration - use rest steps so it is easier?
    When navigate to the "Support" page in help menu

    And download diagnostics for "my-integration" integration

