# @sustainer: sveres@redhat.com

@ui
@swagger
@api-connector-invalid-swagger
Feature: Customization - API Connector - Invalid swagger

  Background:
    Given log into the Syndesis
    Given clean application state

  @check-invalid-swagger-messages
  Scenario: Create from local file
    When click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    Then check visibility of page "Upload Swagger Specification"

    When upload swagger file src/test/resources/swagger/connectors/invalid/kie-server-swagger.json
    And navigate to the next Api Connector wizard step "Review Actions"
    Then check the error box is not present
    And check visibility of page "Review Actions"
