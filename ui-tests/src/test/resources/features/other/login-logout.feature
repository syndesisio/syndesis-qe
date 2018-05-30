@logout-test
Feature: Login logout

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    Given created connections
      | Twitter | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |


#
#  1. logout
#
  @logout-test-logout
  Scenario: Log out
    Then check visibility of Syndesis home page

    Then log out from Syndesis

#
#  2. logout -> login
#
  @logout-test-logout-login
  Scenario: Log out and log in
    Then check visibility of Syndesis home page

    Then log out from Syndesis

    Given log into the Syndesis after logout
    Then check visibility of Syndesis home page

    # check access after logout
    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When click on the "View" kebab menu button of "Twitter Listener"
    Then check visibility of "Twitter Listener" connection details


#
#  TODO: 3. logout -> login as a different OpeShift user without access to namespace - manual test for now
#
