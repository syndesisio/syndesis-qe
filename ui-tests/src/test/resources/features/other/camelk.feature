# @sustainer: mmuzikar@redhat.com
@camel-k
Feature: Camel-k Runtime

  Background:
    And clean application state
    And change runtime to camelk
    Given log into the Syndesis

  @ENTESB-11500
  @camel-k-api-client
  Scenario: Extensions shouldn't be visible
    When click on the "Customizations" link
    Then check "Extensions" link is not visible
