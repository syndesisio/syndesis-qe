# @sustainer: mkralik@redhat.com

@ui
@publicapi
@cicddialog
Feature: Test tagging integration in CI CD dialog

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

  @create-tag
  Scenario: Test creating new tag in the CI CD dialog
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag1 in CI/CD dialog
    #Check that integration1 has the new tag as checked
    Then check that tag with name tag1 is in the tag list
    And check that integration integration1 contains exactly tags
      | tag1 |
    And check that CI/CD dialog contains tags
      | tag1 |
    And check that only following tags are checked in CI/CD dialog
      | tag1 |
    When save CI/CD dialog
    And open CI/CD dialog
    Then check that only following tags are checked in CI/CD dialog
      | tag1 |
    When cancel CI/CD dialog
    #Check that integration2 dialog contains tag but is not checked
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 |
    And check that tag tag1 is not checked in CI/CD dialog
    And check that integration integration2 doesn't contain any tag
    When cancel CI/CD dialog

  @update-tag
  Scenario: Test updating tag in CI CD dialog
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag1 in CI/CD dialog
    And save CI/CD dialog
    And open CI/CD dialog
    And rename tag tag1 to tag1Updated in CI/CD dialog
    #Check that tag name was updated in the dialog on both integrations
    Then check that tag with name tag1Updated is in the tag list
    And check that tag with name tag1 is not in the tag list
    And check that CI/CD dialog contains tags
      | tag1Updated |
    And check that only following tags are checked in CI/CD dialog
      | tag1Updated |
    When save CI/CD dialog
    And open CI/CD dialog
    Then check that only following tags are checked in CI/CD dialog
      | tag1Updated |
    When cancel CI/CD dialog

    #Check that integration2 dialog contains updated tag but is not checked
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1Updated |
    And check that tag tag1Updated is not checked in CI/CD dialog
    And check that integration integration2 doesn't contain any tag
    When cancel CI/CD dialog

  @check-tag
  Scenario: Test checking tag in CI CD dialog
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag12 in CI/CD dialog
    And save CI/CD dialog

    #Check that integration2 dialog contains tag but is not checked
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag12 |
    And check that tag tag12 is not checked in CI/CD dialog
    And check that integration integration2 doesn't contain any tag

    #Check tag in the integration2 dialog
    When check tag tag12 in CI/CD dialog
    And save CI/CD dialog
    And open CI/CD dialog
    Then check that only following tags are checked in CI/CD dialog
      | tag12 |
    And check that integration integration2 contains exactly tags
      | tag12 |
    When cancel CI/CD dialog

    #Check that tag12 is still checked in the integration1
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that only following tags are checked in CI/CD dialog
      | tag12 |
    And check that integration integration1 contains exactly tags
      | tag12 |
    When cancel CI/CD dialog

  @uncheck-tag
  Scenario: Test unchecking tag in CI CD dialog and whether it is removed when no integration uses it
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag12 in CI/CD dialog
    And save CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And save CI/CD dialog

    #Uncheck tag from the integration2
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And uncheck tag tag12 in CI/CD dialog
    And save CI/CD dialog
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag12 |
    And check that tag tag12 is not checked in CI/CD dialog
    And check that integration integration2 doesn't contain any tag
    When cancel CI/CD dialog

    #Check that tag is still in the integration1
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that only following tags are checked in CI/CD dialog
      | tag12 |

    #Uncheck tag from the integration1
    When uncheck tag tag12 in CI/CD dialog
    And save CI/CD dialog
    And open CI/CD dialog

    #Check that unused tag was automatically removed
    Then check that tag tag12 doesn't exist in CI/CD dialog
    And check that integration integration1 doesn't contain any tag
    And check that tag with name tag12 is not in the tag list
    When cancel CI/CD dialog

  @delete-tag
  Scenario: Test deleting tag in CI CD dialog
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag12 in CI/CD dialog
    And save CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And save CI/CD dialog

    #Delete tag from the integration1, it will be removed from all integrations
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And delete tag tag12 in CI/CD dialog
    And cancel CI/CD dialog

    #Check that tag was removed from the Syndesis and it is not in the dialog
    Then check that tag with name tag12 is not in the tag list
    When open CI/CD dialog
    Then check that tag tag12 doesn't exist in CI/CD dialog
    When cancel CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that tag tag12 doesn't exist in CI/CD dialog
    When cancel CI/CD dialog

  @checked-multiple-tag
  Scenario: Test check and delete multiple tag across integrations
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And create new tag with name tag1 in CI/CD dialog
    And create new tag with name tag12 in CI/CD dialog
    And create new tag with name tag12ToDelete in CI/CD dialog
    And save CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    And create new tag with name tag2 in CI/CD dialog
    And check tag tag12 in CI/CD dialog
    And check tag tag12ToDelete in CI/CD dialog
    And save CI/CD dialog

    #Check that tags are checked correctly
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag2 | tag12 | tag12ToDelete |
    And check that only following tags are checked in CI/CD dialog
      | tag1 | tag12 | tag12ToDelete |
    When cancel CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag2 | tag12 | tag12ToDelete |
    And check that only following tags are checked in CI/CD dialog
      | tag2 | tag12 | tag12ToDelete |
    When cancel CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And delete tag tag12ToDelete in CI/CD dialog
    And save CI/CD dialog

    #Check that delete multi-tag doesn't affect other tags
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that tag tag12ToDelete doesn't exist in CI/CD dialog
    And check that CI/CD dialog contains tags
      | tag1 | tag2 | tag12 |
    And check that only following tags are checked in CI/CD dialog
      | tag1 | tag12 |
    When cancel CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that tag tag12ToDelete doesn't exist in CI/CD dialog
    And check that CI/CD dialog contains tags
      | tag1 | tag2 | tag12 |
    And check that only following tags are checked in CI/CD dialog
      | tag2 | tag12 |
    When cancel CI/CD dialog

    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    And rename tag tag12 to tag12Renamed in CI/CD dialog
    And save CI/CD dialog

    #Check that rename multi-tag works correctly
    And navigate to the "Integrations" page
    And select the "integration1" integration
    And open CI/CD dialog
    Then check that CI/CD dialog contains tags
      | tag1 | tag2 | tag12Renamed |
    And check that only following tags are checked in CI/CD dialog
      | tag1 | tag12Renamed |
    When cancel CI/CD dialog
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And open CI/CD dialog
    Then check that tag tag12 doesn't exist in CI/CD dialog
    And check that CI/CD dialog contains tags
      | tag1 | tag2 | tag12Renamed |
    And check that only following tags are checked in CI/CD dialog
      | tag2 | tag12Renamed |
    When cancel CI/CD dialog

    # Rest verification of UI CI/CD dialog
    Then check that integration integration1 contains exactly tags
      | tag1 | tag12Renamed |
    And check that integration integration2 contains exactly tags
      | tag2 | tag12Renamed |
