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

    And click on the "Next" button
    Then check visibility of page "Review Actions"
    And click on the "Next" link
    Then check visibility of page "Specify Security"
    And click on the "Next" button
    And click on the "Save" button

    Then check visibility of the new connector "Swagger Petstore"

  @create-api-connector-from-file-test
  Scenario: Create from local file
    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | file | swagger/connectors/petstore.json |

    And click on the "Next" button
    Then check visibility of page "Review Actions"
    And click on the "Next" link
    Then check visibility of page "Specify Security"
    And click on the "Next" button
    And click on the "Save" button

    Then check visibility of the new connector "Swagger Petstore"

  @edit-api-connector
  Scenario: Edit parameters
    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | file | swagger/connectors/petstore.json |

    And click on the "Next" button
    Then check visibility of page "Review Actions"
    And click on the "Next" link
    Then check visibility of page "Specify Security"
    And click on the "Next" button
    And fill in values by element ID
      | name | Swagger Petstore |
    And click on the "Save" button

    And open the API connector "Swagger Petstore" detail
    Then check visibility of page "Connector Details"

    When click on the "Edit" button
    And fill in values by element ID
      | name        | Swagger Petstore-1           |
      | description | Description-1                |
      | host        | http://petstore.swagger.io-1 |
      | basepath    | /v2-1                        |
    And click on the "Save" button
    Then validate connector detail values
      | name        | Swagger Petstore-1           |
      | description | Description-1                |
      | host        | http://petstore.swagger.io-1 |
      | basepath    | /v2-1                        |

  @create-delete-api-connector
  Scenario: Delete
    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file
      | file | swagger/connectors/petstore.json |

    And click on the "Next" button
    Then check visibility of page "Review Actions"
    And click on the "Next" link
    Then check visibility of page "Specify Security"
    And click on the "Next" button
    And fill in values by element ID
      | name | Swagger Petstore |
    And click on the "Save" button

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
