# @sustainer: mkralik@redhat.com

@ui
@metrics
@webhook
@database
@datamapper
@gh-4303
@ENTESB-11388
@integration-metrics
Feature: Metrics

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis

  @metrics-test
  Scenario: Check metrics
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

    #add log
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
    And set integration name "metrics-test"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "metrics-test" gets into "Running" state

    And select the "metrics-test" integration
    And click on the "Metrics" tab
    Then check that number of total error is 0
    And check that last processed date is valid
    And check that number of valid messages is 0
    And check that number of error messages is 0
    And check that number of total messages is 0
    And check that uptime for metrics-test pod is valid
    And check that startdate for metrics-test pod is valid

    When save time before request for integration metrics-test
    And invoke post request to webhook in integration metrics-test with token test-webhook and body {"first_name":"John","company":"incorrect company"}
    And validate that logs of integration "metrics-test" contains string "Body: [[{"first_name":"John","company":"incorrect company"}]]"
    And save time after request for integration metrics-test
    And sleep for "3000" ms
    Then check that number of total error is 0
    And check that last processed date is valid
    And check that number of valid messages is 1
    And check that number of error messages is 0
    And check that number of total messages is 1
    And check that uptime for metrics-test pod is valid
    And check that startdate for metrics-test pod is valid

    When save time before request for integration metrics-test
    And invoke post request to webhook in integration metrics-test with token test-webhook and body {"first_name":"John","company":"Red Hat still incorrect"}
    And validate that logs of integration "metrics-test" contains string "Body: [[{"first_name":"John","company":"Red Hat still incorrect"}]]"
    And save time after request for integration metrics-test
    And sleep for "3000" ms
    Then check that number of total error is 0
    And check that last processed date is valid
    And check that number of valid messages is 2
    And check that number of error messages is 0
    And check that number of total messages is 2
    And check that uptime for metrics-test pod is valid
    And check that startdate for metrics-test pod is valid

    When save time before request for integration metrics-test
    And invoke post request to webhook in integration metrics-test with token test-webhook and body {"first_name":"John","company":"Red Hat"}
    And validate that logs of integration "metrics-test" contains string "Body: [[{"first_name":"John","company":"Red Hat"}]]"
    And save time after request for integration metrics-test
    And sleep for "3000" ms
    Then check that number of total error is 0
    And check that last processed date is valid
    And check that number of valid messages is 3
    And check that number of error messages is 0
    And check that number of total messages is 3
    And check that uptime for metrics-test pod is valid
    And check that startdate for metrics-test pod is valid

  @metrics-error
  Scenario: Check error
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
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Done" button

    And add integration step on position "0"
    And select "Log" integration step
    And fill in values by element data-testid
      | contextloggingenabled | false               |
      | bodyloggingenabled    | true                |
      | customtext            | before basic filter |
    And click on the "Done" button

    # add advanced filter step
    And add integration step on position "1"
    And select "Advanced Filter" integration step
    Then check visibility of "Advanced Filter" step configuration page
    When fill in the configuration page for "Advanced Filter" step with "${body.companyINCORRECT} not contains 'incorrect'" parameter
    And click on the "Done" button

    And add integration step on position "2"
    And select "Log" integration step
    And fill in values by element data-testid
      | contextloggingenabled | false              |
      | bodyloggingenabled    | true               |
      | customtext            | before data mapper |
    And click on the "Done" button

    # add data mapper
    And add integration step on position "3"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | first_name | first_name |
      | company    | company    |
    And click on the "Done" button

    And publish integration
    And set integration name "metrics-error"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "metrics-error" gets into "Running" state

    And select the "metrics-error" integration
    And click on the "Metrics" tab
    Then check that number of total error is 0
    And check that number of valid messages is 0
    And check that number of error messages is 0
    And check that number of total messages is 0
    And check that uptime for metrics-error pod is valid
    And check that startdate for metrics-error pod is valid

    When save time before request for integration metrics-error
    And invoke post request to webhook in integration metrics-error with token test-webhook and body {"first_name":"John","company":"Red Hat"}
    And validate that logs of integration "metrics-error" contains string "Body: [[{"first_name":"John","company":"Red Hat"}]]"
    And save time after request for integration metrics-error
    And sleep for "3000" ms
    Then check that number of total error is 0
    And check that last processed date is valid
    And check that number of valid messages is 1
    And check that number of error messages is 0
    And check that number of total messages is 1
    And check that uptime for metrics-error pod is valid
    And check that startdate for metrics-error pod is valid
