# @sustainer: mkralik@redhat.com

@rest
@publicapi
@publicapi-connections
Feature: Public API - connections point

  Background: Prepare
    Given clean application state
    And deploy public oauth proxy
    And set up ServiceAccount for Public API

  @publicapi-connection-change
  Scenario: Change connection properties
    When update properties of connection PostgresDB
      | user   | myuser    |
      | schema | sampledb2 |
    And sleep for jenkins delay or "5" seconds
    Then check that PostgresDB connection contains properties
      | user   | myuser    |
      | schema | sampledb2 |

  @publicapi-connection-change-refresh-integration
  @ENTESB-13013
  Scenario: Change connection properties for integration
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period "5000" ms
    And add log step
    And create new integration with name: "integrationConnectionUpdate" and desiredState: "Unpublished"
    And update properties of connection PostgresDB
      | user   | myuser    |
      | schema | sampledb2 |
    And sleep for jenkins delay or "5" seconds
    Then check that integration integrationConnectionUpdate contains warning "'configured-properties' is different"
    And check that integration integrationConnectionUpdate contains warning "Context: Connection('PostgresDB')"
    When update properties of connection PostgresDB and refresh integration
      | user   | myuser    |
      | schema | sampledb2 |
    And sleep for jenkins delay or "5" seconds
    Then check that integration integrationConnectionUpdate doesn't contains any warning

