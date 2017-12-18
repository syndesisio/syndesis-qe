@api-connector
Feature: API connector test

  Background:
    Given "Camilla" logs into the Syndesis
    Given clean application state

  @create-api-connector-from-url-test
  Scenario: CREATE custom API client connector from swagger downloaded from url
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "API Client Connectors" link
    Then she is presented with the Syndesis page "API Client Connectors"
    And she opens new Api Connector wizard
    And she is presented with the Syndesis page "Upload Swagger"
    Then she uploads swagger file
      | url | http://petstore.swagger.io/v2/swagger.json |

    And she navigates to the next Api Connector wizard step "Review Swagger Actions"
    Then she is presented with the Syndesis page "Review Swagger Actions"
    And she navigates to the next Api Connector wizard step "Security"
    Then she is presented with the Syndesis page "Security"
    Then she sets up security by "OAuth 2.0"

    And she navigates to the next Api Connector wizard step "General Connector Info"
    Then she is presented with the Syndesis page "General Connector Info"
    And she creates new connector

    Then she is presented with the new connector "Swagger Petstore"

  @create-api-connector-from-file-test
  Scenario: CREATE custom API client connector from local swagger file
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "API Client Connectors" link
    Then she is presented with the Syndesis page "API Client Connectors"
    And she opens new Api Connector wizard
    And she is presented with the Syndesis page "Upload Swagger"
    Then she uploads swagger file
      | file | swagger_files/api_client_connector/petstore_swagger.json |

    Then she navigates to the next Api Connector wizard step "Review Swagger Actions"
    Then she is presented with the Syndesis page "Review Swagger Actions"
    Then she navigates to the next Api Connector wizard step "Security"
    Then she is presented with the Syndesis page "Security"
    Then she sets up security by "OAuth 2.0"
    Then she navigates to the next Api Connector wizard step "General Connector Info"
    Then she is presented with the Syndesis page "General Connector Info"
    And she creates new connector

    Then she is presented with the new connector "Swagger Petstore"

  @edit-api-connector
  Scenario: EDIT custom API client connector parameters
    When Camilla creates new API connector "Swagger Petstore"
      | security | authType | OAuth 2.0 |

    And she is presented with the Syndesis page "Connector Details"
    Then she opens the API connector "Swagger Petstore" detail

    Then she edits property
      | Connector Name  | Swagger Petstore-1            | name         |
      | Description     | Description-1                 | description  |
      | Host            | http://petstore.swagger.io-1  | host         |
      | Base URL        | /v2-1                         | basePath     |

  @delete-api-connector
  Scenario: DELETE custom API client connector
    When Camilla creates new API connector "Swagger Petstore"
      | security | authType | OAuth 2.0 |
    And clicks on the "Delete" button
    Then she is presented with the Syndesis page "Delete warning"
    When clicks on the modal dialog "Cancel" button
    And she is presented with a connectors list of size 1
    Then she is presented with the Syndesis page "API Client Connectors"
    Then clicks on the "Delete" button
    And she is presented with the Syndesis page "Delete warning"
    When clicks on the modal dialog "Delete" button
    Then she is presented with the Syndesis page "API Client Connectors"
    And she is presented with a connectors list of size 0
