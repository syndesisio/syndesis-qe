# @sustainer: acadova@redhat.com

@rest
@check-metering-labels
@prod
Feature: check metering labels

  Background: Clean application state
    Given clean application state

  Scenario: check new com.redhat labels
    Then verify new RedHat metering labels
