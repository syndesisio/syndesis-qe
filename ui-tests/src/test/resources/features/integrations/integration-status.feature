# @sustainer: mastepan@redhat.com

@ui
@database
@integrations-check-starting-status
Feature: Integration - DB to DB

  Background: Clean application state
    Given clean application state
    Given log into the Syndesis

  @integration-check-starting-status-on-detail-page
  Scenario: Check starting integration status


    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic stored procedure invocation" integration action
    And click on the "Done" button

    When select the "PostgresDB" connection
    And select "Invoke stored procedure" integration action
    And click on the "Done" button

    And click on the "Publish" button

    And fill Name Integration form
      | Integration Name | ASD |

    And click on the "Publish" button

    Then check starting integration status on Integration Detail page

  @integration-check-starting-status-on-integration-list
  Scenario: Check starting integration status


    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic stored procedure invocation" integration action
    And click on the "Done" button

    When select the "PostgresDB" connection
    And select "Invoke stored procedure" integration action
    And click on the "Done" button

    And click on the "Publish" button

    And fill Name Integration form
      | Integration Name | ASD |

    And click on the "Publish" button

    When navigate to the "Integrations" page

    Then check starting integration ASD status on Integrations page

  @integration-check-starting-status-on-home-page
  Scenario: Check starting integration status


    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic stored procedure invocation" integration action
    And click on the "Done" button

    When select the "PostgresDB" connection
    And select "Invoke stored procedure" integration action
    And click on the "Done" button

    And click on the "Publish" button

    And fill Name Integration form
      | Integration Name | ASD |

    And click on the "Publish" button

    When navigate to the "Home" page

    Then check starting integration ASD status on Home page
