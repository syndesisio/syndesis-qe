@dropbox
Feature: Integration - Dropbox to Dropbox

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis

    Given created connections
      | Dropbox | QE Dropbox | QE Dropbox | SyndesisQE Dropbox test |
    And "Camilla" navigates to the "Home" page

#
#  1. Functionality test of upload and download
#
  @dropbox-integration
  Scenario: Create
    # upload file
    When She uploads file with path "/testInputFile.txt" and content "some content" on Dropbox
    Then She checks that file with path "/testInputFile.txt" exists on Dropbox

    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # start step
    When Camilla selects the "QE Dropbox" connection
    And she selects "Download" integration action

    Then she fills "remotePath" action configure component input with "/testInputFile.txt" value
    Then she clicks on the "Next" button
    Then she clicks on the "Done" button


    And she is prompted to select a "Finish" connection from a list of available connections
    # finish step
    When Camilla selects the "QE Dropbox" connection
    And she selects "Upload" integration action

    Then she fills "remotePath" action configure component input with "/testOutputFile.txt" value
    Then she clicks on the "Next" button
    Then she clicks on the "Done" button

    # publish integration
    Then she clicks on the "Save as Draft" button
    And she sets the integration name "dropbox-to-dropbox"
    Then she clicks on the "Publish" button

    Then "She" navigates to the "Integrations" page
    And she waits until integration "dropbox-to-dropbox" gets into "Published" state

    Then She checks that file with path "/testOutputFile.txt" exists on Dropbox

    Then .*deletes file with path "/testInputFile.txt" from Dropbox
    Then .*deletes file with path "/testOutputFile.txt" from Dropbox