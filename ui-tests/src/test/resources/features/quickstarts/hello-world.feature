# @sustainer: mkralik@redhat.com
# https://github.com/syndesisio/syndesis-quickstarts/tree/master/hello-world

@quickstart
@quickstart-hello-world
Feature: Quickstart Hello World

  Background: Clean application state and prepare what is needed
    Given log into the Syndesis
    And clean application state

  @quickstart-solution
  Scenario: Import and run solution
    When navigate to the "Integrations" page
    And click on the "Import" button
    And import integration from relative file path "./src/test/resources/quickstarts/HelloWorld-export.zip"

    And navigate to the "Integrations" page
    And Integration "Hello World" is present in integrations list
    And wait until integration "Hello World" gets into "Stopped" state
    And select the "Hello World" integration
    And click on the "Edit Integration" button

    Then check there are 3 integration steps
    And check that 1. step has Simple Timer title
    And check that 2. step has Log title
    And check that 3. step has Simple Logger title

    When publish integration
    And navigate to the "Integrations" page
    And wait until integration "Hello World" gets into "Running" state

    And sleep for "60000" ms
    And select the "Hello World" integration
    And click on the "Activity" tab
    Then check that 1. step in the 1. activity contains Hello World!! in the output

  # https://youtu.be/Z81TyyvBxy0
  @quickstart-video
  Scenario: Check process in the video
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Timer" connection
    And select "Simple Timer" integration action
    And fill in values by element ID
      | period        | 1       |
      | select-period | Minutes |
    And click on the "Done" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And select "Simple Logger" integration action
    And fill in values
      | log level      | INFO  |
      | Log Body       | false |
      | Log message Id | false |
      | Log Headers    | false |
      | Log everything | true  |
    And click on the "Done" button

    When add integration step on position "0"
    And select "Log" integration step
    And fill in values
      | Message Context | false         |
      | Message Body    | false         |
      | Custom Text     | Hello World!! |
    And click on the "Done" button

    And publish integration
    And set integration name "Hello World video"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Hello World video" gets into "Running" state

    And sleep for "60000" ms
    And select the "Hello World video" integration
    And click on the "Activity" tab
    Then check that 1. step in the 1. activity contains Hello World!! in the output
