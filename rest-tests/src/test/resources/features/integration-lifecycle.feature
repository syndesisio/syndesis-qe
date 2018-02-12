Feature: integration lifecycle scenarios

  Background: Create sample integration
    Given clean application state
    And clean all builds
    And create DB step with query: "SELECT * FROM TODO" and interval: 4000 miliseconds
    And create DB insert taks step

  @integrations-lifecycle
  Scenario: lifecycle integration draft
    When create new integration with name: "DB to DB rest test draft" and desiredState: "Draft"
    Then verify there are no s2i builds running for integration: "DB to DB rest test draft"

  @integrations-lifecycle @integrations-lifecycle-activate
  Scenario: lifecycle integration activate
    When create new integration with name: "DB to DB rest test activate" and desiredState: "Draft"
    When set integration with name: "DB to DB rest test activate" to desiredState: "Active"
    Then wait for integration with name: "DB to DB rest test activate" to become active

  @integrations-lifecycle @integrations-lifecycle-act-deact
  Scenario: lifecycle integration act-deact
    When create new integration with name: "DB to DB rest test act-deact" and desiredState: "Active"
    Then wait for integration with name: "DB to DB rest test act-deact" to become active
    When set integration with name: "DB to DB rest test act-deact" to desiredState: "Inactive"
    Then validate integration: "DB to DB rest test act-deact" pod scaled to 0
    When set integration with name: "DB to DB rest test act-deact" to desiredState: "Active"
    Then validate integration: "DB to DB rest test act-deact" pod scaled to 1

  @integrations-lifecycle @integrations-lifecycle-try-with-same-name
  Scenario: lifecycle integration act-deact
    When create new integration with name: "DB to DB rest same name" and desiredState: "Active"
    Then wait for integration with name: "DB to DB rest same name" to become active
    Given create DB step with query: "SELECT * FROM TODO" and interval: 4000 miliseconds
    And create DB insert taks step
    Then try to create new integration with the same name: "DB to DB rest same name" and state: "Draft"

  @integrations-lifecycle @integrations-lifecycle-long
  Scenario: lifecycle integration act-deact 5 times
    When create new integration with name: "DB to DB rest test act-deact" and desiredState: "Active"
    Then wait for integration with name: "DB to DB rest test act-deact" to become active
    Then switch Inactive and Active state on integration "DB to DB rest test act-deact" for 5 times and check pods up/down
