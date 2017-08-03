# Created by sveres at 2.8.17
@connections-kebab-test
Feature: Test to verify correct function of connections kebab menu
  https://github.com/syndesisio/syndesis-ui/issues/553
  https://app.zenhub.com/workspace/o/syndesisio/syndesis-e2e-tests/issues/19

  Background:
    Given clean application state

  Scenario: Test whether connections kebab menu works as it should
    When "Camilla" navigates to the "Connections" page
    And clicks on the kebab menu icon of each available connection
    Then she can see unveiled kebab menu of all connections, each of this menu consist of "View", "Edit" and "Delete" actions
