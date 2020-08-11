# @sustainer: mmuzikar@redhat.com

@ui
@apicurio
@api-client-auth
@api-connector
Feature: Testing authentication options of API client

  Background:
    Given clean application state
    And log into the Syndesis

    And wait for Todo to become ready
    And Set Todo app credentials

  @api-client-auth-api-key-query
  Scenario: API key used in API client provided in query
    Given deploy HTTP endpoints
    And reset content of "todo" table
    When navigate to the "Home" page
    And click on the "Customizations" link
    And click on the "API Client Connectors" link
    And click on the "Create API Connector" link

    And check visibility of page "Upload Swagger Specification"
    And upload swagger file
      | url | https://petstore.swagger.io/v2/swagger.json |

    And click on the "Next" button
    And click on the "Review/Edit" link
    And switch context to apicurio
    And configure the "api_key" security schema
    And fill in values by element ID
      | in20   | Query Parameter |
      | name20 | api_key         |
    And click on the "Save" button
    And leave apicurio context
    And click on the "Save" link
    And click on the "Next" link
    And click on the "Next" button
    And fill in values by element data-testid
      | host     | http://http-svc:8080 |
      | basepath | /auth                |
    And click on the "Save" button

    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Swagger Petstore" connection type
    And fill in values by element data-testid
      | authenticationparametervalue | password             |
      | host                         | http://http-svc:8080 |
      | basepath                     | /auth                |
    And click on the "Next" button
    And click on the "Save" button

    And navigate to the "Home" page
    And click on the "Create Integration" link
    And select "Timer" integration step
    And select "Simple" integration action
    And click on the "Next" button
    And select "Log" integration step
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Swagger Petstore" integration step
    And select "Find pet by ID" integration action
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "1" of type "Integer" in data mapper
    And sleep for jenkins delay or 2 seconds
    And create data mapper mappings
      | 1 | parameters.petId |
    And click on the "Done" button

    And click on the "Publish" link
    And set integration name "api-key-query"
    And publish integration

    Then Integration "api-key-query" is present in integrations list
    And wait until integration "api-key-query" gets into "Running" state
    And wait until integration api-key-query processed at least 1 message
    Then check that pod "i-api-key-query" logs contain string "Zerik"

  @api-client-auth-api-key-headers
  @api-client-edit
  @ENTESB-12921
  Scenario: API key used in API client provided in headers
    Given deploy HTTP endpoints
    And reset content of "todo" table
    When navigate to the "Home" page
    And click on the "Customizations" link
    And click on the "API Client Connectors" link
    And click on the "Create API Connector" link

    And check visibility of page "Upload Swagger Specification"
    And upload swagger file
      | url | https://petstore.swagger.io/v2/swagger.json |

    And click on the "Next" button
    And click on the "Review/Edit" link
    And switch context to apicurio
    And configure the "apikey" security schema
    And fill in values by element ID
      | in20   | HTTP header |
      | name20 | apikey      |
    And click on the "Save" button
    And leave apicurio context
    And click on the "Save" link
    And click on the "Next" link
    And click on the "Next" button
    And fill in values by element data-testid
      | host     | http://http-svc:8080 |
      | basepath | /auth                |
    And click on the "Save" button

    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Swagger Petstore" connection type
    And fill in values by element data-testid
      | authenticationparametervalue | password             |
      | host                         | http://http-svc:8080 |
      | basepath                     | /auth                |
    And click on the "Next" button
    And sleep for jenkins delay or 2 seconds
    And click on the "Save" button

    And navigate to the "Home" page
    And click on the "Create Integration" link
    And select "Timer" integration step
    And select "Simple" integration action
    And click on the "Next" button
    And select "Log" integration step
    And sleep for jenkins delay or 2 seconds
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Swagger Petstore" integration step
    And select "Find pet by ID" integration action
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "1" of type "Integer" in data mapper
    And open data bucket "parameters"
    And create data mapper mappings
      | 1 | parameters.petId |
    And click on the "Done" button

    And click on the "Publish" link
    And set integration name "api-key-header"
    And publish integration

    Then Integration "api-key-header" is present in integrations list
    And wait until integration "api-key-header" gets into "Running" state
    And wait until integration api-key-header processed at least 1 message
    Then check that pod "i-api-key-header" logs contain string "Zerik"
    