# @sustainer: mcada@redhat.com

@3scale
Feature: 3scale integration

  Background: Clean application state
    Given log into the Syndesis
    And clean application state
    And set 3scale discovery variable to "true"


  @3scale-annotations
  Scenario: 3scale discovery annotations
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    And sleep for jenkins delay or "5" seconds
    And upload swagger file src/test/resources/swagger/connectors/petstore.json
    And click on the "Next" button
    # wait for redirect so it does not click twice on the same page
    And sleep for jenkins delay or "3" seconds
    And click on the "Next" button

    And fill in values
      | Integration Name | threeScaleIntegration |

    And click on the "Save and continue" button
    # give UI time to render all operations
    And sleep for jenkins delay or "10" seconds
    # TODO: should be refactored after api provider tests are done by asmigala
    And select first api provider operation
    And click on the "Publish" button
    Then check visibility of "threeScaleIntegration" integration details

    When navigate to the "Integrations" page
    Then Integration "threeScaleIntegration" is present in integrations list
    And wait until integration "threeScaleIntegration" gets into "Running" state

    Then check that 3scale annotations are present on integration "threeScaleIntegration"
