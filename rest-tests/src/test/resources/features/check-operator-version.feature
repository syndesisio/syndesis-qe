# @sustainer: acadova@redhat.com

@rest
@check-operator-version
Feature: check operator version

  Background: Clean application state
    Given clean application state

  Scenario: check version information
    Then verify whether operator metrics endpoint is active
    And verify whether operator metrics endpoint includes version information
    
