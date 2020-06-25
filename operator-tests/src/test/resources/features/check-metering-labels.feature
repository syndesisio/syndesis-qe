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
    Then verify new RedHat metering labels
