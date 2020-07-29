# @sustainer: tplevko@redhat.com

@rest
@database
@delorean
Feature: Integration - Database

  Background: Create sample integration
    Given clean application state
    And remove all records from table "TODO"
    And remove all records from table "CONTACT"

  @smoke
  Scenario: Smoke - Periodic invocation to Insert
    Then insert into "CONTACT" table
      | Josef_first  | Stieranka_first  | Syndesis-qe | db |
      | Josef_second | Stieranka_second | Syndesis-qe | db |

    When create start DB periodic sql invocation action step with query "SELECT * FROM CONTACT" and period 5000 ms
    And add a split step
    And create basic filter step for "last_name" with word "first" and operation "contains"
    And start mapper definition with name: "mapping 1"
    And MAP using Step 2 and field "/first_name" to "/<>/task"

    And create finish DB invoke sql action step with query "INSERT INTO TODO (task, completed) VALUES (:#task, 3)"
    Then create integration with name: "DB to DB smoke rest test"
    Then wait for integration with name: "DB to DB smoke rest test" to become active

    And sleep for jenkins delay or 15 seconds

    Then validate that number of all todos with task "Josef_first" is greater than 0
    Then validate that number of all todos with task "Josef_second" is 0

  @integrations-db-stored-procedures
  @datamapper
  Scenario: Stored procedures
    Given execute SQL command "CREATE FUNCTION get_task(OUT id int, OUT task varchar, OUT completed integer) RETURNS SETOF record AS 'SELECT * FROM TODO;' LANGUAGE SQL;"
      And execute SQL command "CREATE FUNCTION create_contact(first_name varchar) RETURNS void AS 'INSERT INTO CONTACT(first_name) values($1);' LANGUAGE SQL;"
    When create start DB periodic stored procedure invocation action step named "get_task" and period 30000 ms
      And start mapper definition with name: "db-db"
      And MAP using Step 1 and field "/task" to "/first_name"
      And create finish DB invoke stored procedure "create_contact" action step
      And create integration with name: "db-db-stored-procedures"
    Then wait for integration with name: "db-db-stored-procedures" to become active
    When insert into "TODO" table
      | Adam |
    Then check that query "SELECT * FROM CONTACT" has some output
      And verify that contact with first name "Adam" exists in database
