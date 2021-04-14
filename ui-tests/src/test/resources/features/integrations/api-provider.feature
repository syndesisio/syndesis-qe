# @sustainer: mmuzikar@redhat.com

@ui
@api-provider
@apicurio
@todo-app
@database
@datamapper
@integrations-api-provider
@long-running
Feature: API Provider Integration

  # TODO: test editing integration name (also after publishing)
  # TODO: test operation list sorting and filtering

  Background:
    Given log into the Syndesis
    And clean application state
    And truncate "todo" table


  @api-provider-create-from-spec
  @openapi
  Scenario Outline: Create API provider from <source> spec <location>

    # create integration
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select API Provider as start connection
    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create API Provider spec from <source> <location>
    And navigate to the next API Provider wizard step
    Then check visibility of page "Review API Provider Actions"
    And verify there are 5 API Provider operations defined
    #And verify 1 API Provider operations are tagged updating
    #And verify 1 API Provider operations are tagged creating
    #And verify 2 API Provider operations are tagged fetching
    #And verify 1 API Provider operations are tagged destruction
    #And verify 5 API Provider operations are tagged tasks
    #And verify there are 0 errors for API Provider operations
    # these three warnings are ok
    And verify there are 3 warnings for API Provider operations

    #When navigate to the next API Provider wizard step
    When click on the "Next" button
    And click on the "Save" link
    And set integration name "api-provider-create-from-spec-<source>"
    And click on the "Save" button

    #When finish API Provider wizard
    Then check visibility of page "Choose Operation"

    And check API Provider operation "Create new task" implementing "POST" to "/"
    And check API Provider operation "Delete task" implementing "DELETE" to "/{id}"
    And check API Provider operation "Fetch task" implementing "GET" to "/{id}"
    And check API Provider operation "List all tasks" implementing "GET" to "/"
    And check API Provider operation "Update task" implementing "PUT" to "/{id}"

    Examples:
      | source | location                             |
      | url    | todo-app                             |
      | file   | swagger/connectors/todo.json         |
      | file   | swagger/connectors/todo.swagger.yaml |
      | file   | openapi/connectors/todo.yaml         |

  @gh-6109
  @ENTESB-14058
  @api-provider-create-from-scratch
  Scenario: Create from scratch
    # create integration
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    # hacky way to reuse the existing step
    When create API Provider spec from scratch v2
    And click on the "Next" button

    When change frame to "apicurio"
    # add a simple operation
    And create a new path with link
      | syndesistestpath | false |
    And select path "/syndesistestpath"

    And create new "GET" operation
    And select operation "GET"

    And set operation summary "Operation created from scratch"
    And set operation description "Operation description"
    And set response with plus sign
      | 200 |  | true |

    And set response description "Response description" for response "200"
    And set parameters types
      | type | String  | operations | response | true | 200 |
    #Then check all for errors

    And change frame to "syndesis"
    #When click on button "Save" while in apicurio studio page
    When click on the "Save" link
    Then check visibility of page "Review API Provider Actions"
    When click on the "Next" button
    #can't fill integration name, next leads to operation list
    And click on the "Save" link
    And set integration name "TODO Integration from scratch"

    And click on the "Save" button
    And select API Provider operation flow Operation created from scratch
    # And publish integration\
    And edit integration step on position 2
    And fill in values by element data-testid
      | httpresponsecode | 200 Response description |
    And click on the "Next" button
    And click on the "Save" link
    And publish integration
    #And click on the "Save and publish" button
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration from scratch" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-from-scratch endpoint "/syndesistestpath"
    Then verify response has status 200
    # for OpenApi V2 ENTESB-14058
    # And verify response has body type "application/json"

  @api-provider-get-single
  Scenario: API Provider GET single
    When create an API Provider integration "TODO Integration get single" from file swagger/connectors/todo.json
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration get single" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-get-single endpoint "/api/1"
    Then verify response has status 200
    And verify response has body
        """
        {"id":1}
        """

  @reproducer
  @api-provider-get-non-existent
  @api-provider-error-handling
  @gh-3999
  Scenario: API Provider GET non-existent
    When create an API Provider integration "TODO Integration get non existent" from file swagger/connectors/todo.json
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo WHERE id = :#id" value
    And fill in values by element data-testid
      | raiseerroronnotfound | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And check "Done" button is "visible"
    And click on the "Done" button

    And edit integration step on position 5
    And map Api Provider errors
      | Server Error           | 418 |
      | Entity Not Found Error | 404 |
    And fill in values by element data-testid
      | returnbody | true |
    And click on the "Next" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration get non existent" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-get-non-existent endpoint "/api/14"
    Then verify response has status 404

    When execute GET on API Provider route i-todo-integration-get-non-existent endpoint "/api/e"
    Then verify response has status 418

  @reproducer
  @api-provider-get-collection
  @gh-3788
  Scenario: API Provider GET collection
    When create an API Provider integration "TODO Integration get collection" from file swagger/connectors/todo.json
    And select API Provider operation flow List all tasks
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And sleep for "2000" ms
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration get collection" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-get-collection endpoint "/api/"
    Then verify response has status 200
    And verify response has body
      """
      [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
      """


  @reproducer
  @api-provider-get-collection-empty
  @gh-3788
  @gh-5096
  Scenario: API Provider GET emptycollection
    When create an API Provider integration "TODO Integration get collection empty" from file swagger/connectors/todo.json
    And select API Provider operation flow List all tasks
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
      | task2 |

    Then wait until integration "TODO Integration get collection empty" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-get-collection-empty endpoint "/api/"
    Then verify response has body
      """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
      """

    When reset content of "todo" table
    Then wait until integration "TODO Integration get collection empty" gets into "Running" state
    Then verify response has status 200


  @api-provider-post-new
  Scenario: API Provider POST new
    When create an API Provider integration "TODO Integration post new" from file swagger/connectors/todo.json
    And select API Provider operation flow Create new task
    Then check flow title is "Create new task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration post new" gets into "Running" state
    When execute POST on API Provider route i-todo-integration-post-new endpoint "/api/" with body '{"id":1,"completed":1,"task":"task1"}'
    Then verify response has status 201
    And verify response has body
      """
        {"id":1,"completed":1,"task":"task1"}
      """
    And validate that all todos with task "task1" have value completed 1, period in ms: 2000
    And validate that number of all todos with task "task1" is 1

  @ENTESB-11675
  @api-provider-post-existing
  Scenario: API Provider POST existing
    When create an API Provider integration "TODO Integration post existing" from file swagger/connectors/todo.json
    And select API Provider operation flow Create new task
    Then check flow title is "Create new task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And fill in values by element data-testid
      | raiseerroronnotfound | true |
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | id        |
      | body.completed | completed |
      | body.task      | task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | body.id        | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And edit integration step on position 5
    And map Api Provider errors
      | Duplicate Key Error | 409 |
    And click on the "Next" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
    Then wait until integration "TODO Integration post existing" gets into "Running" state
    When execute POST on API Provider route i-todo-integration-post-existing endpoint "/api/" with body '{"id":1,"completed":1,"task":"task1"}'
    Then verify response has status 409

    And validate that all todos with task "task1" have value completed 0, period in ms: 1000
    And validate that number of all todos with task "task1" is 1

  @gh-6118
  @gh-5017
  @reproducer
  @api-provider-post-collection
  Scenario: API Provider POST collection
    When create an API Provider integration "TODO Integration post collection" from file swagger/connectors/collection.json

    And select API Provider operation flow POST multiple tasks
    Then check flow title is "POST multiple tasks"

    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    When add integration step on position "1"
    And select "Aggregate" integration step
    And click on the "Next" button

    When add integration step on position "1"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id        | id        |
      | completed | completed |
      | task      | task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "3"
    And select "Data Mapper" integration step
    And create data mapper mappings with data bucket
      | 4 - SQL Result | id       | | body.id        |
      | 2 - Request   | completed | | body.completed |
      | 2 - Request  | task       | | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
    Then wait until integration "TODO Integration post collection" gets into "Running" state
    When execute POST on API Provider route i-todo-integration-post-collection endpoint "/api/multi" with body '[{"id":2,"completed":1,"task":"task2"},{"id":3,"completed":1,"task":"task3"}]'
    Then verify response has status 200
    And verify response has body
      """
        [{"id":2,"completed":1,"task":"task2"},{"id":3,"completed":1,"task":"task3"}]
      """
    And validate that number of all todos with task "task1" is 1
    And validate that number of all todos with task "task2" is 1
    And validate that number of all todos with task "task3" is 1


  @api-provider-put
  Scenario: API Provider PUT
    When create an API Provider integration "TODO Integration put" from file swagger/connectors/todo.json
    And select API Provider operation flow Update task
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
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id  | body.id        |
      | body.completed | body.completed |
      | body.task      | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration put" gets into "Running" state
    # update existing
    When execute PUT on API Provider route i-todo-integration-put endpoint "/api/1" with body '{"completed":1,"task":"changedtask1"}'
    Then verify response has body
      """
        {"id":1,"completed":1,"task":"changedtask1"}
      """
    And validate that all todos with task "changedtask1" have value completed 1, period in ms: 1000
    And validate that number of all todos with task "task1" is 0
    And validate that number of all todos with task "task2" is 1
    And validate that number of all todos with task "changedtask1" is 1
    # insert new
    When execute PUT on API Provider route i-todo-integration-put endpoint "/api/7" with body '{"completed":1,"task":"task7"}'
    Then verify response has body
      """
        {"id":7,"completed":1,"task":"task7"}
      """
    And validate that number of all todos with task "task1" is 0
    And validate that number of all todos with task "task2" is 1
    And validate that number of all todos with task "changedtask1" is 1
    And validate that number of all todos with task "task7" is 1

  @reproducer
  @api-provider-delete
  @ENTESB-11402
  @ENTESB-11455
  Scenario: API Provider DELETE
    When create an API Provider integration "TODO Integration delete" from file swagger/connectors/todo.json
    And select API Provider operation flow Delete task
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
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration delete" gets into "Running" state
    # update existing
    When execute DELETE on API Provider route i-todo-integration-delete endpoint "/api/1"
    Then verify response has status 204
    And validate that number of all todos with task "task1" is 0
    And validate that number of all todos with task "task2" is 1


  @api-provider-export-roundtrip
  Scenario: API Provider export roundtrip
    When create an API Provider integration "TODO Integration import export" from file swagger/connectors/todo.json

    # just a simple integration
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration import export" gets into "Running" state

    When select the "TODO Integration import export" integration
    Then check visibility of "TODO Integration import export" integration details

    When export the integration

    # copied mostly from integration-import-export.feature
    And clean application state
    And log into the Syndesis
    And navigate to the "Integrations" page
    And click on the "Import" link
    Then import integration "TODO Integration import export"

    When navigate to the "Integrations" page
    Then Integration "TODO Integration import export" is present in integrations list
    And wait until integration "TODO Integration import export" gets into "Stopped" state

    When select the "TODO Integration import export" integration
    And check visibility of "Stopped" integration status on Integration Detail page
    And sleep for jenkins delay or 3 seconds
    And start integration "TODO Integration import export"
    And navigate to the "Integrations" page
    And sleep for "20000" ms
    Then wait until integration "TODO Integration import export" gets into "Running" state

    When execute GET on API Provider route i-todo-integration-import-export endpoint "/api/1"
    Then verify response has body
      """
        {"id":1}
      """

  @api-provider-not-visible-in-connections
  Scenario: API Provider not visible in connections
    When navigate to the "Connections" page
    Then check that "API Provider" connection is not visible
    When click on the "Create Connection" link
    Then check that connections list does not contain "API Provider" connection

  @api-provider-openapi-modification
  @api-provider-openapi-add-operation
  Scenario: API Provider Edit OpenAPI - add operation
    When create an API Provider integration "TODO Integration add operation" from file swagger/connectors/todo.json

    # implement an existing operation
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button
    And click on the "Save" link
    And sleep for jenkins delay or 5 seconds
    And check "Save" button is "visible"
    And sleep for jenkins delay or 5 seconds

    # create new operation in apicurio
    And edit API Provider OpenAPI specification
    And change frame to "apicurio"
    And create a new path with link
      | v2/{id} | false |
    And select path "/v2/{id}"

    And create path parameter "id"
    And set description "id of task" for path parameter "id"

    And set path parameter type "Integer" for path parameter "id"
    And set path parameter type as "Integer" for path parameter "id"

    And create new "GET" operation
    And select operation "GET"

    And set operation summary "v2 GET by id"
    And set operation description "Operation added by editing OpenAPI"
    And set response with plus sign
      | 200 |  | true |

    And set response description "Returning task" for response "200"
    And set parameters types
      | type | Task  | operations | response | true | 200 |

    #Then check all for errors

    When change frame to "syndesis"
    And click on the "Save" link
    And click on the "Next" button
    And go to API Provider operation list

    And select API Provider operation flow v2 GET by id
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id   |
      | parameters.id | body.task |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button
    And click on the "Save" link

    And set integration name "TODO Integration add operation"
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration add operation" gets into "Running" state

    When execute GET on API Provider route i-todo-integration-add-operation endpoint "/api/1"
    Then verify response has body
      """
        {"id":1}
      """
    When execute GET on API Provider route i-todo-integration-add-operation endpoint "/api/v2/42"
    Then verify response has body
      """
        {"id":42,"task":"42"}
      """


  @gh-5332
  @api-provider-openapi-modification
  @api-provider-openapi-edit-unimplemented
  Scenario: API Provider Edit OpenAPI - edit unimplemented operation
    When create an API Provider integration "TODO Integration edit unimplemented" from file swagger/connectors/todo.json

    And edit API Provider OpenAPI specification
    And change frame to "apicurio"
    And select path "/{id}"
    And select operation "GET"

    And set operation summary "Fetch task edited"
    And set operation description "Fetch task edited"

    And set parameters types
      | type | Array  | operations | response | true | 200 |
      | of | Task  | operations | response | true | 200 |

    # Then check all for errors
    When change frame to "syndesis"
    And click on the "Save" link
    And click on the "Next" button
    And click on the "New integration" link

    # implement an existing operation
    And select API Provider operation flow Fetch task edited
    Then check flow title is "Fetch task edited"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings with data bucket
      | 2 - SQL Result | id        | | body.id        |
      | 2 - SQL Result | completed | | body.completed |
      | 2 - SQL Result | task      | | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "TODO Integration edit unimplemented"
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration edit unimplemented" gets into "Running" state

    When execute GET on API Provider route i-todo-integration-edit-unimplemented endpoint "/api/1"
    Then verify response has body
      """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
      """
    When execute GET on API Provider route i-todo-integration-edit-unimplemented endpoint "/api/42"
    Then verify response has body
      """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
      """

  @gh-5332
  @gh-6099
  @ENTESB-11787
  @api-provider-openapi-modification
  @api-provider-openapi-edit-implemented
  Scenario: API Provider Edit OpenAPI - edit implemented operation
    When create an API Provider integration "TODO Integration edit implemented" from file swagger/connectors/todo.json
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo where id = :#id" value
    And click on the "Next" button

    # add mapping for parameter
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | id |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id   | body.id   |
#     | completed | body.completed | completed can either be null or false
      | task | body.task |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And insert into "todo" table
      | task1 |
      | task2 |
    And navigate to the "Integrations" page
    # sanity check that the operation works as expected
    Then wait until integration "TODO Integration edit implemented" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-edit-implemented endpoint "/api/1"
    Then verify response has body
      """
        {"id":1,"task":"task1"}
      """
    When execute GET on API Provider route i-todo-integration-edit-implemented endpoint "/api/2"
    Then verify response has body
      """
        {"id":2,"task":"task2"}
      """

    # go back to the integration and edit

    When select the "TODO Integration edit implemented" integration
    And edit integration

    And edit API Provider OpenAPI specification
    And change frame to "apicurio"
    And select path "/{id}"
    And select operation "GET"

    And set operation summary "Fetch task edited"
    And set operation description "Fetch task edited"

    And set parameters types
      | type | Array  | operations | response | true | 200 |
      | of   | Task   | operations | response | true | 200 |

    And change frame to "syndesis"

    #Then check all for errors
    When click on the "Save" link
    And click on the "Next" button
    And click on the "New integration" link
    Then check visibility of page "Choose Operation"

    # edit the flow
    And select API Provider operation flow Fetch task edited
    Then check flow title is "Fetch task edited"

    When edit integration step on position 3
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Next" button

    # step indexes are weird
    And delete step on position 4

    And sleep for jenkins delay or 2 seconds
    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings with data bucket
      | 3 - SQL Result | id | | body.id |
#     | completed | body.completed | completed can either be null or false
      | 3 - SQL Result | task | | body.task |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And delete step on position 2

    And click on the "Save" link
    And set integration name "TODO Integration edit implemented"
    And publish integration
    And navigate to the "Integrations" page
    And sleep for "20000" ms
    Then wait until integration "TODO Integration edit implemented" gets into "Running" state

    When execute GET on API Provider route i-todo-integration-edit-implemented endpoint "/api/1"
    Then verify response has body
      """
        [{"id":1,"task":"task1"},{"id":2,"task":"task2"}]
      """
    When execute GET on API Provider route i-todo-integration-edit-implemented endpoint "/api/2"
    Then verify response has body
      """
        [{"id":1,"task":"task1"},{"id":2,"task":"task2"}]
      """


  @gh-6099
  @gh-5332
  @api-provider-openapi-modification
  @api-provider-openapi-delete-implemented
  Scenario: API Provider Edit OpenAPI - delete implemented operation
    When create an API Provider integration "TODO Integration delete implemented" from file swagger/connectors/todo.json

    # implement an existing operation
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or 2 seconds
    And sleep for "2000" ms
    And check "Done" button is "visible"
    And click on the "Done" button

    # implement another operation (which we will keep in the integration after deleting the first one)
    And go to the List all tasks API Provider operation
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for "2000" ms
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    And insert into "todo" table
      | task1 |
      | task2 |
    Then wait until integration "TODO Integration delete implemented" gets into "Running" state

    # just a sanity check that it's really implemented
    When execute GET on API Provider route i-todo-integration-delete-implemented endpoint "/api/1"
    Then verify response has body
      """
        {"id":1}
      """
    When execute GET on API Provider route i-todo-integration-delete-implemented endpoint "/api/"
    Then verify response has body
      """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
      """

    # now edit the api
    When select the "TODO Integration delete implemented" integration
    And edit integration

    And edit API Provider OpenAPI specification
    And change frame to "apicurio"
    And select path "/{id}"
    And select operation "GET"
    And delete "GET" operation
    #Then check all for errors
    And change frame to "syndesis"
    And click on the "Save" link
    And click on the "Next" button
    And go to API Provider operation list
    Then check visibility of page "Choose Operation"

    When click on the "Save" link
    And set integration name "TODO Integration delete implemented"
    And publish integration
    And navigate to the "Integrations" page
    And sleep for "20000" ms
    Then wait until integration "TODO Integration delete implemented" gets into "Running" state

    # verify the deleted operation returns 404, the kept one keeps working
    When execute GET on API Provider route i-todo-integration-delete-implemented endpoint "/api/1"
    Then verify response has status 404
    When execute GET on API Provider route i-todo-integration-delete-implemented endpoint "/api/"
    Then verify response has body
      """
        [{"id":1,"completed":null,"task":"task1"},{"id":2,"completed":null,"task":"task2"}]
      """


  @gh-5332
  @api-provider-openapi-modification
  @api-provider-openapi-delete-unimplemented
  Scenario: API Provider Edit OpenAPI - delete unimplemented operation
    When create an API Provider integration "TODO Integration delete unimplemented" from file swagger/connectors/todo.json
    And edit API Provider OpenAPI specification

    And change frame to "apicurio"
    And select path "/{id}"
    And select operation "GET"
    And delete "GET" operation
    #Then check all for errors
    And change frame to "syndesis"
    And click on the "Save" link
    And click on the "Next" button

    And go to API Provider operation list

    Then check "Fetch task" operation is not present in API Provider operation list


  @gh-4976
  @reproducer
  @api-provider-empty-integration
  @api-provider-prevent-publishing-empty-integration-from-scratch
  Scenario: Publishing an integration without any operation should not be possible
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create API Provider spec from scratch v2
    And click on the "Next" button

    And change frame to "apicurio"
    And create a new path with link
      | noop | true |
    And change frame to "syndesis"
    And click on the "Save" link

    Then check visibility of page "Review API Provider Actions"
    And check "Next" button is "visible"
    And check "Next" button is "disabled"


  @gh-6101
  @gh-4977
  @reproducer
  @api-provider-empty-integration
  @api-provider-prevent-publishing-empty-integration-by-deleting-all
  Scenario: Publishing an integration with all operations deleted should not be possible
    When create an API Provider integration "Empty Integration" from file swagger/connectors/todo.json
    And edit API Provider OpenAPI specification

    And change frame to "apicurio"
    And select path "/{id}"
    And select operation "GET"
    And delete "GET" operation

    And select path "/{id}"
    And select operation "PUT"
    And delete "PUT" operation

    And select path "/{id}"
    And select operation "DELETE"
    And delete "DELETE" operation

    And select path "/"
    And select operation "GET"
    And delete "GET" operation

    And select path "/"
    And select operation "POST"
    And delete "POST" operation

    #Then check all for errors
    And change frame to "syndesis"
    And click on the "Save" link
    Then verify there are 1 errors for API Provider operations

    Then check "Next" button is "visible"
    Then check "Next" button is "disabled"


  ###
  # ad-hoc tests for randomly found issues
  ###

  @api-provider-save-progress
  Scenario: Clicking Go To Operation List does not discard progress in API Provider
    When create an API Provider integration "TODO Integration save progress" from file swagger/connectors/todo.json

    # just a simple integration
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And go to API Provider operation list
    Then check API Provider operation "Fetch task" implementing "GET" to "/{id}"

    When select API Provider operation flow Fetch task
    Then check there are 3 integration steps


  @api-provider-back-button-from-scratch
  Scenario: Clicking back button from Apicurio should work consistently in API Provider
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select API Provider as start connection
    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create API Provider spec from scratch v2
    And click on the "Next" button
    And go back in browser history
    Then check visibility of page "Upload API Provider Specification"

  @reproducer
  @gh-4031
  @ENTESB-12379
  @api-provider-simple-response-type
  Scenario: API Provider operation with simple return type
    When click on the "Create Integration" link to create a new integration.
    And select the "API Provider" connection
    And create API Provider spec from file swagger/connectors/simple-string.json
    And navigate to the next API Provider wizard step
    Then check visibility of page "Review API Provider Actions"
    And verify there are 1 API Provider operations defined
    And verify there are 0 errors for API Provider operations
    When click on the "Next" button

    When select API Provider operation flow Get string
    Then check flow title is "Get string"

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "Simple API Provider Integration"
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "Simple API Provider Integration" gets into "Running" state
    When execute GET on API Provider route i-simple-api-provider-integration endpoint "/api/1"
    Then verify response has status 200
    And verify response has body type "text/plain"


  @reproducer
  @gh-4615
  @api-provider-step-not-deletable
  Scenario: API Provider start and stop connection cannot be deleted
    When create an API Provider integration "TODO Integration not deletable" from file swagger/connectors/todo.json

    And select API Provider operation flow Fetch task

    # we add a random step here so that we can find its trash icon,
    # otherwise the test could pass if the trash icon changed and we just couldn't find it

    And add integration step on position "0"
    And select "Log" integration step
    And click on the "Done" button

    Then verify delete button on step 1 is not visible
    And verify delete button on step 2 is visible
    And verify delete button on step 3 is not visible


  @reproducer
  @gh-4471
  @api-provider-base-path-in-url
  Scenario: Base path in API Provider URL
    When create an API Provider integration "TODO Integration base path" from file swagger/connectors/todo.json
    And select API Provider operation flow Fetch task
    Then check flow title is "Fetch task"
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | parameters.id | body.id |
    And sleep for jenkins delay or 2 seconds
    And click on the "Done" button
    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration base path" gets into "Running" state
    When select the "TODO Integration base path" integration
    Then verify the displayed API Provider URL matches regex ^https://i-todo-integration-base-path-syndesis.*/api$

  @conditional-flow
  @api-provider-conditional-flow
  Scenario: Conditional flows used in API Provider
    When create an API Provider integration "conditional-provider" from file swagger/connectors/todo.json
    And select API Provider operation flow Create new task
    Then check flow title is "Create new task"
    When add integration step on position "0"
    And select "Conditional Flows" integration step
    And select "Advanced expression builder" integration action

    And fill in values by element data-testid
      | flowconditions-0-condition | ${body.body.completed} == -1 |
      | usedefaultflow             | true                         |
    And click on the "Next" button
    And click on the "Next" button

    When configure condition on position 1
    And add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "DELETE FROM TODO WHERE task = :#task" value
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Data Mapper" connection
    And create data mapper mappings
      | body.task | task |
    And click on the "Done" button
    And return to primary flow from integration flow from dropdown

    When configure condition on position 2
    And add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO TODO (task, completed) VALUES (:#task, :#completed)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Data Mapper" connection
    And create data mapper mappings
      | body.task      | task      |
      | body.completed | completed |
    And click on the "Done" button
    And return to primary flow from integration flow from dropdown

    #Adding a constant response for validating the requests later
    #The tests don't care about the response body, only the behavior of Conditional flows
    And add integration step on position "1"
    And select the "Data Mapper" connection
    And define constant "1" of type "Integer" in data mapper
    And create data mapper mappings
      | 1 | body.id |
    And click on the "Done" button

    And click on the "Save" link
    And publish integration
    And navigate to the "Integrations" page
    Then wait until integration "conditional-provider" gets into "Running" state
    When select the "conditional-provider" integration

    #The body is checked only for the step to pass, more important checks are validating the number of TODOs in the table
    When execute POST on API Provider route i-conditional-provider endpoint "/api" with body '{"completed":1,"task":"task7", "id": 1}'
    Then verify response has status 201
    And verify response has body
    """
      {"id":1}
    """
    And validate that number of all todos with task "task7" is 1
    When execute POST on API Provider route i-conditional-provider endpoint "/api" with body '{"completed":-1,"task":"task7", "id": 1}'
    Then verify response has status 201
    And validate that number of all todos with task "task7" is 0

  @reproducer
  @gh-6230
  Scenario: Check that API Provider shows the number of flow instead of steps for random flow
    When create an API Provider integration "TODO Integration" from file swagger/connectors/todo.json
    And select API Provider operation flow List all tasks
    Then check flow title is "List all tasks"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "SELECT * FROM todo" value
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | id        | body.id        |
      | completed | body.completed |
      | task      | body.task      |
    And sleep for jenkins delay or 2 seconds
    And check "Done" button is "visible"
    And click on the "Done" button
    And click on the "Save" link
    And save and cancel integration editor
    And navigate to the "Integrations" page
    And select the "TODO Integration" integration
    Then verify there are 5 flows in the integration

  @api-provider-finish-replace
  Scenario: API Provider Finish step should not be replaceable
    When create an API Provider integration "TODO Integration post existing" from file swagger/connectors/todo.json
    And select API Provider operation flow Create new task
    Then check flow title is "Create new task"

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (id, completed, task) VALUES (:#id, :#completed, :#task)" value
    And click on the "Next" button

    And edit integration step on position 3
    And click on the "Cancel" link
    Then check visibility of page "Add to integration"
