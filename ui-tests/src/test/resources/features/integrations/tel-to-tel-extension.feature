@integrations-tel-to-tel-extension
Feature: Integration - Telegram to Telegram with extension

  Background:
    Given clean application state
    Given log into the Syndesis
    Given import extensions from syndesis-extensions folder
      | syndesis-connector-telegram |
    Given created connections
      | Telegram    | Telegram | Telegram | Telegram account | no validation |

  @integration-tel-to-tel
  Scenario: Create
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Telegram" connection
    And select "Receive Messages" integration action
    And fill in values
      | Chat Id    | @chatqe_from |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"
    When select the "Telegram" connection
    And select "Send a Text Message" integration action
    And fill in values
      | Chat Id    | @chatqe_to |
    And click on the "Done" button

    # add data mapper step
    Then check visibility of the "Add a Step" button
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    Then create data mapper mappings
      | text  | text |
    And scroll "top" "right"
    And click on the "Done" button

    # finish and save integration
    Then check visibility of page "Add to Integration"
    And click on the "Publish" button
    And set integration name "tel-to-tel E2E"
    And click on the "Publish" button
    Then check visibility of "tel-to-tel E2E" integration details
    Then navigate to the "Integrations" page
    Then wait until integration "tel-to-tel E2E" gets into "Published" state
    # validation:
    Then send telegram message with text "hello from @chat_from" to "@chatqe_from" chat
    And sleep for "2000" ms
    Then validate telegram message with text "hello from @chatqe_from" was sent to "@chatqe_to" chat
