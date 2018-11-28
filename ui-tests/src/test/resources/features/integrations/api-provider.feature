# @sustainer: asmigala@redhat.com

@api-provider
Feature: API Provider Integration

  Background:
    Given log into the Syndesis
    And clean application state
    And truncate "todo" table

  @create-api-provider-from-spec
  Scenario Outline: Create API provider from <source> spec <location>

    # create integration
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select API Provider as start connection
    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create api provider spec from <source> <location>
    And navigate to the next API Provider wizard step
    Then check visibility of page "Review API Provider Actions"
    And verify there are 5 operations defined
    And verify 1 operations are tagged updating
    And verify 1 operations are tagged creating
    And verify 2 operations are tagged fetching
    And verify 1 operations are tagged destruction
    And verify 5 operations are tagged tasks
    And verify there are 0 errors
    # these three warnings are ok
    And verify there are 3 warnings

    When navigate to the next API Provider wizard step
    Then check visibility of page "Name API Provider Integration"
    And fill in integration name "Todo API Provider Integration"

    When finish API Provider wizard
    Then check visibility of page "Choose Operation"
    And check operation "Create new task" implementing "POST /" with status "501 Not Implemented"
    And check operation "Delete task" implementing "DELETE /{id}" with status "501 Not Implemented"
    And check operation "Fetch task" implementing "GET /{id}" with status "501 Not Implemented"
    And check operation "List all tasks" implementing "GET /" with status "501 Not Implemented"
    And check operation "Update task" implementing "PUT /{id}" with status "501 Not Implemented"

    Examples:
      | source | location                             |
      | url    | todo-app                             |
      | file   | swagger/connectors/todo.json         |
      | file   | swagger/connectors/todo.swagger.yaml |


  # TODO: expand once apicurio framework is done
  @create-api-provider-from-scratch
  Scenario: Create from scratch
    # create integration
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    # hacky way to reuse the existing step
    When create api provider spec from scratch .
    And click on the "Next" button
    And create an API Provider operation in apicurio
    And click on button "Save" while in apicurio studio page
    Then check visibility of page "Review API Provider Actions"
    When click on the "Next" button
    And fill in integration name "TODO Integration"

    And click on the "Save and continue" button
    And select operation Receiving GET request on /syndesistestpath
    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/syndesistestpath" returns status 200 and body
        """
        """


  @api-provider-get-single
  Scenario: API Provider GET single
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation Fetch task
    Then check flow title is "Fetch task"

    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        {"id":1}
        """

  @api-provider-get-non-existent
  Scenario: API Provider GET non-existent
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation Fetch task
    Then check flow title is "Fetch task"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo WHERE id = :#id" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And click on the "Done" button

    And add integration "step" on position "2"
    And select "Data Mapper" integration step
    And open data bucket "3 - SQL Result"
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/14" returns status 404 and body
        """
        """

  @api-provider-get-collection
  Scenario: API Provider GET collection
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation List all tasks
    Then check flow title is "List all tasks"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/" returns status 200 and body
        """
        [{"id":1,"completed":0,"task":"task1"},{"id":2,"completed":0,"task":"task2"}]
        """

  @api-provider-get-collection-empty
  Scenario: API Provider GET emptycollection
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation List all tasks
    Then check flow title is "List all tasks"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id           | body.id        |
      | completed;id | body.completed |
      | task         | body.task      |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/" returns status 200 and body
        """
        []
        """

  @api-provider-post-new
  Scenario: API Provider POST new
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation Create new task
    Then check flow title is "Create new task"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And click on the "Done" button

    And add integration "step" on position "2"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing POST on route i-todo-integration endpoint "/api/" with request '{"id":1,"completed":1,"task":"task1"}' returns status 201 and body
        """
        {"id":1,"completed":1,"task":"task1"}
        """
    And validate that all todos with task "task1" have value completed "1", period in ms: "1000"
    And validate that number of all todos with task "task1" is "1", period in ms: "1000"

  @api-provider-post-existing
  Scenario: API Provider POST existing
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation Create new task
    Then check flow title is "Create new task"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And click on the "Done" button

    And add integration "step" on position "2"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing POST on route i-todo-integration endpoint "/api/" with request '{"id":1,"completed":1,"task":"task1"}' returns status 500 and body
        """
        """
    And validate that all todos with task "task1" have value completed "0", period in ms: "1000"
    And validate that number of all todos with task "task1" is "1", period in ms: "1000"

  @api-provider-put
  Scenario: API Provider PUT
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation Update task
    Then check flow title is "Update task"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo(id, completed, task) VALUES (:#id, :#completed, :#task) ON CONFLICT (id) DO UPDATE SET completed=:#completed, task=:#task" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id  | id        |
      | body.completed | completed |
      | body.task      | task      |
    And click on the "Done" button

    And add integration "step" on position "2"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | parameters.id  | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration" gets into "Running" state
    # update existing
    And verify that executing PUT on route i-todo-integration endpoint "/api/1" with request '{"completed":1,"task":"changedtask1"}' returns status 200 and body
        """
        {"id":1,"completed":1,"task":"changedtask1"}
        """
    And validate that all todos with task "changedtask1" have value completed "1", period in ms: "1000"
    And validate that number of all todos with task "task1" is "0", period in ms: "1000"
    And validate that number of all todos with task "task2" is "1", period in ms: "1000"
    And validate that number of all todos with task "changedtask1" is "1", period in ms: "1000"
    # insert new
    And verify that executing PUT on route i-todo-integration endpoint "/api/7" with request '{"completed":1,"task":"task7"}' returns status 200 and body
        """
        {"id":7,"completed":1,"task":"task7"}
        """
    And validate that number of all todos with task "task1" is "0", period in ms: "1000"
    And validate that number of all todos with task "task2" is "1", period in ms: "1000"
    And validate that number of all todos with task "changedtask1" is "1", period in ms: "1000"
    And validate that number of all todos with task "task7" is "1", period in ms: "1000"


  @api-provider-delete
  Scenario: API Provider DELETE
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation Delete task
    And check flow title is "Delete task"

    When click on the "Add a Connection" button
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "DELETE FROM todo where id = :#id" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration" gets into "Running" state
    # update existing
    And verify that executing DELETE on route i-todo-integration endpoint "/api/1" returns status 204 and body
      """
      """
    And validate that number of all todos with task "task1" is "0", period in ms: "1000"
    And validate that number of all todos with task "task2" is "1", period in ms: "1000"

  @api-provider-export-roundtrip
  Scenario: API Provider export roundtrip
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    # just a simple integration
    And select operation Fetch task
    Then check flow title is "Fetch task"
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And click on the "Done" button
    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state

    When select the "TODO Integration" integration
    Then check visibility of "TODO Integration" integration details

    When export the integration

    # copied mostly from integration-import-export.feature
    And clean application state
    And log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" button
    Then import integration "TODO Integration"

    When navigate to the "Integrations" page
    Then Integration "TODO Integration" is present in integrations list
    And wait until integration "TODO Integration" gets into "Stopped" state

    When select the "TODO Integration" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    And sleep for jenkins delay or "3" seconds
    And start integration "TODO Integration"
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state

    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        {"id":1}
        """


  @api-provider-not-visible-in-connections
  Scenario: API Provider not visible in connections
    When navigate to the "Connections" page
    Then check that "API Provider" connection is not visible
    When click on the "Create Connection" button
    Then check that connections list does not contain "API Provider" connection

  @api-provider-check-tech-preview
  Scenario: API Provider marked as tech preview
    When click on the "Create Integration" button to create a new integration.
    Then check that connection "API Provider" is marked as Tech Preview


  ###
  # ad-hoc tests for randomly found issues
  ###

  @api-provider-save-progress
  Scenario: Clicking Go To Operation List does not discard progress in API Provider
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    # just a simple integration
    And select operation Fetch task
    Then check flow title is "Fetch task"

    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And click on the "Done" button

    And click on the "Go to Operation List" button
    Then check operation "Fetch task" implementing "GET /{id}" with status "200 OK"

    When select operation Fetch task
    Then check there are 3 integration steps


  @api-provider-back-button-from-scratch
  Scenario: Clicking back button from Apicurio should work consistently in API Provider
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select API Provider as start connection
    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create api provider spec from scratch .
    And click on the "Next" button
    And go back in browser history
    Then check visibility of page "Upload API Provider Specification"


  @api-provider-simple-response-type
  Scenario: API Provider operation with simple return type
    When click on the "Create Integration" button to create a new integration.
    And select the "API Provider" connection
    And create api provider spec from file swagger/connectors/simple-string.json
    And navigate to the next API Provider wizard step
    Then check visibility of page "Review API Provider Actions"
    And verify there are 1 operations defined
    And verify there are 0 errors

    When navigate to the next API Provider wizard step
    Then check visibility of page "Name API Provider Integration"
    When fill in integration name "Simple API Provider Integration"

    When finish API Provider wizard
    And select operation Get string
    Then check flow title is "Get string"


    When add integration "step" on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body |
    And click on the "Done" button

    And click on the "Publish" button
    And navigate to the "Integrations" page
    Then wait until integration "Simple API Provider Integration" gets into "Running" state
    And verify that executing GET on route i-simple-api-provider-integration endpoint "/api/1" returns status 200 and body
        """
        1
        """


