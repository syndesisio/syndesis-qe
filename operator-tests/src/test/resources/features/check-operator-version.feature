# @sustainer: acadova@redhat.com

@check-operator-version
  @prod
Feature: check operator version

  Background:
    Given clean default namespace
      And deploy Syndesis CRD
      And install cluster resources
      And grant permissions to user
      And create pull secret
      And deploy Syndesis operator

  Scenario: check version information
    When deploy Syndesis CR from file "minimal.yml"
      And wait for Syndesis to become ready
    Then verify whether operator metrics endpoint is active
      And verify whether operator metrics endpoint includes version information

