# @sustainer: alice.rum@redhat.com

@ui
@webhook
@database
@webhook-extension
@long-running
Feature: Webhook extension

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis
    And navigate to the "Home" page

#
#  1. Test if webhook GET triggers database operation
#
  @webhook-get
  Scenario: Check message
    When click on the "Create Integration" link to create a new integration.
    And check visibility of visual integration editor
    Then check that position of connection to fill is "Start"

    # select salesforce connection as 'from' point
    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"author":"New Author","title":"Book Title"} |
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into CONTACT values ('Prokop' , 'Dvere', :#COMPANY , 'some lead', '1999-01-01')" value
    And click on the "Next" button
    # add data mapper step
    And add integration step on position "0"
    And select "Data Mapper" integration step
    And check visibility of data mapper ui
    And create data mapper mappings
      | author | COMPANY |
    And click on the "Done" button
    And publish integration
    And set integration name "webhook-test"
    And publish integration
    Then wait until integration "webhook-test" gets into "Running" state

    When select the "webhook-test" integration
    And sleep for jenkins delay or 10 seconds
    And invoke post request to webhook in integration webhook-test with token test-webhook and body {"author":"New Author","title":"Book Title"}
    # give integration time to invoke DB request
    And sleep for jenkins delay or 3 seconds
    Then check that query "select * from contact where first_name = 'Prokop' AND last_name = 'Dvere' AND company = 'New Author'" has some output

  @reproducer
  @gh-5575
  @webhook-ui-url
  Scenario: Check whether UI contains webhook URL
    When click on the "Create Integration" link to create a new integration.
    Then check that position of connection to fill is "Start"

    # select salesforce connection as 'from' point
    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    Then check visibility of page "Choose a Finish Connection"
    When select "Log" integration step
    And fill in values by element data-testid
      | contextloggingenabled | false                  |
      | bodyloggingenabled    | true                   |
      | customtext            | test visibility of URL |
    And click on the "Done" button
    And publish integration
    And set integration name "webhook-test-5575"
    And publish integration
    Then wait until integration "webhook-test-5575" gets into "Running" state

    When select the "webhook-test-5575" integration
    And sleep for jenkins delay or 10 seconds

    Then check that webhook url for "webhook-test-5575" with token "test-webhook" in UI is same as in routes
    And verify the displayed webhook URL matches regex ^https://i-.*-syndesis.*/webhook/.*$

  @ENTESB-13988
    @webhook-error-handling
  Scenario Outline: Check whether webhook returns <error_code> error code
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath                     | test-webhook |
      | errorresponsecodes-server-error | <error_code> |

    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"first_name":"John","company":"Red Hat"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | personInstance |
    And click on the "Next" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO contact (create_date) VALUES ('Red Hat')" value
    And click on the "Done" button

    And publish integration
    And set integration name "webhook-error-code-<error_code>"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "webhook-error-code-<error_code>" gets into "Running" state

    When invoke post request to webhook
      | webhook-error-code-<error_code> | test-webhook | {"message":"Shaco"} | <error_code> |
    # ENTESB-13811 reproducer
    Then validate that logs of integration "webhook-error-code-<error_code>" contains string "ERROR: invalid input syntax for type date: "Red Hat""
    And validate that logs of integration "webhook-error-code-<error_code>" contains string "nested exception is org.postgresql.util.PSQLException: ERROR: invalid input syntax for type date: "Red Hat""

    Examples:
      | error_code |
      | 400        |
      | 404        |
      | 405        |
      | 409        |
      | 500        |
      | 501        |
      | 503        |

  @ENTESB-13988
    @webhook-return-codes
  Scenario Outline: Check whether webhook returns <code> code
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath      | test-webhook |
      | httpresponsecode | <code>       |

    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"first_name":"John","company":"Red Hat"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | personInstance |
    And click on the "Next" button

    # finish point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO todo (task) VALUES ('test')" value
    And click on the "Done" button

    And publish integration
    And set integration name "webhook-return-code-<code>"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "webhook-return-code-<code>" gets into "Running" state

    When invoke post request to webhook
      | webhook-return-code-<code> | test-webhook | {"message":"Shaco"} | <code> |

    Examples:
      | code |
      | 200  |
      | 201  |
      | 202  |
      | 204  |
    