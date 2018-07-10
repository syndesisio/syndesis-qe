# @sustainer: mastepan@redhat.com

@wip
Feature: Integration - Monitoring

  Background: Clean application state
    Given log into the Syndesis
    And remove all records from table "todo"
    And remove all records from table "contact"
    When inserts into "contact" table
      | Josef_monitoring | Stieranka | Istrochem | db |
    Given db to db "CRUD2-read-create E2E" integration with period 5000 ms

  @integration-monitoring-activity
  Scenario: Activity log
    Then navigate to the "Integrations" page
    And select the "CRUD2-read-create E2E" integration
    And check visibility of "CRUD2-read-create E2E" integration details
    Then click on the "Activity" tab
    And expands "first" activity row
    Then click on the "View" Button of "Data Mapper" activity step
    Then check visibility of the activity log
    And validates that this log represents rest endpoint log of "CRUD2-read-create E2E" and "Data Mapper" activity step
    Then selects the "28-05-2016" till "28-03-2017" dates in calendar
