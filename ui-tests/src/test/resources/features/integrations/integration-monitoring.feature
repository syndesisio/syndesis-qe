Feature: integration monitoring scenarios

  Background: Clean application state
    Given "Camilla" logs into the Syndesis
    And remove all records from DB
    When inserts into "contact" table
      | Josef_monitoring | Stieranka | Istrochem | db |
    Given db to db "CRUD2-read-create E2E" integration with period 5000 ms

  @integration-monitoring-activity
  Scenario: integration monitoring
    Then "Camilla" navigates to the "Integrations" page
    And Camilla selects the "CRUD2-read-create E2E" integration
    And Camilla is presented with "CRUD2-read-create E2E" integration details
    Then clicks on the "Activity" tab
    And expands "first" activity row
    Then clicks on the "View" Button of "Data Mapper" activity step
    Then she is presented with the activity log
    And validates that this log represents rest endpoint log of "CRUD2-read-create E2E" and "Data Mapper" activity step
    Then selects the "28-05-2016" till "28-03-2017" dates in calendar
