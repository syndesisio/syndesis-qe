# @sustainer: acadova@redhat.com

@check-metering-labels
@prod
Feature: check metering labels

  Background:
    Given clean default namespace
      And deploy Syndesis CRD
      And install cluster resources
      And grant permissions to user
      And create pull secret
      And deploy Syndesis operator

  Scenario: check new com.redhat labels
    When deploy Syndesis CR
      And wait for Syndesis to become ready
    Then check that metering labels have correct values for "DB"
    And check that metering labels have correct values for "OAUTH"
    And check that metering labels have correct values for "PROMETHEUS"
    And check that metering labels have correct values for "SERVER"
    And check that metering labels have correct values for "META"
    And check that metering labels have correct values for "OPERATOR"
    And check that metering labels have correct values for "JAEGER"
