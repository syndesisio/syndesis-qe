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

  @ENTESB-14559
  @integration-labels
  Scenario: Check integration labels
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

  @ENTESB-16398
  @integration-envs
  Scenario: Check integration env
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And add environment variables
      | ENV1 | VALUE1 |
    Then check that integration contains environment variables and they values
      | ENV1 | VALUE1 |
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains environment variables and they values
      | ENV1 | VALUE1 |
    And add environment variables
      | ENV2 | VALUE2 |
    Then check that integration contains environment variables and they values
      | ENV1 | VALUE1 |
      | ENV2 | VALUE2 |
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains environment variables and they values
      | ENV1 | VALUE1 |
      | ENV2 | VALUE2 |
    When publish integration
    And navigate to the "Integrations" page
    Then wait until integration "integration1" gets into "Running" state
    Then check that the pod "integration1" contains variables:
      | ENV1 | VALUE1 |
      | ENV2 | VALUE2 |

    #delete
    When select the "integration1" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And delete environment variable "ENV2"
    Then check that integration contains environment variables and they values
      | ENV1 | VALUE1 |
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains environment variables and they values
      | ENV1 | VALUE1 |
    And add environment variables
      | ENV3 | VALUE3 |
      | ENV4 | VALUE4 |
    When publish integration
    And navigate to the "Integrations" page
    Then wait until integration "integration1" gets into "Running" state
    Then check that the pod "integration1" contains variables:
      | ENV1 | VALUE1 |
      | ENV3 | VALUE3 |
      | ENV4 | VALUE4 |
    And check that the pod "integration1" doesn't contain variables:
      | ENV2 | VALUE2 |

    #update
    When select the "integration1" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And update environment "ENV3" variable to "VALUE3UPDATED"
    And update environment "ENV4" name to "ENV4NAMEUPDATED"
    And update environment "ENV4NAMEUPDATED" variable to "VALUE4UPDATED"
    When click on the "Save" button
    And click on the "Save" link
    Then check that integration contains environment variables and they values
      | ENV1            | VALUE1        |
      | ENV3            | VALUE3UPDATED |
      | ENV4NAMEUPDATED | VALUE4UPDATED |
    When publish integration
    And navigate to the "Integrations" page
    Then wait until integration "integration1" gets into "Running" state
    Then check that the pod "integration1" contains variables:
      | ENV1            | VALUE1        |
      | ENV3            | VALUE3UPDATED |
      | ENV4NAMEUPDATED | VALUE4UPDATED |
    And check that the pod "integration1" doesn't contain variables:
      | ENV3 | VALUE3 |
      | ENV4 | VALUE4 |
