# @sustainer: mmuzikar@redhat.com
@disabled
@camel-k
Feature: Camel-k Runtime

  Background:
    And clean application state
    And change runtime to camelk
    Given log into the Syndesis

  @ENTESB-11500
  @camel-k-api-client
  Scenario: Extensions shouldn't be visible
    When click on the "Customizations" link
    Then check "Extensions" link is not visible

  @camel-k-smoke
  @ENTESB-14063
  Scenario: CamelK smoke test
    Given truncate "todo" table
    And insert into "todo" table
      | task1        |
      | task2_filter |

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"

    #select DB connection as 'from' point
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in invoke query input with "SELECT * FROM todo" value
    And fill in period input with "5" value
    And select "seconds" from sql dropdown
    And click on the "Done" button
    And check that position of connection to fill is "Finish"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Done" button

    #adding split step to split the result of Db connection
    And add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    #adding mustache template to test pulling more dependencies
    And add integration step on position "1"
    And select "Template" integration step
    And set the template type to "Mustache"
    And set the template to "{{task}} passed the filter"
    And click on the "Done" button

    #add data mapper step for template
    And add integration step on position "1"
    And select "Data Mapper" integration step
    And check visibility of data mapper ui
    And create data mapper mappings
      | task | task |
    And scroll "top" "right"
    And click on the "Done" button

    And add integration step on position "1"
    And select "Advanced Filter" integration step
    Then check visibility of "Advanced Filter" step configuration page
    When fill in the configuration page for "Advanced Filter" step with "${body.task} not contains 'filter'" parameter
    And click on the "Done" button

    #finish and save integration
    When click on the "Save" link
    And set integration name "CamelK DB to log"
    And publish integration

    Then Integration "CamelK DB to log" is present in integrations list
    #wait for integration to get in active state
    And wait until integration "CamelK DB to log" gets into "Running" state

    And sleep for jenkins delay or 20 seconds
    Then validate that logs of integration "CamelK DB to log" contains string "task1"
    And validate that logs of integration "CamelK DB to log" doesn't contain string "task2"
