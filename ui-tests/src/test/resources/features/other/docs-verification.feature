# @sustainer: acadova@redhat.com

@docs-verification

Feature: Documentation verification

  Background: Clean application state
    Given clean application state
    And log into the Syndesis

  Scenario: Version check
    When navigates to the "About" page in help menu
    Then check version in about page

    And log into the Syndesis

    When navigates to the "User Guide" page in help menu
    Then verify whether the docs has right version

