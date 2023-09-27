# @sustainer: mkralik@redhat.com
@ui
@google-calendar
@settings-oauth-test
@oauth
Feature: Settings OAuth

  Background: Clean application state
    Given log into the Syndesis
    And clean application state
    And navigate to the "Settings" page

  @settings-oauth-fill
  Scenario: Fill OAuth settings and check immediately
    When fill "Google Calendar" oauth settings "QE Google Calendar"
    Then check that given "Google Calendar" oauth settings are filled in

  @settings-oauth-fill
  Scenario: Fill OAuth settings and check after changing page
    When fill "Google Calendar" oauth settings "QE Google Calendar"
    And navigate to the "Home" page
    And navigate to the "Settings" page
    Then check that given "Google Calendar" oauth settings are filled in
#
  @settings-oauth-remove-immediately
  Scenario: Fill OAuth settings, remove and check immediately
    When fill "Google Calendar" oauth settings "QE Google Calendar"
    And navigate to the "Home" page
    And navigate to the "Settings" page
    Then check that given "Google Calendar" oauth settings are filled in
#    And check button "Remove" of item "Google Calendar"
    When remove information about OAuth "Google Calendar"
#    this delay is necessary due to different net delays, to stabilize test
    And sleep for "3000" ms
  #  Then check visibility of "Delete Successful Settings successfully deleted." in alert-success notification
    And check that given "Google Calendar" oauth settings are not filled in

  @settings-oauth-remove
  Scenario: Fill OAuth settings, remove and check after changing page
    When fill "Google Calendar" oauth settings "QE Google Calendar"
    And navigate to the "Home" page
    And navigate to the "Settings" page
    Then check that given "Google Calendar" oauth settings are filled in
 #   And check button "Remove" of item "Google Calendar"
    When remove information about OAuth "Google Calendar"
    #Then check visibility of "Delete Successful Settings successfully deleted." in alert-success notification
    When navigate to the "Home" page
    And navigate to the "Settings" page
    Then check that given "Google Calendar" oauth settings are not filled in

  @reproducer
  @settings-oauth-validate
  @gh-4021
  @gh-5891
  Scenario Outline: Fill <type> OAuth settings, create connection and validate
    When fill "<type>" oauth settings "<account>"
    And navigate to the "Connections" page
    And create connection "<type>" with name "My <type> connection" using oauth
    And remove all "all" alerts
    Then validate oauth connection "My <type> connection" by clicking Validate button
    Examples:
      | type            | account            |
      | Gmail           | QE Google Mail     |
      | Google Calendar | QE Google Calendar |
#      | Twitter         | twitter_talky      |
