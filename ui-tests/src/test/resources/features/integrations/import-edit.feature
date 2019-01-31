# @sustainer: mastepan@redhat.com

@import-edit-integration
Feature: Integration - import-edit

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    
  Scenario: Import, edit, publish, check integration

    When navigate to the "Integrations" page
    And click on the "Import" button
    And import the integration from file integrations/import-edit-integration-export.zip
    And navigate to the "Integrations" page

    And select the "import-edit-integration" integration.*
    And click on the "Edit Integration" button

    And edit integration step on position 1

    And fills in values
    | SQL statement | select * from contact limit 1 |

    And click on the "Done" button
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "import-edit-integration" gets into "Running" state

    Then check rows number of table "CONTACT" is greater than 1 after 30 s
