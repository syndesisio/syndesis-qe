# @sustainer: mastepan@redhat.com

@api-connector-integration
Feature: Integration - DB to API

  Background:
    Given log into the Syndesis
    And clean application state
    And Set Todo app credentials
    And reset content of "TODO" table
    And reset content of "contact" table

    And create new API connector
      | source   | file          | swagger/connectors/todo.swagger.yaml |
      | security | authType      | HTTP Basic Authentication            |
      | details  | connectorName | Todo connector                       |
      | details  | routeHost     | todo                                 |
      | details  | baseUrl       | /api                                 |

    And created connections
      | Todo connector | todo | Todo connection | no validation |

    And navigate to the "Home" page

  @DB-custom-api-connector-integration
  Scenario: Create
    When click on the "Create Integration" button to create a new integration.
    And select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "SELECT * FROM CONTACT" value
    And fill in period input with "10" value
    And select "Seconds" from sql dropdown
    Then click on the "Done" button

    When select the "Todo connection" connection
    Then select "Create new task" integration action

    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    And create data mapper mappings
      | last_name | body.task |
    And click on the "Done" button
    And click on the "Publish" button
    And set integration name "Todo integration"
    Then click on the "Publish" button

    When navigate to the "Integrations" page
    Then wait until integration "Todo integration" gets into "Running" state

    When navigate to Todo app
    Then check Todo list grows in "15" second


  @DB-custom-api-connector-DB
  Scenario: Create
    Given invoke database query "insert into CONTACT values ('evelyn' , 'Queen', '1' , 'some lead', '1999-01-01')"
    And invoke database query "insert into TODO values (1, 'malphite' , 0)"

    When click on the "Create Integration" button to create a new integration.
    #start connector
    And select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "SELECT * FROM CONTACT WHERE first_name = 'evelyn'" value
    And fill in period input with "30" value
    And select "Seconds" from sql dropdown
    Then click on the "Done" button

    #finish connector
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "insert into contact values ('malphite', 'Jackson', :#COMPANY, 'db', '2018-03-23');" value
    Then click on the "Done" button

    #middle connector
    When click on the "Add a Connection" button
    And select the "Todo connection" connection
    Then select "Fetch task" integration action

    #start - here - middle -finish
    When add integration "step" on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | company | parameters.id |
    And click on the "Done" button

    #start - mapper - middle - here -finish
    When add integration "step" on position "2"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When open data bucket "3 - Response"
    And create data mapper mappings
      | task | COMPANY |
    And click on the "Done" button

    And click on the "Publish" button
    And set integration name "db_custom_api_db"
    Then click on the "Publish" button

    When navigate to the "Integrations" page
    Then wait until integration "db_custom_api_db" gets into "Running" state
    And check that query "select * from contact where company = 'malphite'" has some output
