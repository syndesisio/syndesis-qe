# @sustainer: mmuzikar@redhat.com

@ui
@openapi
@api-provider
@api-client
Feature: OpenApi support

  Background:
    Given clean application state
    And log into the Syndesis
    And truncate "todo" table
    And Set Todo app credentials

  #TODO: Add tests for Response references

  #Copy pasta from api-provider feature, only change is this generates openapi spec
  @openapi-create-from-scratch
  Scenario: OpenAPI Provider - Create from scratch
    # create integration
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    # hacky way to reuse the existing step
    When create API Provider spec from scratch v3
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
    And set type of "application/json" media type to "String" on property "type" for response "200"
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

    And navigate to the "Integrations" page
    Then wait until integration "TODO Integration from scratch" gets into "Running" state
    When execute GET on API Provider route i-todo-integration-from-scratch endpoint "/syndesistestpath"
    Then verify response has status 200

  @ENTESB-12737
  @openapi-client
  Scenario: Custom OpenAPI client
    When clicks on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    And upload swagger file src/test/resources/openapi/connectors/todo.json

    When click on the "Next" button
    Then check visibility of page "Review Actions"

    When click on the "Next" link
    Then check visibility of page "Specify Security"

    And click on the "Next" button
    And fill in values by element ID
      | name     | Todo connector |
      | basepath | /api           |
    And fill in TODO API host URL
    And click on the "Save" button

    When created connections
      | Todo connector | todo | Todo connection | no validation |
    And navigate to the "Integrations" page
    And click on the "Create Integration" link

    And select "Timer" integration step
    And select "Simple" integration action
    And click on the "Next" button

    And select "Log" integration step
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And adds integration step on position "0"
    And select "Todo connection" integration step
    And select "List all tasks" integration action
    And click on the "Next" button

    And click on the "Publish" link
    And set integration name "OpenAPI-client"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "OpenAPI-client" gets into "Running" state
    And insert into "TODO" table
      | Squirtle |
    And validate that logs of integration "i-openapi-client" contains string "Squirtle"

  @manual
  @openapi-provider-client-mismatch
  Scenario: OpenAPI Provider & API Client specification version mismatch
    When create an API Provider integration "OpenAPI" from file swagger/connectors/todo.json
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
    Then wait until integration "OpenAPI" gets into "Running" state
    When execute GET on API Provider route i-openapi endpoint "/api/1"
    Then verify response has status 200
    #Sanity check main test is if API client can use 3.0 specification
    # and still be able to communicate with API Provider using swagger
    And verify response has body
        """
        {"id":1}
        """

    When clicks on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    And upload swagger file src/test/resources/openapi/connectors/todo.json

    When click on the "Next" button
    Then check visibility of page "Review Actions"

    When click on the "Next" link
    Then check visibility of page "Specify Security"

    And click on the "Next" button
    And fill in values by element ID
      | name     | Todo connector |
      | basepath | /api           |
    And fill in TODO API host URL
    And click on the "Save" button

    When created connections
      | Todo connector | todo | Todo connection | no validation |
    And navigate to the "Home" page

    And click on the "Create Integration" link
    And check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button

    And check that position of connection to fill is "Finish"
    And select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Todo connection" connection
    And select "Fetch task" integration action
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Data Mapper" connection
    And define constant "12" of type "Integer" in data mapper
    And create data mapper mappings
      | 12 | parameters.id |
    And click on the "Done" button

    And click on the "Publish" link
    And set integration name "client-provider-mismatch"
    And click on the "Save" button

  @ENTESB-12736
  @openapi-provider-post-new
  Scenario: API Provider POST new - OpenAPI specification
    When create an API Provider integration "TODO Integration post new" from file openapi/connectors/todo.json
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
    When execute POST on API Provider route i-todo-integration-post-new endpoint "/api/" with body '{"id":13,"completed":1,"task":"task1"}'
    Then verify response has status 201
    And verify response has body
      """
        {"id":13,"completed":1,"task":"task1"}
      """
    And validate that all todos with task "task1" have value completed 1, period in ms: 1000
    And validate that number of all todos with task "task1" is 1

  @api-provider-error-handling
  @openapi-provider-error-handling
  @gh-3999
  Scenario: OpenAPI Provider GET non-existent
    When create an API Provider integration "TODO Integration get non existent" from file openapi/connectors/todo.yaml
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
    When execute GET on API Provider route i-todo-integration-get-non-existent endpoint "/14"
    Then verify response has status 404

    When execute GET on API Provider route i-todo-integration-get-non-existent endpoint "/e"
    Then verify response has status 418

  @openapi-provider-get-single
  @api-provider-serve-spec
  Scenario: OpenAPI Provider GET single
    When create an API Provider integration "TODO Integration get single" from file openapi/connectors/todo.yaml
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
    When execute GET on API Provider route i-todo-integration-get-single endpoint "/1"
    Then verify response has status 200
    And verify response has body
        """
        {"id":1}
        """

    When execute GET on API Provider route i-todo-integration-get-single endpoint "/openapi.json"
    Then verify response has status 200
    And verify response body contains
        """
        "openapi": "3.0.2"
        """

  @openapi-callback-warnings
  Scenario: Warning when user uses specification with callbacks
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select API Provider as start connection
    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create API Provider spec from url https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/callback-example.yaml
    And navigate to the next API Provider wizard step
    Then check visibility of page "Review API Provider Actions"
    And verify API spec warnings contain "All callbacks will be ignored."

  @openapi-link-warnings
  Scenario: Warning when user uses specification with links
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select API Provider as start connection
    When select the "API Provider" connection
    Then check visibility of page "Upload API Provider Specification"

    When create API Provider spec from url https://raw.githubusercontent.com/OAI/OpenAPI-Specification/master/examples/v3.0/link-example.yaml
    And navigate to the next API Provider wizard step
    Then check visibility of page "Review API Provider Actions"
    And verify API spec warnings contain "All links will be ignored."
    And verify API spec warnings contain "Links component is not supported yet."
