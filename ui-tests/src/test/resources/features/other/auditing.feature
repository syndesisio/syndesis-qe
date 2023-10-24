# @sustainer: mkralik@redhat.com

@ui
@auditing
Feature: Test auditing feature

  Background: Clean application state
    Given clean application state
    And enable auditing
    And log into the Syndesis
    And navigate to the "Home" page

  Scenario: Test auditing
    ### Connector
    #create
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
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record contains following parameters
      | type       | connector        |
      | name       | Swagger Petstore |
      | user       | developer        |
      | recordType | created          |
    And check that the last audit record contains the event with the following parameters
      | type     | set              |
      | property | name             |
      | current  | Swagger Petstore |
    And check that the last audit record contains the event with the following parameters
      | type     | set                           |
      | property | configuredProperties.basePath |
      | current  | /v2                           |
    And check that the last audit record contains the event with the following parameters
      | type     | set                        |
      | property | configuredProperties.host  |
      | current  | http://petstore.swagger.io |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                |
      | property | configuredProperties.specification |
      | current  | {"swagger":"2.0","host":"pe...     |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                     |
      | property | configuredProperties.authenticationType |
      | current  | apiKey:api_key                          |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                |
      | property | configuredProperties.componentName |
      | current  | connector-rest-swagger-http4       |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                              |
      | property | configuredProperties.authenticationParameterName |
      | current  | api_key                                          |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                                   |
      | property | configuredProperties.authenticationParameterPlacement |
      | current  | header                                                |

    #update
    When open the API connector "Swagger Petstore" detail
    And click on the "Edit" button
    And fill in values by element data-testid
      | api-connector-name-field        | Swagger Petstore-1           |
      | api-connector-description-field | Description-1                |
      | api-connector-host-field        | http://petstore.swagger.io-1 |
      | api-connector-baseurl-field     | /v2-1                        |
    And click on the "Save" button
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record ID is the same as the previous stored one
    And check that the last audit record contains following parameters
      | type       | connector          |
      | name       | Swagger Petstore-1 |
      | user       | developer          |
      | recordType | updated            |
    And check that the last audit record contains the event with the following parameters
      | type     | change             |
      | property | name               |
      | previous | Swagger Petstore   |
      | current  | Swagger Petstore-1 |
    And check that the last audit record contains the event with the following parameters
      | type     | change                        |
      | property | configuredProperties.basePath |
      | previous | /v2                           |
      | current  | /v2-1                         |
    And check that the last audit record contains the event with the following parameters
      | type     | change                       |
      | property | configuredProperties.host    |
      | previous | http://petstore.swagger.io   |
      | current  | http://petstore.swagger.io-1 |

    #delete
    When navigate to the "API Client Connectors" page
    And delete connector Swagger Petstore-1
    And check visibility of page "Modal Dialog"
    When click on the modal dialog "Delete" button
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record ID is the same as the previous stored one
    And check that the last audit record contains following parameters
      | type       | connector |
      | name       | *         |
      | user       | developer |
      | recordType | deleted   |

    ### Connections
    #create
    When deploy ActiveMQ broker
    And created connections
      | Red Hat AMQ | AMQ | AMQ | AMQ connection |
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record contains following parameters
      | type       | connection |
      | name       | AMQ        |
      | user       | developer  |
      | recordType | created    |
    And check that the last audit record contains the event with the following parameters
      | type     | set  |
      | property | name |
      | current  | AMQ  |
    And check that the last audit record contains the event with the following parameters
      | type     | set                            |
      | property | configuredProperties.brokerUrl |
      | current  | tcp://syndesis-amq-tcp:61616   |
    And check that the last audit record contains the event with the following parameters
      | type     | set                           |
      | property | configuredProperties.username |
      | current  | amq                           |
    And check that the last audit record contains the event with the following parameters
      | type     | set                           |
      | property | configuredProperties.password |
      | current  | **********                    |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                       |
      | property | configuredProperties.skipCertificateCheck |
      | current  | false                                     |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                    |
      | property | configuredProperties.brokerCertificate |
    And check that the last audit record contains the event with the following parameters
      | type     | set                           |
      | property | configuredProperties.clientID |
    And check that the last audit record contains the event with the following parameters
      | type     | set                                    |
      | property | configuredProperties.clientCertificate |

    #update
    When navigate to the "Connections" page
    And opens the "AMQ" connection detail
    And click on the "Edit" button
    And fill in values by element data-testid
      | brokerurl | testurl      |
      | username  | testusername |
      | password  | testpassword |
    And click on the "Save" button
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record ID is the same as the previous stored one
    And check that the last audit record contains following parameters
      | type       | connection |
      | name       | AMQ        |
      | user       | developer  |
      | recordType | updated    |
    And check that the last audit record contains the event with the following parameters
      | type     | change                         |
      | property | configuredProperties.brokerUrl |
      | previous | tcp://syndesis-amq-tcp:61616   |
      | current  | testurl                        |
    And check that the last audit record contains the event with the following parameters
      | type     | change                        |
      | property | configuredProperties.username |
      | previous | amq                           |
      | current  | testusername                  |
    And check that the last audit record contains the event with the following parameters
      | type     | change                        |
      | property | configuredProperties.password |
      | previous | **********                    |
      | current  | **********                    |

    #delete
    When navigate to the "Connections" page
    And delete the "AMQ" connection
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record ID is the same as the previous stored one
    And check that the last audit record contains following parameters
      | type       | connection |
      | name       | *          |
      | user       | developer  |
      | recordType | deleted    |

    ### Integration
    #create
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And click on the "Next" button
    And click on the "Save" link
    And set integration name "integration1"
    And save and cancel integration editor
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record contains following parameters
      | type       | integration  |
      | name       | integration1 |
      | user       | developer    |
      | recordType | created      |
    And check that the last audit record contains the event with the following parameters
      | type     | set          |
      | property | name         |
      | current  | integration1 |

    #update
    When navigate to the "Integrations" page
    And select the "integration1" integration
    And click on the "Edit Integration" link
    And click on the "Save" link
    And set integration name "integration2"
    And save and cancel integration editor
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record ID is the same as the previous stored one
    And check that the last audit record contains following parameters
      | type       | integration  |
      | name       | integration2 |
      | user       | developer    |
      | recordType | updated      |
    And check that the last audit record contains the event with the following parameters
      | type     | change       |
      | property | name         |
      | previous | integration1 |
      | current  | integration2 |

    #delete
    When navigate to the "Integrations" page
    And delete the "integration2" integration
    And sleep for jenkins delay or 5 seconds

    Then check that the last audit record ID is the same as the previous stored one
    And check that the last audit record contains following parameters
      | type       | integration |
      | name       | *           |
      | user       | developer   |
      | recordType | deleted     |
