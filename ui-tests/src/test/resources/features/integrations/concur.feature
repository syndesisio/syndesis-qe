# @sustainer: mcada@redhat.com
#
# Concur credentials callback can be only changed by concur support, to test it
# one must install syndesis with --route=syndesis.my-minishift.syndesis.io and
# redirect this route to correct minishift/openshift IP via /etc/hosts file.
#
# I am also not able to create verification steps, because communication would
# go through concur WS and we have different credentials for that and they point
# to another concur instance thus not able to validate what our oauth credentials
# created. For more information ask kstam@redhat.com as he said it is not possible
# to have oauth and ws credentials to point to the same developer instance.
#

@concur
Feature: Concur Connector

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis
    And navigate to the "Settings" page
    And fill all oauth settings
    And create connections using oauth
      | SAP Concur | Test-Concur-connection |
    And invoke database query "insert into CONTACT values ('Akali' , 'Queen', '50' , 'some lead', '1999-01-01')"
    And navigate to the "Home" page

  @concur-listitem
  Scenario: Check message

    # create integration
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Done" button is "Disabled"

    When fill in periodic query input with "SELECT * FROM CONTACT where first_name = 'Akali'" value
    And fill in period input with "60" value
    And select "Seconds" from sql dropdown
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "Log" connection
    And select "Simple Logger" integration action
    And fill in values
      | log level      | ERROR |
      | Log Body       | true  |
      | Log message Id | true  |
      | Log Headers    | true  |
      | Log everything | true  |
    Then click on the "Done" button

    When click on the "Add a Connection" button
    And select the "Test-Concur-connection" connection
    Then select "Gets all lists" integration action

    When add integration "step" on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | company | parameters.limit |
    And click on the "Done" button

    When add integration "step" on position "2"
    And select "Log" integration step
    And fill in values
      | Message Context | true |
      | Message Body    | true |
    Then click on the "Done" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Integration_with_concur"
    And click on the "Publish" button
    Then check visibility of "Integration_with_concur" integration details

    When navigate to the "Integrations" page
    Then Integration "Integration_with_concur" is present in integrations list
    And wait until integration "Integration_with_concur" gets into "Running" state
    And validate that logs of integration "Integration_with_concur" contains string "gWnytPb$pHxNJMPz4yL0nosnCQ4r30gRWs4w"

    Then reset content of "contact" table
