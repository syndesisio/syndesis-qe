# @sustainer: tplevko@redhat.com

@rest
@timer-connector
@timer
@http
@gh-7156
Feature: Integration - Timer

  Background:
    Given clean application state
    And deploy HTTP endpoints
    And create HTTP connection

  @simple-timer
  Scenario: Simple Timer to GET
    And sleep for jenkins delay or "10" seconds
    When add "timer" endpoint with connector id "timer" and "timer-action" action and with properties:
      | action       | period |
      | timer-action | 7000   |

    And create HTTP "GET" step
    And create integration with name: "timer-to-http-1"
    And wait for integration with name: "timer-to-http-1" to become active

    Then verify that after "8" seconds there were "1" calls

  @cron-timer
  Scenario: Cron Timer to GET
    And sleep for jenkins delay or "10" seconds
    When add "timer" endpoint with connector id "timer" and "timer-chron" action and with properties:
      | action     | cron          |
      | timer-cron | 0 0/1 * * * ? |
    And create HTTP "GET" step
    And create integration with name: "cron-timer-to-http-1"
    And wait for integration with name: "cron-timer-to-http-1" to become active

    Then verify that after "60" seconds there were "1" calls
