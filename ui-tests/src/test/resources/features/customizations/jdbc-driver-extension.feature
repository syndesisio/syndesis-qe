@wip
@jbdc-driver-import
Feature: JDBC driver import

  @check-connection
  Scenario: Validate
    Given allocate new "oracle12cR1" database for "Oracle12" connection
    Given clean application state
    Given log into the Syndesis
    Given import extensions from syndesis-extensions folder
      | syndesis-library-jdbc-driver |

    And wait until "meta" pod is reloaded

    Given created connections
      | Database | Oracle12 | Oracle12 | Oracle 12 RC1 |
    And free allocated "oracle12cR1" database
