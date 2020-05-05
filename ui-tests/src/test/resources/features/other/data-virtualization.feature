# @sustainer: mmajerni@redhat.com
@dv
@ui
Feature: Data Virtualization smoke tests

  Background:
    Given clean application state
    And deploy DV
    And log into the Syndesis

    And reset content of "contact" table
    And inserts into "contact" table
      | Joe | Jackson | Red Hat | db |

    And reset content of "todo" table
    And inserts into "todo" table
      | task1 |
      | task2 |
      | task3 |
      | task4 |
      | task5 |

    And navigate to the "Data" page

  @ENTESB-13150
  @dvSmoke
  Scenario: basic smoke ui test
    When click on the "Create Data Virtualization" link to create a new data virtualization
    And fill in values by element ID
      | virtname        | smokeVirtualization              |
      | virtdescription | smoke virtualization description |

    And click on the "Create" button
    And sleep for 5 seconds
    Then check that data virtualization "smokeVirtualization" is present in virtualizations list
    And check that data virtualization "smokeVirtualization" has description "smoke virtualization description"

    When edit data virtualization "smokeVirtualization"
    And click on the "Create a view" link
    And select DV connection "PostgresDB" on "create" page
    And select DV connection tables on create page
      | contact |

    And click on the "Next" link
    And fill in values by element ID
      | name        | testView             |
      | description | testView description |
    And click on the "Done" button
    And click on the "Done" button

    Then check that data virtualization view "testView" is present in view list

    When click on the "Import views" link
    And select DV connection "PostgresDB" on "import" page
    And click on the "Next" link
    And select DV connection tables on import page
      | contact |
      | todo    |
    And click on the "Done" button

    Then check that data virtualization view "contact" is present in view list
    And check that data virtualization view "todo" is present in view list

    When go to "SQL client" tab on virtualization page
    And set parameters for SQL client
      | contact | 10 | 0 |
    And click on the "Submit" button
    Then check that number of rows for query is 1 in sql client

    When set parameters for SQL client
      | todo | 10 | 2 |
    And click on the "Submit" button
    Then check that number of rows for query is 3 in sql client

    When navigate to the "Data" page
    And make action "Publish" on the virtualization "smokeVirtualization"

    And wait until virtualization "smokeVirtualization" gets into "Running" state

    And edit data virtualization "smokeVirtualization"
    And edit data virtualization view "testView"

    Then check that ddl exists and contains text "CREATE VIEW testView"
    And check that number of rows for preview is 1 at view editor page
    When create an invalid view and check that error appears

    And click on the "Done" button
    Then check that data virtualization view "testView" is invalid

    And make action "Stop" on the virtualization "smokeVirtualization"
    And wait until virtualization "smokeVirtualization" gets into "Stopped" state
    And make action "Delete" on the virtualization "smokeVirtualization"
