# @sustainer: acadova@redhat.com

@ui
@dropbox
@integrations-dropbox
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
  @ENTESB-11791
  @dropbox-integration
  Scenario: Upload and download files from dropbox
    # upload file
    When upload file with path "/testInputFile.txt" and content "some content" on Dropbox
    Then check that file with path "/testInputFile.txt" exists on Dropbox

    # create integration
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # start step
    When select the "QE Dropbox" connection
    And select "Download" integration action

    Then fill in "remotepath" action configure component input with "/testInputFile.txt" value
    Then click on the "Next" button
    Then click on the "Next" button


    And check that position of connection to fill is "Finish"
    # finish step
    When select the "QE Dropbox" connection
    And select "Upload" integration action

    Then fill in "remotepath" action configure component input with "/testOutputFile.txt" value
    #The select value seems unitialized at first so flipping through the options enables the Next button
    And fill in values by element data-testid
      | uploadmode | Force |
    And fill in values by element data-testid
      | uploadmode | Add |

    Then click on the "Next" button
    Then click on the "Next" button

    # publish integration
    Then click on the "Save" link
    And set integration name "dropbox-to-dropbox"
    Then publish integration

    Then navigate to the "Integrations" page
    And wait until integration "dropbox-to-dropbox" gets into "Running" state
    And wait until integration dropbox-to-dropbox processed at least 1 message

    Then check that file with path "/testOutputFile.txt" exists on Dropbox

    Then delete file with path "/testInputFile.txt" from Dropbox
    Then delete file with path "/testOutputFile.txt" from Dropbox