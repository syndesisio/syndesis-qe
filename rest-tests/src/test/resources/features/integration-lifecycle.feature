@wip
Feature: Integration - Lifecycle

  Background: Create sample integration
    Given clean application state
    And clean all builds
    And create DB step with query: "SELECT * FROM TODO" and interval: 4000 miliseconds
    And create finish DB invoke stored procedure "add_lead" action step

  @integrations-lifecycle
  Scenario: Draft state
    When create new integration with name: "DB to DB rest test draft" and desiredState: "Unpublished"
    Then verify there are no s2i builds running for integration: "DB to DB rest test draft"

  @integrations-lifecycle @integrations-lifecycle-activate
  Scenario: Active state
    When create new integration with name: "DB to DB rest test activate" and desiredState: "Unpublished"
    When set integration with name: "DB to DB rest test activate" to desiredState: "Published"
    Then wait for integration with name: "DB to DB rest test activate" to become active

  @integrations-lifecycle @integrations-lifecycle-act-deact
  Scenario: Activate-Deactivate switch
    When create new integration with name: "DB to DB rest test act-deact" and desiredState: "Published"
    Then wait for integration with name: "DB to DB rest test act-deact" to become active
    When set integration with name: "DB to DB rest test act-deact" to desiredState: "Unpublished"
    Then validate integration: "DB to DB rest test act-deact" pod scaled to 0
    When set integration with name: "DB to DB rest test act-deact" to desiredState: "Published"
    Then validate integration: "DB to DB rest test act-deact" pod scaled to 1

  @integrations-lifecycle @integrations-lifecycle-try-with-same-name
  Scenario: Create with same name
    When create new integration with name: "DB to DB rest same name" and desiredState: "Published"
    Then wait for integration with name: "DB to DB rest same name" to become active
    Given create DB step with query: "SELECT * FROM TODO" and interval: 4000 miliseconds
    And create finish DB invoke stored procedure "add_lead" action step
    Then try to create new integration with the same name: "DB to DB rest same name" and state: "Unpublished"

  @integrations-lifecycle @integrations-lifecycle-long
  Scenario: Activate-Deactivate switch 5 times
    When create new integration with name: "DB to DB rest test act-deact" and desiredState: "Published"
    Then wait for integration with name: "DB to DB rest test act-deact" to become active
    Then switch Inactive and Active state on integration "DB to DB rest test act-deact" for 5 times and check pods up/down
