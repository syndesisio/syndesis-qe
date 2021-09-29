# @sustainer: mkralik@redhat.com

@ui
@log
@smoke
@smoke
@stage-smoke
@integration-info
Feature: Integration info

  Background: Clean application state
    Given log into the Syndesis
    And clean application state
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And selects the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "integration1"
    And save and cancel integration editor
    And navigate to the "Home" page

  @reproducer
  @ENTESB-11685
  @integration-same-name
  Scenario: Check error for integration with the same name
    When click on the "Create Integration" link to create a new integration.
    And selects the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    When select the "Log" connection
    And click on the "Done" button
    And click on the "Save" link
    And set integration name "integration1"
    And click on the "Save" button
    Then check that alert dialog contains text "Integration name 'integration1' is not unique"
    And check that alert dialog contains details "NoDuplicateIntegration"

  @reproducer
  @ENTESB-11685
  @integration-same-name-after-update
  Scenario: Check error for integration which has same name after update
    When click on the "Create Integration" link to create a new integration.
    And selects the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "integration2"
    And save and cancel integration editor
    And navigate to the "Home" page
    And navigate to the "Integrations" page
    And select the "integration2" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And set integration name "integration1"
    And click on the "Save" button
    Then check that alert dialog contains text "Integration name 'integration1' is not unique"
    And check that alert dialog contains details "NoDuplicateIntegration"

  @integration-labels
  Scenario:
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And add integration label "label1=value1"
    Then check that integration contains labels
      | label1=value1 |
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains labels
      | label1=value1 |
    When add integration label "label2=value2"
    Then check that integration contains labels
      | label1=value1 | label2=value2 |
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains labels
      | label1=value1 | label2=value2 |
    When publish integration
    And navigate to the "Integrations" page
    Then wait until integration "integration1" gets into "Running" state
    ## check label
    And check that the pod "integration1" contains labels
      | label1=value1 | label2=value2 |

    When select the "integration1" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And delete integration label "label2=value2"
    Then check that integration contains labels
      | label1=value1 |
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains labels
      | label1=value1 |
    When publish integration
    And navigate to the "Integrations" page
    Then wait until integration "integration1" gets into "Running" state
    And check that the pod "integration1" contains labels
      | label1=value1 |
