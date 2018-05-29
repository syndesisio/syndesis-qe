@dropbox
Feature: Integration - Dropbox to Dropbox

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis

    Given created connections
      | Dropbox | QE Dropbox | QE Dropbox | SyndesisQE Dropbox test |
    And navigate to the "Home" page

#
#  1. Functionality test of upload and download
#
  @dropbox-integration
  Scenario: Create
    # upload file
    When upload file with path "/testInputFile.txt" and content "some content" on Dropbox
    Then check that file with path "/testInputFile.txt" exists on Dropbox

    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # start step
    When select the "QE Dropbox" connection
    And select "Download" integration action

    Then fill in "remotePath" action configure component input with "/testInputFile.txt" value
    Then click on the "Next" button
    Then click on the "Done" button


    And check that position of connection to fill is "Finish"
    # finish step
    When select the "QE Dropbox" connection
    And select "Upload" integration action

    Then fill in "remotePath" action configure component input with "/testOutputFile.txt" value
    Then click on the "Next" button
    Then click on the "Done" button

    # publish integration
    Then click on the "Save as Draft" button
    And set integration name "dropbox-to-dropbox"
    Then click on the "Publish" button

    Then navigate to the "Integrations" page
    And wait until integration "dropbox-to-dropbox" gets into "Published" state

    Then check that file with path "/testOutputFile.txt" exists on Dropbox

    Then delete file with path "/testInputFile.txt" from Dropbox
    Then delete file with path "/testOutputFile.txt" from Dropbox