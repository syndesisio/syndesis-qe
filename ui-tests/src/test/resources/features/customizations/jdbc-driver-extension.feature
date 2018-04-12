@jbdc-driver-import
Feature: JDBC driver import

  @check-connection
  Scenario: Validate
    Given allocate new "oracle12cR1" database for "Oracle12" connection
    Given clean application state
    Given "Camilla" logs into the Syndesis
    Given imported extensions
      | Example JDBC Driver Library | syndesis-library-jdbc-1.0.0 |
    And she stays there for "60000" ms
    Given created connections
      | Database | Oracle12 | Oracle12 | Oracle 12 RC1 |
    And frees allocated "oracle12cR1" database