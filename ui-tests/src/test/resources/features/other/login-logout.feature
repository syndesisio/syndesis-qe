@logout-test
Feature: Login logout

  Background: Clean application state
    Given clean application state
    Given "Camilla" logs into the Syndesis
    Given created connections
      | Twitter | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |


#
#  1. logout
#
  @logout-test-logout
  Scenario: Log out
    Then "Camilla" is presented with the Syndesis home page

    Then log out from Syndesis

#
#  2. logout -> login
#
  @logout-test-logout-login
  Scenario: Log out and log in
    Then "Camilla" is presented with the Syndesis home page

    Then log out from Syndesis

    Given "Camilla" logs into the Syndesis after logout
    Then "Camilla" is presented with the Syndesis home page

    # check access after logout
    When "Camilla" navigates to the "Connections" page
    Then Camilla is presented with the Syndesis page "Connections"

    When clicks on the "View" kebab menu button of "Twitter Listener"
    Then Camilla is presented with "Twitter Listener" connection details


#
#  TODO: 3. logout -> login as a different OpeShift user without access to namespace - manual test for now
#
