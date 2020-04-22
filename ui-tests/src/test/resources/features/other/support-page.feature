# @sustainer: sveres@redhat.com

@ui
@datamapper
@salesforce
@twitter
@support-page
Feature: Support page

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis
    And navigate to the "Support" page in help menu

#
#  1. version check
#
#  this is trivial issue: syndesis.version in syndesis-qe pom.xml does not match version of syndesis installed on openshift
  @prod
  @gh-5960
  @ENTESB-12367
  @support-page-version-check
  Scenario: Version
    When navigates to the "About" page in help menu
    Then check version string in about page
    And check that commit id exists in about page
    And check that build id exists in about page

#
#  2. download all diagnostic info
#
  @ENTESB-12366
  @support-page-download-diagnostic
  Scenario: Export diagnostic of all
    And download diagnostics for all integrations

#
#  3. download specific diagnostic info
#
  @oauth
  @ENTESB-12366
  @support-page-download-specific-diagnostic
  Scenario: Export diagnostic of single integration
    Given remove file "syndesis.zip" if it exists

    Given clean application state
    And log into the Syndesis
    And reset content of "todo" table
    And reset content of "CONTACT" table
    And inserts into "CONTACT" table
      | Joe | Jackson | Red Hat | db |

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    When fill in periodic query input with "SELECT * FROM CONTACT" value
    And fill in period input with "1" value
    And select "Minutes" from sql dropdown
    And click on the "Next" button

    Then check visibility of page "Choose a Finish Connection"
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |

    And click on the "Done" button

    When click on the "Save" link
    And set integration name "support-page-integration"
    And publish integration
    Then Integration "support-page-integration" is present in integrations list
    Then wait until integration "support-page-integration" gets into "Running" state

    When navigate to the "Support" page in help menu
    And download diagnostics for "support-page-integration" integration
