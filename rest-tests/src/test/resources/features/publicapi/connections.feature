# @sustainer: mkralik@redhat.com

@rest
@publicapi
@publicapi-connections
Feature: Public API - connections point

  Background: Prepare
    Given clean application state
    And deploy public oauth proxy
    And set up ServiceAccount for Public API

  Scenario: Change connection properties
    When update properties of connection PostgresDB
      | user   | myuser    |
      | schema | sampledb2 |
    Then check that PostgresDB connection contains properties
      | user   | myuser    |
      | schema | sampledb2 |
