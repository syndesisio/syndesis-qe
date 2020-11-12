# @sustainer: avano@redhat.com

@rest
@prod
@smoke
Feature: Check productized build

  Background: Create sample integration
    Given clean application state

  @check-prod-version
  Scenario: Check artifacts in integration
    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
    And add log step
    And create integration with name: "prod-check"
    Then wait for integration with name: "prod-check" to become active
    And check that "prod-check" integration pom contains productized version in property "syndesis.version"
    And check that "prod-check" integration pom contains productized version in property "camel.version"
