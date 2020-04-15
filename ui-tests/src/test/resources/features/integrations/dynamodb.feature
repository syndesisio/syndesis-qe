# @sustainer: tplevko@redhat.com

@ui
@datamapper
@dynamodb
@integrations-dynamodb
Feature: Dynamodb connector

  Background: Clean application state
    Given clean application state
    And log into the Syndesis
    And delete dynamoDb DB table
    And create new dynamoDb table with primary key "email" and sort key "company"
    And created connections
      | Amazon DynamoDB | AWS DDB | AWS-DDB-test | AWS-DDB test |
    And navigate to the "Home" page

  @dynamodb-put
  Scenario: Webhook to dynamoDb - create item
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"email":"test","company":"test","name":"test","department":"test"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When selects the "AWS-DDB-test" connection
    And select "Put Item" integration action
    And fill in values by element data-testid
      | element | {"email":":#email","company":":#company","name":":#name","department":":#department"} |
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | email      | :#email      |
      | company    | :#company    |
      | name       | :#name       |
      | department | :#department |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to dynamoDb"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to dynamoDb" gets into "Running" state
    And select the "Webhook to dynamoDb" integration
    And invoke post request to webhook in integration Webhook to dynamoDb with token test-webhook and body {"email":"test@redhat.com","company":"Red Hat","name":"test person","department":"CEE"}
    And verify the dynamoDB table contains record
      | email      | test@redhat.com |
      | company    | Red Hat         |
      | name       | test person     |
      | department | CEE             |

  @dynamodb-update
  Scenario: Webhook to dynamoDb - update item
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"email":"test","company":"test","name":"test","department":"test"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When selects the "AWS-DDB-test" connection
    And select "Put Item" integration action
    And fill in values by element data-testid
      | element | {"email":":#email","company":":#company","name":":#name","department":":#department"} |
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | email      | :#email      |
      | company    | :#company    |
      | name       | :#name       |
      | department | :#department |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to dynamoDb"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to dynamoDb" gets into "Running" state
    And select the "Webhook to dynamoDb" integration
    And invoke post request to webhook in integration Webhook to dynamoDb with token test-webhook and body {"email":"test@redhat.com","company":"Red Hat","name":"test person","department":"CEE"}
    And verify the dynamoDB table contains record
      | email      | test@redhat.com |
      | company    | Red Hat         |
      | name       | test person     |
      | department | CEE             |

    And invoke post request to webhook in integration Webhook to dynamoDb with token test-webhook and body {"email":"test@redhat.com","company":"Red Hat","name":"other person","department":"ACME"}
    And verify the dynamoDB table contains single record
    And verify the dynamoDB table contains record
      | email      | test@redhat.com |
      | company    | Red Hat         |
      | name       | other person    |
      | department | ACME            |

  @dynamodb-query
  Scenario: Webhook to dynamoDb - query item
    Given reset content of "contact" table
    When insert into dynamoDb table
      | email      | test@redhat.com |
      | company    | Red Hat         |
      | name       | person          |
      | department | CEE             |
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"email":"test","company":"test"} |
    And click on the "Done" button

      # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company, lead_source) VALUES(:#first_name,:#company,:#lead_source)" value
    And click on the "Done" button

    When add integration step on position "0"
    Then check visibility of page "Choose a Finish Connection"
    When selects the "AWS-DDB-test" connection
    And select "Query" integration action
    And fill in values by element data-testid
      | attributes | email,company,name,department             |
      | element    | {"email":":#email","company":":#company"} |
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | email   | :#email   |
      | company | :#company |
    And click on the "Done" button

    When add integration step on position "2"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And open data bucket "3 - Result"
    When create data mapper mappings
      | name    | first_name  |
      | company | company     |
      | email   | lead_source |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook dynamoDb query"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook dynamoDb query" gets into "Running" state
    And select the "Webhook dynamoDb query" integration
    And invoke post request to webhook in integration Webhook dynamoDb query with token test-webhook and body {"email":"test@redhat.com","company":"Red Hat"}
    And check that query "SELECT * FROM contact WHERE first_name = 'person'" has some output

  @dynamodb-delete
  Scenario: Webhook to dynamoDb - delete item
    When insert into dynamoDb table
      | email      | test@redhat.com |
      | company    | Red Hat         |
      | name       | test person     |
      | department | CEE             |

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"email":"test","company":"test"} |
    And click on the "Done" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When selects the "AWS-DDB-test" connection
    And select "Remove Item" integration action
    And fill in values by element data-testid
      | element | {"email":":#email","company":":#company"} |
    And click on the "Done" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | email   | :#email   |
      | company | :#company |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to dynamoDb delete"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to dynamoDb delete" gets into "Running" state
    And select the "Webhook to dynamoDb delete" integration
    And invoke post request to webhook in integration Webhook to dynamoDb delete with token test-webhook and body {"email":"test@redhat.com","company":"Red Hat"}
    And verify the dynamoDB table doesn't contain record
      | email      | test@redhat.com |
      | company    | Red Hat         |
      | name       | test person     |
      | department | CEE             |
