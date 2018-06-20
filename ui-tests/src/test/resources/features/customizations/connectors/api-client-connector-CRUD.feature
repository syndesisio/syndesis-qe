@api-connector
Feature: Customization - API Connector CRUD

  Background:
    Given log into the Syndesis
    Given clean application state

  @create-api-connector-from-url-test
  Scenario: Create from url
    When navigate to the "Customizations" page
    Then check visibility of page "Customizations"

    When click on the "API Client Connectors" link
    Then check visibility of page "API Client Connectors"
    And open new Api Connector wizard
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | url | http://petstore.swagger.io/v2/swagger.json |

    And navigate to the next Api Connector wizard step "Review Actions"
    Then check visibility of page "Review Actions"
    And navigate to the next Api Connector wizard step "Specify Security"
    Then check visibility of page "Specify Security"
    Then set up security
      | authType       | OAuth 2.0                             |
      | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |

    And navigate to the next Api Connector wizard step "Review Edit Connector Details"
    Then check visibility of page "Review Edit Connector Details"
    And create new connector

    Then check visibility of the new connector "Swagger Petstore"

  @create-api-connector-from-file-test
  Scenario: Create from local file
    When navigate to the "Customizations" page
    Then check visibility of page "Customizations"

    When click on the "API Client Connectors" link
    Then check visibility of page "API Client Connectors"
    And open new Api Connector wizard
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | file | swagger/connectors/petstore.json |

    Then navigate to the next Api Connector wizard step "Review Actions"
    Then check visibility of page "Review Actions"
    Then navigate to the next Api Connector wizard step "Specify Security"
    Then check visibility of page "Specify Security"
    Then set up security
      | authType       | OAuth 2.0                             |
      | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |

    Then navigate to the next Api Connector wizard step "Review Edit Connector Details"
    Then check visibility of page "Review Edit Connector Details"
    And create new connector

    Then check visibility of the new connector "Swagger Petstore"

  @edit-api-connector
  Scenario: Edit parameters
    When create new API connector
      | source   | file           | swagger/connectors/petstore.json      |
      | security | authType       | OAuth 2.0                             |
      | security | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |
      | details  | connectorName  | Swagger Petstore                      |

    Then open the API connector "Swagger Petstore" detail
    And check visibility of page "Connector Details"

    Then edit property
      | Connector Name | Swagger Petstore-1           | name        |
      | Description    | Description-1                | description |
      | Host           | http://petstore.swagger.io-1 | host        |
      | Base URL       | /v2-1                        | basePath    |

  @delete-api-connector
  Scenario: Delete
    When create new API connector
      | source   | file           | swagger/connectors/petstore.json      |
      | security | authType       | OAuth 2.0                             |
      | security | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |
      | details  | connectorName  | Swagger Petstore                      |
    And click on the "Delete" button
    Then check visibility of page "Modal Dialog"
    When click on the modal dialog "Cancel" button
    And check visibility of a connectors list of size 2
    Then check visibility of page "API Client Connectors"

    #create new connection from the connector
    And navigate to the "Connections" page
    And click on the "Create Connection" button
    And select "Swagger Petstore" connection type
    And click on the "Next" button
    And fill Name Connection form
      | Connection Name | Petstore conn |
    And click on the "Create" button

    Then navigate to the "Customizations" page
    And click on the "API Client Connectors" link

    #delete created connector
    Then delete connector Swagger Petstore
    And check visibility of page "Modal Dialog"
    When click on the modal dialog "Delete" button
    Then check visibility of page "API Client Connectors"
    And check visibility of a connectors list of size 1

    #check the connection's been deleted too
    Then navigate to the "Connections" page
    And check that "Petstore conn" connection is not visible
