# @sustainer: sveres@redhat.com

@import-edit-integration
Feature: Integration - import-edit

  Background: Clean application state
    Given clean application state
    And reset content of "CONTACT" table
    And log into the Syndesis

  @ENTESB-11787
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
    And insert into "contact" table
      | Joe | Jackson | Red Hat | db |
    And navigate to the "Integrations" page
    Then wait until integration "import-edit-integration" gets into "Running" state
    And wait until integration import-edit-integration processed at least 1 message
    And check rows number of table "CONTACT" is greater than 1
