@api-connector-invalid-swagger
Feature: Customization - API Connector - Invalid swagger

  Background:
    Given "Camilla" logs into the Syndesis
    Given clean application state

  @check-invalid-swagger-messages
  Scenario: Create from local file
    When "Camilla" navigates to the "Customizations" page
    Then she is presented with the Syndesis page "Customizations"

    When clicks on the "API Client Connectors" link
    Then she is presented with the Syndesis page "API Client Connectors"
    And she opens new Api Connector wizard
    And she is presented with the Syndesis page "Upload Swagger Specification"
    Then uploads swagger file swagger/connectors/invalid/kie-server-swagger.json
    And she navigates to the next Api Connector wizard step "Review Actions"
    And checks the error box is not present
    And she is presented with the Syndesis page "Review Actions"
