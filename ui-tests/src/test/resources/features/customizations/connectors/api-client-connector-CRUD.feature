@api-connector
Feature: Customization - API Connector CRUD

  Background:
    Given "Camilla" logs into the Syndesis
    Given clean application state

  @create-api-connector-from-url-test
  Scenario: Create from url
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "API Client Connectors" link
    Then she is presented with the Syndesis page "API Client Connectors"
    And she opens new Api Connector wizard
    And she is presented with the Syndesis page "Upload Swagger Specification"
    Then she uploads swagger file
      | url | http://petstore.swagger.io/v2/swagger.json |

    And she navigates to the next Api Connector wizard step "Review Actions"
    Then she is presented with the Syndesis page "Review Actions"
    And she navigates to the next Api Connector wizard step "Specify Security"
    Then she is presented with the Syndesis page "Specify Security"
    Then she sets up security
      | authType       | OAuth 2.0                             |
      | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |

    And she navigates to the next Api Connector wizard step "Review Edit Connector Details"
    Then she is presented with the Syndesis page "Review Edit Connector Details"
    And she creates new connector

    Then she is presented with the new connector "Swagger Petstore"

  @create-api-connector-from-file-test
  Scenario: Create from local file
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "API Client Connectors" link
    Then she is presented with the Syndesis page "API Client Connectors"
    And she opens new Api Connector wizard
    And she is presented with the Syndesis page "Upload Swagger Specification"
    Then she uploads swagger file
      | file | swagger/connectors/petstore.json |

    Then she navigates to the next Api Connector wizard step "Review Actions"
    Then she is presented with the Syndesis page "Review Actions"
    Then she navigates to the next Api Connector wizard step "Specify Security"
    Then she is presented with the Syndesis page "Specify Security"
    Then she sets up security
    | authType       | OAuth 2.0                             |
    | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |

    Then she navigates to the next Api Connector wizard step "Review Edit Connector Details"
    Then she is presented with the Syndesis page "Review Edit Connector Details"
    And she creates new connector

    Then she is presented with the new connector "Swagger Petstore"

  @edit-api-connector
  Scenario: Edit parameters
    When Camilla creates new API connector
      | source   | file           | swagger/connectors/petstore.json      |
      | security | authType       | OAuth 2.0                             |
      | security | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |
      | details  | connectorName  | Swagger Petstore                      |

    Then she opens the API connector "Swagger Petstore" detail
    And she is presented with the Syndesis page "Connector Details"

    Then she edits property
      | Connector Name  | Swagger Petstore-1            | name         |
      | Description     | Description-1                 | description  |
      | Host            | http://petstore.swagger.io-1  | host         |
      | Base URL        | /v2-1                         | basePath     |

  @delete-api-connector
  Scenario: Delete
    When Camilla creates new API connector
      | source   | file           | swagger/connectors/petstore.json      |
      | security | authType       | OAuth 2.0                             |
      | security | accessTokenUrl | syndesisUrl+syndesisCallbackUrlSuffix |
      | details  | connectorName  | Swagger Petstore                      |
    And clicks on the "Delete" button
    Then she is presented with the Syndesis page "Modal Dialog"
    When clicks on the modal dialog "Cancel" button
    And she is presented with a connectors list of size 1
    Then she is presented with the Syndesis page "API Client Connectors"
    Then clicks on the "Delete" button
    And she is presented with the Syndesis page "Modal Dialog"
    When clicks on the modal dialog "Delete" button
    Then she is presented with the Syndesis page "API Client Connectors"
    And she is presented with a connectors list of size 0
