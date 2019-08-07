# @sustainer: mkralik@redhat.com

@ui
@publicapi
@export-import
@gh-6360
Feature: Import and export integration test

  Background: Clean application state
    Given clean application state
    And deploy public oauth proxy
    And set up ServiceAccount for Public API
    And delete all tags in Syndesis
    And log into the Syndesis
    And navigate to the "Home" page

    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "integration1"
    And save and cancel integration editor
    And navigate to the "Home" page

    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "integration2"
    And save and cancel integration editor
    And navigate to the "Home" page

    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "integration3"
    And save and cancel integration editor
    And navigate to the "Home" page

  @export-import-integration
  Scenario: Test export the integrations according to tag and import it again
    When navigate to the "Integrations" page
    And click on the "Manage CI/CD" link to manage tags.
    And create new tag with name tag1 in Manage CI/CD page
    And create new tag with name tag2 in Manage CI/CD page
    And create new tag with name tag12 in Manage CI/CD page
    And create new tag with name tag13 in Manage CI/CD page
    And create new tag with name anotherTag1 in Manage CI/CD page
    And create new tag with name anotherTag2 in Manage CI/CD page

    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And check tag tag1 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And check tag tag13 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And check tag tag2 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    And check tag tag13 in CI/CD dialog
    And check tag anotherTag1 in CI/CD dialog
    And check tag anotherTag2 in CI/CD dialog
    And save CI/CD dialog

    Then check that tag tag1 is used in 1 integrations
    And check that tag tag2 is used in 1 integrations
    And check that tag tag12 is used in 2 integrations
    And check that tag tag13 is used in 2 integrations
    And check that tag anotherTag1 is used in 1 integrations
    And check that tag anotherTag2 is used in 1 integrations

    # Export integration according to tag via Public REST API
    When export integrations with tag tag12 as "export12UI.zip"
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
      | anotherTag1 | anotherTag2 | tag13 | tag1 | tag2 | tag12 |
    And check that only following tags are checked in CI/CD dialog
      | anotherTag1 | anotherTag2 | tag13 |
    # Check that some tags are not used gh-5917
    And check that tag tag1 is used in 0 integrations
    And check that tag tag2 is used in 0 integrations
    And check that tag tag12 is used in 0 integrations
    And check that tag tag13 is used in 1 integrations
    And check that tag anotherTag1 is used in 1 integrations
    And check that tag anotherTag2 is used in 1 integrations
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

    Then check that tag tag1 is used in 1 integrations
    And check that tag tag2 is used in 1 integrations
    And check that tag tag12 is used in 2 integrations
    And check that tag tag13 is used in 2 integrations
    And check that tag importedTag is used in 2 integrations
    And check that tag anotherTag1 is used in 1 integrations
    And check that tag anotherTag2 is used in 1 integrations

  @export-import-all-integration
  Scenario: Test export the all integrations and import it again
    When navigate to the "Integrations" page
    And click on the "Manage CI/CD" link to manage tags.
    And create new tag with name tag1 in Manage CI/CD page
    And create new tag with name tag2 in Manage CI/CD page
    And create new tag with name tag12 in Manage CI/CD page
    And create new tag with name tag13 in Manage CI/CD page
    And create new tag with name anotherTag1 in Manage CI/CD page
    And create new tag with name anotherTag2 in Manage CI/CD page

    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And check tag tag1 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And check tag tag13 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And check tag tag2 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And save CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration3" integration
    And open CI/CD dialog
    And check tag tag13 in CI/CD dialog
    And check tag anotherTag1 in CI/CD dialog
    And check tag anotherTag2 in CI/CD dialog
    And save CI/CD dialog

    Then check that tag tag1 is used in 1 integrations
    And check that tag tag2 is used in 1 integrations
    And check that tag tag12 is used in 2 integrations
    And check that tag tag13 is used in 2 integrations
    And check that tag anotherTag1 is used in 1 integrations
    And check that tag anotherTag2 is used in 1 integrations
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
#   gh-5917
    Then check that CI/CD dialog contains tags
      | anotherTag1 | anotherTag2 | tag13 | tag12 | tag1 | tag2 |
    And check that only following tags are checked in CI/CD dialog
      | anotherTag1 | anotherTag2 | tag13 | tag12 |
    # Check that some tags are not used gh-5917
    And check that tag tag1 is used in 0 integrations
    And check that tag tag2 is used in 0 integrations
    And check that tag tag12 is used in 0 integrations

    # tag12 was added to integration3
    And check that tag tag12 is used in 1 integrations
    And check that tag tag13 is used in 1 integrations
    And check that tag anotherTag1 is used in 1 integrations
    And check that tag anotherTag2 is used in 1 integrations
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

    Then check that tag tag1 is used in 1 integrations
    And check that tag tag2 is used in 1 integrations
    And check that tag tag12 is used in 3 integrations
    And check that tag tag13 is used in 2 integrations
    And check that tag importedTag is used in 3 integrations
    And check that tag anotherTag1 is used in 1 integrations
    And check that tag anotherTag2 is used in 1 integrations
