# @sustainer: mmajerni@redhat.com

@ui
@apicurio
@apicurio-customization
Feature: Customization - API Connector - ApicurIO GUI

  Background:
    Given log into the Syndesis
    Given clean application state

    When click on the "Customizations" link
    Then check visibility of page "Customizations"

    When click on the "API Client Connectors" link
    Then check visibility of page "API Client Connectors"

    When open new Api Connector wizard

    And check visibility of page "Upload Swagger Specification"
    And upload swagger file src/test/resources/swagger/connectors/invalid/kie-server-swagger.json
    And navigate to the next Api Connector wizard step "Review Actions"
    Then check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    And check that apicurio shows 218 imported operations
    And check that apicurio shows 58 warnings

    And clicks on the "Review/Edit" link
    And change frame to "apicurio"

  @ignore
  @gh-3459
  @ENTESB-11443
  @ENTESB-16061
  @apicurio-check-warnings-change
  Scenario: Check if warnings change is propagated into connector review page from ApicurIO GUI
    When remove warning via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    Then check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    And check that apicurio shows 218 imported operations
    And check that apicurio shows 57 warnings

  @apicurio-check-operations-change-add
  Scenario: Check if operations change is propagated into connector review page from ApicurIO GUI
    When add an operation via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    And check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    Then check that apicurio shows 219 imported operations

  @gh-4910
  @ENTESB-17281
  @apicurio-check-operations-change-add-with-error
  Scenario: Check if operations change with an error is propagated into connector review page from ApicurIO GUI
    When add an operation with error via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    And check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    Then check that apicurio shows 219 imported operations
    And check that apicurio shows 1 errors

  @apicurio-check-operations-change-remove
  Scenario: Check if removing an operations is reflected in connector review page from ApicurIO GUI
    When remove an operation via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    And check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    Then check that apicurio shows 217 imported operations

  @apicurio-check-operations-change-but-no-save
  Scenario: Check if operations change is correctly not propagated into connector when changes from ApicurIO GUI are not saved
    When add an operation via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Cancel" link
    Then check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    And check that apicurio shows 218 imported operations

  @ENTESB-11474
  @apicurio-check-security-settings-no-security
  Scenario: Check apicurio security settings - no security
    And change frame to "syndesis"
    And clicks on the "Cancel" link
    And clicks on the "Next" link
    Then check that api connector authentication section contains security type "No Security"

    When click on the "Next" button
    And click on the "Save" button
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Kie Server API" connection type
    Then check that apicurio connection authentication type contains only fields:
      | host     |
      | basepath |


  @ENTESB-11474
  @apicurio-check-security-settings-basic
  Scenario: Check apicurio security settings - basic security
    When add security schema BASIC via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    Then check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    And check that apicurio shows 218 imported operations

    When clicks on the "Next" link
    Then check that api connector authentication section contains security type "HTTP Basic Authentication"

    When click on the "Next" button
    And click on the "Save" button
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Kie Server API" connection type
    Then check that apicurio connection authentication type contains only fields:
      | username |
      | password |
      | host     |
      | basepath |


  @ENTESB-11474
  @apicurio-check-security-settings-api-key
  Scenario: Check apicurio security settings - API key
    When add security schema API Key via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    Then check visibility of page "Review Actions"
    And check that apicurio imported operations number is loaded
    And check that apicurio shows 218 imported operations

    When clicks on the "Next" link
    Then check that api connector authentication section contains security type "API Key"

    When click on the "Next" button
    And click on the "Save" button
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Kie Server API" connection type
    Then check that apicurio connection authentication type contains only fields:
      | API key  |
      | host     |
      | basepath |

  @ENTESB-12019
  @ENTESB-11841
  @ENTESB-11474
  @gh-5429
  @gh-6123
  @apicurio-check-security-settings-oauth-2
  Scenario: Check apicurio security settings - OAuth 2

    When add security schema OAuth 2 via apicurio gui
    And change frame to "syndesis"
    And clicks on the "Save" link
    Then check visibility of page "Review Actions"
    # we need to give time to UI to fetch the changes
    And check that apicurio imported operations number is loaded
    And check that apicurio shows 218 imported operations
    When clicks on the "Next" link
    Then check that api connector authentication section contains security type "OAuth 2.0"

    When click on the "Next" button
    And click on the "Save" button
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Kie Server API" connection type
    And click on element with data-testid "connection-creator-settings"
    And click on the "Confirm" button
    Then check that OAuth fields exists for connection "Kie Server API"
      | clientid              |
      | clientsecret          |
      | authorizationendpoint |
      | tokenendpoint         |
      | oauthscopes           |
