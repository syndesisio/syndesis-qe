# @sustainer: asmigala@redhat.com

@ui
@api-provider
@todo-app
@database
@datamapper
@integrations-api-provider
Feature: API Provider Integration

  # TODO: test editing integration name (also after publishing)
  # TODO: test operation list sorting and filtering

  Background:
    Given log into the Syndesis
    And clean application state
    And truncate "todo" table


  @api-provider-create-from-spec
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
    # TODO: remove the status (no longer shown in UI)
    And check operation "Create new task" implementing "POST" to "/" with status "501 Not Implemented"
    And check operation "Delete task" implementing "DELETE" to "/{id}" with status "501 Not Implemented"
    And check operation "Fetch task" implementing "GET" to "/{id}" with status "501 Not Implemented"
    And check operation "List all tasks" implementing "GET" to "/" with status "501 Not Implemented"
    And check operation "Update task" implementing "PUT" to "/{id}" with status "501 Not Implemented"

    Examples:
      | source | location                             |
      | url    | todo-app                             |
      | file   | swagger/connectors/todo.json         |
      | file   | swagger/connectors/todo.swagger.yaml |


  # TODO: expand once apicurio framework is done
  @api-provider-create-from-scratch
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
    And select operation flow Receiving GET request on /syndesistestpath
    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/syndesistestpath" returns status 200 and body
        """
        """


  @api-provider-get-single
  Scenario: API Provider GET single
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        {"id":1}
        """

  @reproducer
  @api-provider-get-non-existent
  @gh-3999
  Scenario: API Provider GET non-existent
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo WHERE id = :#id" value
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And open data bucket "3 - SQL Result"
    And open data mapper collection mappings
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/14" returns status 404 and body
        """
        """

  @reproducer
  @api-provider-get-collection
  @gh-3788
  Scenario: API Provider GET collection
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow List all tasks
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """

  @reproducer
  @api-provider-get-collection-empty
  @gh-3788
  Scenario: API Provider GET emptycollection
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow List all tasks
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | id           | body.id        |
      | completed;id | body.completed |
      | task         | body.task      |
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/" returns status 200 and body
        """
        []
        """

  @api-provider-post-new
  Scenario: API Provider POST new
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Create new task
    Then check flow title is "Create new task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And click on the "Done" button

    And publish API Provider integration
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
    And select operation flow Create new task
    Then check flow title is "Create new task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing POST on route i-todo-integration endpoint "/api/" with request '{"id":1,"completed":1,"task":"task1"}' returns status 500 and body
        """
        """
    And validate that all todos with task "task1" have value completed "0", period in ms: "1000"
    And validate that number of all todos with task "task1" is "1", period in ms: "1000"

  @gh-5017
  @reproducer
  @api-provider-post-collection
  Scenario: API Provider POST collection
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And edit API Provider OpenAPI specification


    # creating operation

    And create a new path with plus sign with name "multi"
    And select path "/multi"

    And create new "POST" operation
    And select operation "POST"

    And set summary "POST multiple tasks"
    And set description "POST multiple tasks"

    And set response 200 with plus sign
    And set response description "Created IDs" for response 200
    And set response type "Array" for response 200
    And set response type of "Task" for response 200

    And create request body
    And set body description "Created IDs"

    And set body response type "Array" of "Task" as ""
    And click on button "Save" while in apicurio studio page

    And select operation flow POST multiple tasks
    Then check flow title is "POST multiple tasks"

    When add integration step on position "0"
    And select "Split" integration step

    When add integration step on position "1"
    And select "Aggregate" integration step

    When add integration step on position "1"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And click on the "Done" button

    And add integration step on position "3"
    And select "Data Mapper" integration step
    And open data bucket "2 - Request"
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing POST on route i-todo-integration endpoint "/api/" with request '[{"id":2,"completed":1,"task":"task2"},{"id":3,"completed":1,"task":"task3"}]' returns status 200 and body
        """
        """
    And validate that number of all todos with task "task1" is "1", period in ms: "1000"
    And validate that number of all todos with task "task2" is "1", period in ms: "1000"
    And validate that number of all todos with task "task3" is "1", period in ms: "1000"

  @api-provider-put
  Scenario: API Provider PUT
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Update task
    Then check flow title is "Update task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo(id, completed, task) VALUES (:#id, :#completed, :#task) ON CONFLICT (id) DO UPDATE SET completed=:#completed, task=:#task" value
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id  | id        |
      | body.completed | completed |
      | body.task      | task      |
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And open data bucket "1 - Request"
    And create data mapper mappings
      | parameters.id  | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And click on the "Done" button

    And publish API Provider integration
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

  @reproducer
  @api-provider-delete
  @gh-4040
  Scenario: API Provider DELETE
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Delete task
    And check flow title is "Delete task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "DELETE FROM todo where id = :#id" value
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And click on the "Done" button

    And publish API Provider integration
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
    And select operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And click on the "Done" button
    And publish API Provider integration
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


  @api-provider-openapi-modification
  @api-provider-openapi-add-operation
  Scenario: API Provider Edit OpenAPI - add operation
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    # implement an existing operation
    And select operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button
    And click on the "Save" button

    # create new operation in apicurio
    And edit API Provider OpenAPI specification
    And create a new path with plus sign with name "v2/{id}"
    And select path "/v2/{id}"

    And create path parameter "id"
    And set description "id of task" for path parameter "id"

    And set path parameter type "Integer" for path parameter "id"
    And set path parameter type as "Integer" for path parameter "id"

    And create new "GET" operation
    And select operation "GET"

    And set summary "v2 GET by id"
    And set description "Operation added by editing OpenAPI"
    And set response 200 with plus sign

    And set response description "Returning task" for response 200
    And set response type "Task" for response 200

    Then check all for errors

    When click on button "Save" while in apicurio studio page

    And select operation flow v2 GET by id
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id   |
      | parameters.id | body.task |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button
    And click on the "Save" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state

    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        {"id":1}
        """
    And verify that executing GET on route i-todo-integration endpoint "/api/v2/42" returns status 200 and body
        """
        {"id":42,"task":"42"}
        """

  @api-provider-openapi-modification
  @api-provider-openapi-edit-unimplemented
  Scenario: API Provider Edit OpenAPI - edit unimplemented operation
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    And edit API Provider OpenAPI specification
    And select path "/{id}"
    And select operation "GET"

    And set summary "Fetch task edited"
    And set description "Fetch task edited"

    And set response type "Array" for response 200
    And set response type of "Task" for response 200

    Then check all for errors
    When click on button "Save" while in apicurio studio page
    Then check visibility of page "Choose Operation"

    # implement an existing operation
    And select operation flow Fetch task edited
    Then check flow title is "Fetch task edited"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And open data bucket "2 - SQL Result"
    And open data mapper collection mappings
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button

    And click on the "Save" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration" gets into "Running" state

    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """
    And verify that executing GET on route i-todo-integration endpoint "/api/42" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """

  @api-provider-openapi-modification
  @api-provider-openapi-edit-implemented
  Scenario: API Provider Edit OpenAPI - edit implemented operation
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Fetch task
    Then check flow title is "Fetch task"


    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo where id = :#id" value
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And open data bucket "2 - SQL Result"
    And open data mapper collection mappings
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button

    # add mapping for parameter
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button

    And click on the "Save" button

    And publish API Provider integration
    And inserts into "todo" table
      | task1 |
      | task2 |
    And navigate to the "Integrations" page
    # sanity check that the operation works as expected
    Then wait until integration "TODO Integration" gets into "Running" state
    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        {"id":1,"completed":null,"task":"task1"}
        """
    And verify that executing GET on route i-todo-integration endpoint "/api/2" returns status 200 and body
        """
        {"id":2,"completed":null,"task":"task2"}
        """

    # go back to the integration and edit

    When select the "TODO Integration" integration
    And edit integration

    And edit API Provider OpenAPI specification
    And select path "/{id}"
    And select operation "GET"

    And set summary "Fetch task edited"
    And set description "Fetch task edited"

    And set response type "Array" for response 200
    And set response type of "Task" for response 200

    Then check all for errors
    When click on button "Save" while in apicurio studio page
    Then check visibility of page "Choose Operation"

    # edit the flow
    And select operation flow Fetch task edited
    Then check flow title is "Fetch task edited"

    When edit integration step on position 5
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Next" button

    # step indexes are weird
    And delete step on position 7

    And sleep for jenkins delay or "2" seconds
    And add integration step on position "2"
    And select "Data Mapper" integration step
    And open data bucket "3 - SQL Result"
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And click on the "Done" button

    And delete step on position 3

    And click on the "Save" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state

    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """
    And verify that executing GET on route i-todo-integration endpoint "/api/42" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """

  @api-provider-openapi-modification
  @api-provider-openapi-delete-implemented
  Scenario: API Provider Edit OpenAPI - delete implemented operation
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    # implement an existing operation
    And select operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or "2" seconds
    And sleep for 2 seconds
    And click on the "Done" button
    And click on the "Save" button

    # implement another operation (which we will keep in the integration after deleting the first one)
    And go to the List all tasks operation
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for 2 seconds
    And click on the "Done" button
    And click on the "Save" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    And inserts into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration" gets into "Running" state

    # just a sanity check that it's really implemented
    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 200 and body
        """
        {"id":1}
        """
    And verify that executing GET on route i-todo-integration endpoint "/api/" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """

    # now edit the api
    When select the "TODO Integration" integration
    And edit integration

    And edit API Provider OpenAPI specification
    And select path "/{id}"
    And select operation "GET"
    And delete current operation
    Then check all for errors
    When click on button "Save" while in apicurio studio page
    Then check visibility of page "Choose Operation"

    When publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state

    # verify the deleted operation returns 404, the kept one keeps working
    And verify that executing GET on route i-todo-integration endpoint "/api/1" returns status 404 and body
        """
        """
    And verify that executing GET on route i-todo-integration endpoint "/api/" returns status 200 and body
        """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
        """


  @api-provider-openapi-modification
  @api-provider-openapi-delete-unimplemented
  Scenario: API Provider Edit OpenAPI - delete unimplemented operation
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And edit API Provider OpenAPI specification

    And select path "/{id}"
    And select operation "GET"
    And delete current operation
    Then check all for errors
    When click on button "Save" while in apicurio studio page

    Then check Fetch task operation not present in operation list


  @gh-4976
  @reproducer
  @api-provider-empty-integration
  @api-provider-prevent-publishing-empty-integration-from-scratch
  Scenario: Publishing an integration without any operation should not be possible
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create api provider spec from scratch .
    And click on the "Next" button
    And create a new path with plus sign with name "noop"
    And click on button "Save" while in apicurio studio page

    Then check visibility of page "Review API Provider Actions"
    And check "Next" button is "visible"
    And check "Next" button is "disabled"



  @gh-4977
  @reproducer
  @api-provider-empty-integration
  @api-provider-prevent-publishing-empty-integration-by-deleting-all
  Scenario: Publishing an integration with all operations deleted should not be possible
    When create an API Provider integration "Empty Integration" from file swagger/connectors/todo.json
    And edit API Provider OpenAPI specification

    And select path "/{id}"
    And select operation "GET"
    And delete current operation

    And select path "/{id}"
    And select operation "PUT"
    And delete current operation

    And select path "/{id}"
    And select operation "DELETE"
    And delete current operation

    And select path "/"
    And select operation "GET"
    And delete current operation

    And select path "/"
    And select operation "POST"
    And delete current operation

    Then check all for errors
    When click on button "Save" while in apicurio studio page

    Then check "Publish" button is "visible"
    Then check "Publish" button is "disabled"


  ###
  # ad-hoc tests for randomly found issues
  ###

  @api-provider-save-progress
  Scenario: Clicking Go To Operation List does not discard progress in API Provider
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    # just a simple integration
    And select operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And click on the "Done" button

    And go to API Provider operation list
    Then check operation "Fetch task" implementing "GET" to "/{id}" with status "200 OK"

    When select operation flow Fetch task
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
    And select operation flow Get string
    Then check flow title is "Get string"


    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body |
    And click on the "Done" button

    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "Simple API Provider Integration" gets into "Running" state
    And verify that executing GET on route i-simple-api-provider-integration endpoint "/api/1" returns status 200 and body
        """
        1
        """

  @reproducer
  @gh-4615
  @api-provider-step-not-deletable
  Scenario: API Provider start and stop connection cannot be deleted
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json

    And select operation flow Fetch task

    # we add a random step here so that we can find its trash icon,
    # otherwise the test could pass if the trash icon changed and we just couldn't find it

    And add integration step on position "0"
    And select "Log" integration step
    And click on the "Done" button

    Then verify delete button on step 1 is not visible
    And verify delete button on step 3 is visible
    And verify delete button on step 5 is not visible

  @reproducer
  @gh-4471
  @api-provider-base-path-in-url
  Scenario: Base path in API Provider URL
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or "2" seconds
    And click on the "Done" button
    And publish API Provider integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration" gets into "Running" state
    When select the "TODO Integration" integration
    Then verify the displayed URL matches regex ^https://i-todo-integration-syndesis.*/api$





