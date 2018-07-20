# @sustainer: mastepan@redhat.com

@api-connector-invalid-swagger
Feature: Customization - API Connector - Invalid swagger

  Background:
    Given log into the Syndesis
    Given clean application state

  @check-invalid-swagger-messages
  Scenario: Create from local file
    When navigate to the "Customizations" page
    Then check visibility of page "Customizations"

    When click on the "API Client Connectors" link
    Then check visibility of page "API Client Connectors"
    And open new Api Connector wizard
    And check visibility of page "Upload Swagger Specification"
    Then upload swagger file src/test/resources/swagger/connectors/invalid/kie-server-swagger.json
    And navigate to the next Api Connector wizard step "Review Actions"
    And check the error box is not present
    And check visibility of page "Review Actions"
