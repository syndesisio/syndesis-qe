# @sustainer: mkralik@redhat.com

@ui
@oauth
@logout-test
Feature: Login logout

  Background: Clean application state
    Given log into the Syndesis
    And clean application state
    When navigate to the "Settings" page
    And fill all oauth settings
    And navigate to the "Connections" page
    And create connections using oauth
      | Salesforce | Salesforce-test |


#
#  1. logout
#
  @logout-test-logout
  Scenario: Log out
    Then check visibility of Syndesis home page
    And log out from Syndesis

#
#  2. logout -> login
#
  @logout-test-logout-login
  Scenario: Log out and log in
    Then check visibility of Syndesis home page

    When log out from Syndesis
    And log into the Syndesis after logout
    Then check visibility of Syndesis home page

    # check access after logout
    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When click on the "View" kebab menu button of "Salesforce-test"
    Then check visibility of "Salesforce-test" connection details

## just commented, in case KeyCloak will be used in the future for OSD.
##  3. logout -> login with sso
##
#  @osd
#  @logout-test-logout-login-sso
#  Scenario: Log out and log in with sso
#    Then check visibility of Syndesis home page
#
#    When log out from Syndesis
#    And log into the Syndesis after logout with SSO
#    Then check visibility of Syndesis home page


#
#  TODO: 3. logout -> login as a different OpeShift user without access to namespace - manual test for now
#
