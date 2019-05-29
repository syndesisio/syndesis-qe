# @sustainer: mkralik@redhat.com

@ui
@publicapi
@export-import
Feature: Import and export integration test

  Background: Clean application state
    Given clean application state
    And deploy public oauth proxy
    And set up ServiceAccount for Public API
    And delete all tags in Syndesis
    And log into the Syndesis
    And navigate to the "Home" page

    And click on the "Create Integration" button to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple Timer" integration action
    And click on the "Done" button
    And select the "Log" connection
    And click on the "Done" button
    And click on the "Save" button
    And set integration name "integration1"
    And click on the "Save" button
    And click on the "Cancel" button
    And navigate to the "Home" page

    And click on the "Create Integration" button to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple Timer" integration action
    And click on the "Done" button
    And select the "Log" connection
    And click on the "Done" button
    And click on the "Save" button
    And set integration name "integration2"
    And click on the "Save" button
    And click on the "Cancel" button
    And navigate to the "Home" page

    And click on the "Create Integration" button to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple Timer" integration action
    And click on the "Done" button
    And select the "Log" connection
    And click on the "Done" button
    And click on the "Save" button
    And set integration name "integration3"
    And click on the "Save" button
    And click on the "Cancel" button
    And navigate to the "Home" page

  @export-import-integration
  Scenario: Test export the integrations according to tag and import it again
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag1 in CI/CD dialog
    And create new tag with name tag12 in CI/CD dialog
    And create new tag with name tag13 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And create new tag with name tag2 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    And check tag tag13 in CI/CD dialog
    And create new tag with name anotherTag1 in CI/CD dialog
    And create new tag with name anotherTag2 in CI/CD dialog
    And save CI/CD dialog
    # Export integration according to tag via Public REST API
    And export integrations with tag tag12 as "export12UI.zip"
    # Delete exported integration
    And navigate to the "Integrations" page
    And delete integration with name integration1
    And delete integration with name integration2
    Then verify that integration with name integration1 doesn't exist
    And verify that integration with name integration2 doesn't exist
    And verify that integration with name integration3 exist

    # Check that integration3 was not affected exporting
    When navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | anotherTag1 | anotherTag2 | tag13 |
    And check that only following tags are checked in CI/CD dialog
      | anotherTag1 | anotherTag2 | tag13 |
    When cancel CI/CD dialog
    # Import integrations via Public REST API
    And import integrations with tag importedTag with name "export12UI.zip"

    # Check that tags was set correctly
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag12 | tag13 | tag2 | importedTag | anotherTag1 | anotherTag2 |
    And check that only following tags are checked in CI/CD dialog
      | tag1 | tag12 | tag13 | importedTag |
    When cancel CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag12 | tag13 | tag2 | importedTag | anotherTag1 | anotherTag2 |
    And check that only following tags are checked in CI/CD dialog
      | tag2 | tag12 | importedTag |
    When cancel CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag12 | tag13 | tag2 | importedTag | anotherTag1 | anotherTag2 |
    And check that only following tags are checked in CI/CD dialog
      | tag13 | anotherTag1 | anotherTag2 |
    When cancel CI/CD dialog

  @export-import-all-integration
  Scenario: Test export the all integrations and import it again
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag1 in CI/CD dialog
    And create new tag with name tag12 in CI/CD dialog
    And create new tag with name tag13 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And create new tag with name tag2 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    And check tag tag13 in CI/CD dialog
    And create new tag with name anotherTag1 in CI/CD dialog
    And create new tag with name anotherTag2 in CI/CD dialog
    And save CI/CD dialog
    # Export integration according to tag via Public REST API
    And export integrations with tag tag12 and others as "exportAllUI.zip"

    # Delete exported integration except integration3
    And navigate to the "Integrations" page
    And delete integration with name integration1
    And delete integration with name integration2
    Then verify that integration with name integration1 doesn't exist
    And verify that integration with name integration2 doesn't exist
    And verify that integration with name integration3 exist

    # Check that tag12 was added to integration3
    When navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | anotherTag1 | anotherTag2 | tag13 | tag12 |
    And check that only following tags are checked in CI/CD dialog
      | anotherTag1 | anotherTag2 | tag13 | tag12 |
    When cancel CI/CD dialog

    # Import integrations via Public REST API
    And import integrations with tag importedTag with name "exportAllUI.zip"

    # Check that tags was set correctly
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag12 | tag13 | tag2 | importedTag | anotherTag1 | anotherTag2 |
    And check that only following tags are checked in CI/CD dialog
      | tag1 | tag12 | tag13 | importedTag |
    When cancel CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag12 | tag13 | tag2 | importedTag | anotherTag1 | anotherTag2 |
    And check that only following tags are checked in CI/CD dialog
      | tag2 | tag12 | importedTag |
    When cancel CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag12 | tag13 | tag2 | importedTag | anotherTag1 | anotherTag2 |
    And check that only following tags are checked in CI/CD dialog
      | tag13 | anotherTag1 | anotherTag2 | tag12 | importedTag |
    When cancel CI/CD dialog
