# @sustainer: mastepan@redhat.com

@ui
@salesforce
@database
@datamapper
@integrations-salesforce-to-database
Feature: Integration - Salesforce to DB


  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    Given clean "TODO" table
    Given clean SF, removes all leads with email: "k1stieranka1@istrochem.sk"
    Given created connections
      | Salesforce | QE Salesforce | QE Salesforce | SyndesisQE salesforce test |

  @integrations-salesforce-to-database-scenario
  Scenario: Create
    When inserts into "contact" table
      | Josef | Stieranka | Istrochem | db |
    Then navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select salesforce connection as 'from' point
    When select the "QE Salesforce" connection
    And select "On create" integration action
    And select "Lead" from "sObjectName" dropdown
    And click on the "Done" button

      # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke Stored Procedure" integration action
    And select "add_lead" from "procedureName" dropdown
    And click on the "Done" button

      # add data mapper step
    Then check visibility of page "Add to Integration"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then create data mapper mappings
      | Company             | company             |
      | Email               | email               |
      | Phone               | phone               |
      | FirstName; LastName | first_and_last_name |


#    THIS "combine step" is temporary commented out.
#    It works separately but not in combination with below "separate step".
#    UNTIL ids for datamapper input fields are available. It has no meaning to spend a lot of time
#    to find magic css selector combination to identify these fields:
#    And she combines "FirstName" as "2" with "LastName" as "1" to "first_and_last_name" using "Space [ ]" separator

      #   B. Many steps: --START
      # # And create mapping from "FirstName" to "first_and_last_name"
      # Then fill in "FirstCombine" selector-input with "FirstName" value
      # And select "Combine" from "ActionSelect" selector-dropdown
      # And select "Space" from "SeparatorSelect" selector-dropdown
      # # Then check visibility of the "Add Source" button #this 'button' is 'link' in fact, see issue: 1156.
      # # And click on the "Add Source" button #for the time being keep 'link', see issue 1156
      # And click on the "Add Source" link
      # Then fill in "SecondCombine" selector-input with "LastName" value
      # And fill in "FirstCombinePosition" selector-input with "2" value
      # And fill in "SecondCombinePosition" selector-input with "1" value
      # Then fill in "TargetCombine" selector-input with "first_and_last_name" value
      #   B. Many steps: --END

#    And sleep for "12000" ms
    And scroll "top" "right"
    And click on the "Done" button
      # finish and save integration
    And click on the "Save" button

    And set integration name "Salesforce to PostresDB E2E"
    And publish integration
      # wait for integration to get in active state
    Then wait until integration "Salesforce to PostresDB E2E" gets into "Running" state
#    VALIDATION:
    And create SF lead with first name: "Karol1", last name: "Stieranka1", email: "k1stieranka1@istrochem.sk" and company: "Istrochem"
    # give it more time to propagate lead from sf to db
    And sleep for jenkins delay or "10" seconds
    And validate DB created new lead with first name: "Karol1", last name: "Stieranka1", email: "k1stieranka1@istrochem.sk"
#    And remove all records from table "todo"
    Given clean SF, removes all leads with email: "k1stieranka1@istrochem.sk"

