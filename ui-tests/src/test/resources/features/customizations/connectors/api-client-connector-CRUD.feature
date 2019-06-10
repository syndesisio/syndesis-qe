# @sustainer: mastepan@redhat.com

@ui
@api-connector
@swagger
@api-connector-crud
Feature: Customization - API Connector CRUD

  Background:
    Given log into the Syndesis
    Given clean application state

  @create-api-connector-from-url-test
  Scenario: Create from url
    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | url | http://petstore.swagger.io/v2/swagger.json |

    And navigate to the next Api Connector wizard step "Review Actions"
    Then check visibility of page "Review Actions"
    And navigate to the next Api Connector wizard step "Specify Security"
    Then check visibility of page "Specify Security"
    Then set up api connector security
      | authType       | OAuth 2.0                             |
      | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |

    And navigate to the next Api Connector wizard step "Review Edit Connector Details"
    Then check visibility of page "Review Edit Connector Details"
    And create new connector

    Then check visibility of the new connector "Swagger Petstore"

  @create-api-connector-from-file-test
  Scenario: Create from local file
    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | file | swagger/connectors/petstore.json |

    Then navigate to the next Api Connector wizard step "Review Actions"
    Then check visibility of page "Review Actions"
    Then navigate to the next Api Connector wizard step "Specify Security"
    Then check visibility of page "Specify Security"
    Then set up api connector security
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

    And open the API connector "Swagger Petstore" detail
    Then check visibility of page "Connector Details"

    When click on the "Edit" button
    And fill in values by element ID
      | name        | Swagger Petstore-1           |
      | description | Description-1                |
      | host        | http://petstore.swagger.io-1 |
      | basePath    | /v2-1                        |
    And click on the "Save" button
    Then validate connector detail values
      | name        | Swagger Petstore-1           |
      | description | Description-1                |
      | host        | http://petstore.swagger.io-1 |
      | basePath    | /v2-1                        |

  @create-delete-api-connector
  Scenario: Delete
    When create new API connector
      | source   | file           | swagger/connectors/petstore.json      |
      | security | authType       | OAuth 2.0                             |
      | security | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |
      | details  | connectorName  | Swagger Petstore                      |
    And click on the "Delete" button
    Then check visibility of page "Modal Dialog"
    When cancel modal dialog window
    And check visibility of a connectors list of size 1
    Then check visibility of page "API Client Connectors"

    #create new connection from the connector
    And created connections
      | Swagger Petstore | no credentials | Petstore conn | no validation |

    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page

    #delete created connector
    Then delete connector Swagger Petstore
    And check visibility of page "Modal Dialog"
    When click on the modal dialog "Delete" button
    Then check visibility of page "API Client Connectors"
    And check visibility of a connectors list of size 0

    #check the connection's been deleted too
    Then navigate to the "Connections" page
    And check that "Petstore conn" connection is not visible
