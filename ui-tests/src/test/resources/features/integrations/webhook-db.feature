# @sustainer: mkralik@redhat.com

@ui
@webhook
@database
@datamapper
@integrations-webhook-db
Feature: Integration - Webhook to DB

  Background: Clean application state
    Given clean application state
    And log into the Syndesis

    And reset content of "contact" table
    And reset content of "todo" table

  @reproducer
  @gh-4182
  @webhook-db-db
  Scenario: Webhook to db with middle db step
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input| JSON Instance |
    And fill text into text-editor
       | {"first_name":"John","company":"Red Hat"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Done" button

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES('middleSTEP', 'middleStep')" value
    And click on the "Done" button

    # Then check visibility of page "Add to Integration"
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | first_name | first_name |
      | company    | company    |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to DB with DB"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to DB with DB" gets into "Running" state
    And select the "Webhook to DB with DB" integration
    And invoke post request to webhook in integration webhook-to-db with token test-webhook and body {"first_name":"John","company":"Red Hat"}

    Then checks that query "select * from contact where first_name='middleSTEP'" has 1 row output
    And checks that query "select * from contact where first_name='John'" has 1 row output

  @webhook-db-batch
  Scenario: Webhook to db with batch insert

    When navigate to the "Home" page
    And click on the "Create Integration" link
    Then check visibility of visual integration editor

    And check that position of connection to fill is "Start"
    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | [{"task": "buy bread", "completed": 1},{"task": "clean your room", "completed": 0}] |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO TODO(task, completed) VALUES(:#task, :#completed)" value
    And fill in values by element data-testid
      | batch | true |
    And click on the "Done" button

    When add integration step on position "0"
    And select the "Data Mapper" connection
    And open data mapper collection mappings
    Then check visibility of data mapper ui
    And open data mapper collection mappings
    And create data mapper mappings
      | task      | task      |
      | completed | completed |

    And click on the "Done" button
    And publish integration
    And set integration name "DB batch Insert"
    And publish integration
    Then wait until integration "DB batch Insert" gets into "Running" state

    And select the "DB batch Insert" integration
    And invoke post request to webhook in integration db-batch-insert with token test-webhook and body [{"task": "write doc", "completed": 1},{"task": "publish doc", "completed": 0}]

    And check that query "SELECT * FROM TODO WHERE task = 'write doc'" has some output
    And check that query "SELECT * FROM TODO WHERE task = 'publish doc'" has some output
