# @sustainer: mastepan@redhat.com

@import-edit-integration
Feature: Integration - import-edit

  Background: Clean application state
    Given clean application state
    And reset content of "CONTACT" table
    And log into the Syndesis

  Scenario: Import, edit, publish, check integration
    When navigate to the "Integrations" page
    And click on the "Import" link
    And import the integration from file integrations/import-edit-integration-export.zip
    And navigate to the "Integrations" page
    And select the "import-edit-integration" integration
    And click on the "Edit Integration" link
    And edit integration step on position 1
    And fill in values by element data-testid
      | query | select * from contact limit(1) |
    And click on the "Next" button
    And publish integration
    And click on the "Save and publish" button
    And navigate to the "Integrations" page
    Then wait until integration "import-edit-integration" gets into "Running" state
    And check rows number of table "CONTACT" is greater than 1
