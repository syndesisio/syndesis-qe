# @sustainer: mkralik@redhat.com

@ui
@activity
@webhook
@database
@datamapper
@integration-activity
Feature: Activity

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis

  @activity-test
  Scenario: Check activity
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values
      | Webhook Token | test-webhook |
    And click on the "Next" button
    And fill in values
      | Select Type | JSON Instance |
    #only available after type is selected
    And fill in values by element ID
      | specification | {"first_name":"John","company":"Red Hat"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Done" button

    #add log
    And add integration step on position "0"
    And select "Log" integration step
    And fill in values
      | Message Context | false               |
      | Message Body    | true                |
      | Custom Text     | before basic filter |
    And click on the "Done" button

    #add basic filter step
    And add integration step on position "1"
    And select "Basic Filter" integration step
    Then check visibility of "Basic Filter" step configuration page
    # And check that basic filter step path input options contains "company" option TODO gh-issue: https://github.com/syndesisio/syndesis/issues/4162
    When fill in the configuration page for "Basic Filter" step with "ANY of the following, company, contains, Red Hat" parameter
    And click on the "Done" button

    # add log
    And add integration step on position "2"
    And select "Log" integration step
    And fill in values
      | Message Context | false                  |
      | Message Body    | true                   |
      | Custom Text     | before advanced filter |
    And click on the "Done" button

    # add advanced filter step
    And add integration step on position "3"
    And select "Advanced Filter" integration step
    Then check visibility of "Advanced Filter" step configuration page
    When fill in the configuration page for "Advanced Filter" step with "${body.company} not contains 'incorrect'" parameter
    And click on the "Done" button

    # add log
    And add integration step on position "4"
    And select "Log" integration step
    And fill in values
      | Message Context | false                |
      | Message Body    | true                 |
      | Custom Text     | before mapper filter |
    And click on the "Done" button

    # add data mapper
    And add integration step on position "5"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | first_name | first_name |
      | company    | company    |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to DB"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to DB" gets into "Running" state

    And select the "Webhook to DB" integration
    And click on the "Activity" tab
    Then check that in the activity log are 0 activities

    When click on the "Details" tab
    And save time before request
    And invoke post request to webhook with body {"first_name":"John","company":"incorrect company"}
    And save time after request
    And sleep for "3000" ms
    And click on the "Activity" tab
    Then check that in the activity log are 1 activities
    # Test activity
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors
    # Test steps in activity
    And check that 1. activity has 2 steps in the log
    And check that all steps in the 1. activity has Success status
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Log step
    And check that 1. step in the 1. activity contains Body: [[{"first_name":"John","company":"incorrect company"}]] before basic filter in the output
    And check that 2. step in the 1. activity is Basic Filter step
    And check that 2. step in the 1. activity contains No output in the output

    # post next request
    When click on the "Details" tab
    And save time before request
    And invoke post request to webhook with body {"first_name":"John","company":"Red Hat still incorrect"}
    And save time after request
    And sleep for "3000" ms
    And click on the "Activity" tab
    Then check that in the activity log are 2 activities
    # Test activity
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors

    # Test steps in activity
    # TODO should be 3 - > https://github.com/syndesisio/syndesis/issues/4181
    # TODO or 4 (with basic filter), id depends of the resolution of -> https://github.com/syndesisio/syndesis/issues/4087
    And check that 1. activity has 4 steps in the log
    And check that all steps in the 1. activity has Success status
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Log step
    And check that 1. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat still incorrect"}]] before basic filter in the output
    And check that 2. step in the 1. activity is Log step
    And check that 2. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat still incorrect"}]] before advanced filter in the output
    And check that 3. step in the 1. activity is Advanced Filter step
    And check that 3. step in the 1. activity contains No output in the output
    #TODO this step is not defined, it shouldn't be here -> gh-4181
    And check that 4. step in the 1. activity is Log step
    And check that 4. step in the 1. activity contains No output in the output

    When click on the "Details" tab
    And save time before request
    And invoke post request to webhook with body {"first_name":"John","company":"Red Hat"}
    And save time after request
    And sleep for "3000" ms
    And click on the "Activity" tab
    Then check that in the activity log are 3 activities
    # Test activity
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors

    # Test steps in activity
    # TODO should be 5 - > https://github.com/syndesisio/syndesis/issues/4181
    # TODO or 7 (with basic and advanced filter), id depends of the resolution of -> https://github.com/syndesisio/syndesis/issues/4087
    And check that 1. activity has 7 steps in the log
    And check that all steps in the 1. activity has Success status
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Log step
    And check that 1. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before basic filter in the output
    And check that 2. step in the 1. activity is Log step
    And check that 2. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before advanced filter in the output
    And check that 3. step in the 1. activity is Log step
    And check that 3. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before mapper filter in the output
    And check that 4. step in the 1. activity is Data Mapper step
    And check that 4. step in the 1. activity contains No output in the output
    And check that 5. step in the 1. activity is Invoke SQL step
    And check that 5. step in the 1. activity contains No output in the output
    #TODO this step is not defined, it shouldn't be here -> gh-4181
    And check that 6. step in the 1. activity is Log step
    And check that 6. step in the 1. activity contains No output in the output
    And check that 7. step in the 1. activity is Log step
    And check that 7. step in the 1. activity contains No output in the output

    # check that version is changed after republish
    When edit integration
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Webhook to DB" gets into "Running" state

    And select the "Webhook to DB" integration
    And click on the "Activity" tab

    When click on the "Details" tab
    And save time before request
    And invoke post request to webhook with body {"first_name":"John","company":"incorrect company"}
    And save time after request
    And sleep for "3000" ms

    And click on the "Activity" tab
    Then check that in the activity log are 4 activities
    And check that 1. activity version contains Version 2
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors

  @activity-error
  Scenario: Check error
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values
      | Webhook Token | test-webhook |
    And click on the "Next" button
    And fill in values
      | Select Type | JSON Instance |
    #only available after type is selected
    And fill in values by element ID
      | specification | {"first_name":"John","company":"Red Hat"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO contact (create_date) VALUES ('Red Hat')" value
    And click on the "Done" button

    #add log
    And add integration step on position "0"
    And select "Log" integration step
    And fill in values
      | Message Context | false                  |
      | Message Body    | true                   |
      | Custom Text     | before error insertion |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to DB with error"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to DB with error" gets into "Running" state

    And select the "Webhook to DB with error" integration
    And click on the "Activity" tab
    Then check that in the activity log are 0 activities

    When click on the "Details" tab
    And save time before request
    And invoke post request which can fail to webhook with body {"first_name":"John","company":"Red Hat"}
    And save time after request
    And sleep for "3000" ms
    And click on the "Activity" tab

    # Test activity
    Then check that in the activity log are 1 activities
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has error
    # Test steps in activity
    And check that 1. activity has 2 steps in the log
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Log step
    And check that 1. step in the 1. activity has Success status
    And check that 1. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before error insertion in the output

    And check that 2. step in the 1. activity is Invoke SQL step
    And check that 2. step in the 1. activity has Error status
    And check that 2. step in the 1. activity contains DataIntegrityViolationException: PreparedStatementCallback; SQL []; ERROR: invalid input syntax for type date: "Red Hat" in the output

  @reproducer
  @gh-4192
  @error-data-mapper-before
  Scenario: Check whether shows error when data mapper is before
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values
      | Webhook Token | test-webhook |
    And click on the "Next" button
    And fill in values
      | Select Type | JSON Instance |
    #only available after type is selected
    And fill in values by element ID
      | specification | {"first_name":"John","company":"Red Hat"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO contact (create_date) VALUES (:#date)" value
    And click on the "Done" button

    # add data mapper
    And add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | company | date |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to DB with error in final"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to DB with error in final" gets into "Running" state

    And select the "Webhook to DB with error in final" integration
    And click on the "Activity" tab
    Then check that in the activity log are 0 activities

    When click on the "Details" tab
    And save time before request
    And invoke post request which can fail to webhook with body {"first_name":"John","company":"Red Hat"}
    And save time after request
    And sleep for "3000" ms
    And click on the "Activity" tab
    # Test activity
    Then check that in the activity log are 2 activities
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has error
    # Test steps in activity
    And check that 1. activity has 1 steps in the log
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Data Mapper step
    And check that 1. step in the 1. activity has Success status
    And check that 1. step in the 1. activity contains No output in the output
    And check that 2. step in the 1. activity is Invoke SQL step
    And check that 2. step in the 1. activity has Error status
    And check that 2. step in the 1. activity contains DataIntegrityViolationException: PreparedStatementCallback; SQL []; ERROR: invalid input syntax for type date: "Red Hat" in the output
