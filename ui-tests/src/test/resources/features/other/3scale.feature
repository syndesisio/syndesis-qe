# @sustainer: jsafarik@redhat.com

@ui
@3scale
Feature: 3scale integration

  Background: Clean application state
    Given log into the Syndesis
    And clean application state
    And enable 3scale discovery with url "http://about:blank"

  @3scale-annotations
  Scenario: 3scale discovery annotations
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    And upload swagger file src/test/resources/swagger/connectors/petstore.json
    And click on the "Next" button
    # wait for redirect so it does not click twice on the same page
    And sleep for jenkins delay or "3" seconds
    And click on the "Next" button
    # give UI time to render all operations
    # TODO: should be refactored after api provider tests are done by asmigala
    #And select first api provider operation
    And click on the "Save" link
    And fill in values by element data-testid
      | name | threeScaleIntegration |
    And click on the "Save and publish" button
    Then Integration "threeScaleIntegration" is present in integrations list
    And wait until integration "threeScaleIntegration" gets into "Running" state
    And check that 3scale annotations are present on integration "threeScaleIntegration"

    @manual
    @3scale-discovery
    Scenario: Test discovery with real 3scale instance
      When navigate to the "Integrations" page
      And click on the "Import" link
      And import integration from relative file path "./src/test/resources/integrations/TaskAPI-export.zip"

      And navigate to the "Integrations" page
      And Integration "Task API" is present in integrations list
      And wait until integration "Task API" gets into "Stopped" state
      And select the "Task API" integration
      And click on the "Edit Integration" link
      And click on the "Save" link

      And click on the "Save and publish" button
      And wait until integration "Task API" gets into "Running" state
      And sleep for "10000" ms
