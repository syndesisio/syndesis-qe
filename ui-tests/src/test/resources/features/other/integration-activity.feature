# @sustainer: mkralik@redhat.com

@ui
@activity
@webhook
@database
@datamapper
@integration-activity
@long-running
Feature: Activity

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And deploy ActiveMQ broker
    And log into the Syndesis
    And created connections
      | Red Hat AMQ | AMQ | AMQ | AMQ connection is awesome |

  @activity-test
  Scenario: Check activity
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
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"first_name":"John","company":"Red Hat"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | personInstance |
    And click on the "Next" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Done" button

    #add log
    And add integration step on position "0"
    And select "Log" integration step
    And fill in values by element data-testid
      | contextloggingenabled | false               |
      | bodyloggingenabled    | true                |
      | customtext            | before basic filter |
    And click on the "Done" button

    #add basic filter step
    And add integration step on position "1"
    And select "Basic Filter" integration step
    Then check visibility of "Basic Filter" step configuration page
    And check that basic filter step path input options contains "first_name" option
    And check that basic filter step path input options contains "company" option
    When fill in the configuration page for "Basic Filter" step with "ANY of the following, company, contains, Red Hat" parameter
    And click on the "Done" button

    # add log
    And add integration step on position "2"
    And select "Log" integration step
    And fill in values by element data-testid
      | contextloggingenabled | false                  |
      | bodyloggingenabled    | true                   |
      | customtext            | before advanced filter |
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
    And fill in values by element data-testid
      | contextloggingenabled | false              |
      | bodyloggingenabled    | true               |
      | customtext            | before data mapper |
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
    And set integration name "activity-test"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "activity-test" gets into "Running" state
    And select the "activity-test" integration
    And click on the "Activity" tab
    Then check that in the activity log are 0 activities

    When save time before request for integration activity-test
    And invoke post request to webhook in integration activity-test with token test-webhook and body {"first_name":"John","company":"incorrect company"}
    And save time after request for integration activity-test
    And sleep for "3000" ms
    Then check that in the activity log are 1 activities
    # Test activity
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors
    # Test steps in activity
    And check that 1. activity has 3 steps in the log
    And check that all steps in the 1. activity has Success status
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Incoming Webhook step
    And check that 1. step in the 1. activity contains No output in the output
    And check that 2. step in the 1. activity is Log step
    And check that 2. step in the 1. activity contains Body: [[{"first_name":"John","company":"incorrect company"}]] before basic filter in the output
    And check that 3. step in the 1. activity is Basic Filter step
    And check that 3. step in the 1. activity contains No output in the output

    # post next request
    When save time before request for integration activity-test
    And invoke post request to webhook in integration activity-test with token test-webhook and body {"first_name":"John","company":"Red Hat still incorrect"}
    And save time after request for integration activity-test
    And sleep for "3000" ms
    Then check that in the activity log are 2 activities
    # Test activity
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors
    # Test steps in activity
    And check that 1. activity has 5 steps in the log
    And check that all steps in the 1. activity has Success status
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Incoming Webhook step
    And check that 1. step in the 1. activity contains No output in the output
    And check that 2. step in the 1. activity is Log step
    And check that 2. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat still incorrect"}]] before basic filter in the output
    And check that 3. step in the 1. activity is Basic Filter step
    And check that 3. step in the 1. activity contains No output in the output
    And check that 4. step in the 1. activity is Log step
    And check that 4. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat still incorrect"}]] before advanced filter in the output
    And check that 5. step in the 1. activity is Advanced Filter step
    And check that 5. step in the 1. activity contains No output in the output

    # post next request
    When save time before request for integration activity-test
    And invoke post request to webhook in integration activity-test with token test-webhook and body {"first_name":"John","company":"Red Hat"}
    And save time after request for integration activity-test
    And sleep for "3000" ms
    Then check that in the activity log are 3 activities
    # Test activity
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors
    # Test steps in activity
    And check that 1. activity has 8 steps in the log
    And check that all steps in the 1. activity has Success status
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Incoming Webhook step
    And check that 1. step in the 1. activity contains No output in the output
    And check that 2. step in the 1. activity is Log step
    And check that 2. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before basic filter in the output
    And check that 3. step in the 1. activity is Basic Filter step
    And check that 3. step in the 1. activity contains No output in the output
    And check that 4. step in the 1. activity is Log step
    And check that 4. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before advanced filter in the output
    And check that 5. step in the 1. activity is Advanced Filter step
    And check that 5. step in the 1. activity contains No output in the output
    And check that 6. step in the 1. activity is Log step
    And check that 6. step in the 1. activity contains Body: [[{"first_name":"John","company":"Red Hat"}]] before data mapper in the output
    And check that 7. step in the 1. activity is Data Mapper step
    And check that 7. step in the 1. activity contains No output in the output
    And check that 8. step in the 1. activity is Invoke SQL step
    And check that 8. step in the 1. activity contains No output in the output

    # check that version is changed after republish
    When edit integration
    And publish integration
    And click on the "Save and publish" button
    And navigate to the "Integrations" page
    And sleep for jenkins delay or 5 seconds
    And wait until integration "activity-test" gets into "Running" state

    And select the "activity-test" integration
    And click on the "Activity" tab

    When save time before request for integration activity-test
    And invoke post request to webhook in integration activity-test with token test-webhook and body {"first_name":"John","company":"incorrect company"}
    And save time after request for integration activity-test
    And sleep for "3000" ms
    Then check that in the activity log are 4 activities
    And check that 1. activity version contains Version 2
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has not any errors

  @gh-4192
  @ENTESB-13811
  @activity-error
  Scenario: Check activity with error
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | activity-error |
      | destinationtype | Queue  |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"first_name":"John","company":"Red Hat"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | personInstance |
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
    And fill in values by element data-testid
      | contextloggingenabled | false                  |
      | bodyloggingenabled    | true                   |
      | customtext            | before error insertion |
    And click on the "Done" button

    And publish integration
    And set integration name "activity-error"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "activity-error" gets into "Running" state

    And select the "activity-error" integration
    And click on the "Activity" tab
    Then check that in the activity log are 0 activities

    When save time before request for integration activity-error
    And publish message with content '{"first_name":"John","company":"Red Hat"}' to queue "activity-error"
    And wait until integration activity-error processed at least 1 message
    And save time after request for integration activity-error
    And sleep for "3000" ms

    # Test activity
    Then check that in the activity log are 1 activities
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has error
    # Test steps in activity
    And check that 1. activity has 3 steps in the log
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Subscribe for messages step
    And check that 1. step in the 1. activity contains No output in the output
    And check that 2. step in the 1. activity is Log step
    And check that 2. step in the 1. activity has Success status
    And check that 2. step in the 1. activity contains Body: [[{"first_name":"John", "company":"Red Hat"}]] before error insertion in the output

    And check that 3. step in the 1. activity is Invoke SQL step
    And check that 3. step in the 1. activity has Error status
    And check that 3. step in the 1. activity contains DataIntegrityViolationException: PreparedStatementCallback; ERROR: invalid input syntax for type date: "Red Hat" in the output

  @reproducer
  @gh-4192
  @ENTESB-13811
  @activity-error-data-mapper-before
  Scenario: Check whether shows error when data mapper is before
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | activity-error-mapper |
      | destinationtype | Queue  |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"first_name":"John","company":"Red Hat"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | personInstance |
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
    And set integration name "activity-error-data-mapper-before"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "activity-error-data-mapper-before" gets into "Running" state

    And select the "activity-error-data-mapper-before" integration
    And click on the "Activity" tab
    Then check that in the activity log are 0 activities

    When save time before request for integration activity-error-data-mapper-before
    And publish message with content '{"first_name":"John","company":"Red Hat"}' to queue "activity-error-mapper"
    And wait until integration activity-error-data-mapper-before processed at least 1 message
    And save time after request for integration activity-error-data-mapper-before
    And sleep for "3000" ms
    # Test activity
    Then check that in the activity log are 1 activities
    And check that 1. activity version contains Version 1
    And check that 1. activity date and time is valid with 5 second accuracy
    And check that 1. activity has error
    # Test steps in activity
    And check that 1. activity has 3 steps in the log
    And check that all steps in the 1. activity has valid duration
    And check that all steps in the 1. activity has valid time with 5 second accuracy
    And check that 1. step in the 1. activity is Subscribe for messages step
    And check that 1. step in the 1. activity has Success status
    And check that 1. step in the 1. activity contains No output in the output
    And check that 2. step in the 1. activity is Data Mapper step
    And check that 2. step in the 1. activity has Success status
    And check that 2. step in the 1. activity contains No output in the output
    And check that 3. step in the 1. activity is Invoke SQL step
    And check that 3. step in the 1. activity has Error status
    And check that 3. step in the 1. activity contains DataIntegrityViolationException: PreparedStatementCallback; Bad value for type timestamp/date/time: {1}; nested exception is org.postgresql.util.PSQLException: Bad value for type timestamp/date/time: {1} in the output
