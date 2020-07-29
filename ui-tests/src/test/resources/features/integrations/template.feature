#@sustainer: mmuzikar@redhat.com

@ui
@data-mapper
@template
@oauth
Feature: Templates

  Background: Clean application state
    Given clean application state
    And log into the Syndesis
    And delete emails from "QE Google Mail" with subject "syndesis-template-test"
    And navigate to the "Settings" page
    And fill "Gmail" oauth settings "QE Google Mail"
    And create connections using oauth
      | Gmail | QE Google Mail |
    And reset content of "contact" table
    And insert into "contact" table
      | Joe | Jackson | Red Hat | db |

  @db-template-send
  Scenario Outline: Send an Email with text formatted by <template_type> template
    #create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"

    #Select DB connection as 'from' point
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in invoke query input with "SELECT * FROM contact" value
    And click on the "Next" button
    And check that position of connection to fill is "Finish"

    #select GMail connection as 'to' point
    When select the "QE Google Mail" connection
    And select "Send Email" integration action
    And fill in values by element data-testid
      | subject | syndesis-template-test |
    And fill in data-testid field "to" from property "email" of credentials "QE Google Mail"
    And click on the "Next" button

    #adding split step to split the result of Db connection
    # Then check visibility of page "Add to Integration"
    And add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    #adding <template_type> template to format the mail
    # Then check visibility of page "Add to Integration"
    And add integration step on position "1"
    And select "Template" integration step
    And set the template type to "<template_type>"
    And set the template to "<template_text>"
    And click on the "Done" button

    #add data mapper step for template
    # Then check visibility of page "Add to Integration"
    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | first_name | firstname |
      | last_name  | surname   |
      | company    | company   |
    And scroll "top" "right"
    And click on the "Done" button

    #add mapping for email step
    # Then check visibility of page "Add to Integration"
    And add integration step on position "3"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | message | text |
    And scroll "top" "right"
    And click on the "Done" button

    #finish and save integration
    When click on the "Save" link
    And set integration name "db-template-send-<template_type>"
    And publish integration

    # wait for integration to get in active state
    And wait until integration "db-template-send-<template_type>" gets into "Running" state
    And wait until integration db-template-send-<template_type> processed at least 1 message

    Then check that email from "QE Google Mail" with subject "syndesis-template-test" and text "Joe Jackson works at Red Hat" exists

    Examples:
      | template_type | template_text                                  |
      | Mustache      | {{firstname}} {{surname}} works at {{company}} |
      | Freemarker    | ${firstname} ${surname} works at ${company}    |
      | Velocity      | $firstname $surname works at $company          |

  @gh-6316
  @db-template-mustache-file-upload
  Scenario: Send an Email with text formatted by Mustache template uploaded from resources/templates/mustache.txt
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"

    #select DB connection as 'from' point
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in invoke query input with "SELECT * FROM contact" value
    And click on the "Done" button
    And check that position of connection to fill is "Finish"

    #select GMail connection as 'to' point
    When select the "QE Google Mail" connection
    And select "Send Email" integration action
    And fill in values by element data-testid
      | subject | syndesis-template-test |
    And fill in data-testid field "to" from property "email" of credentials "QE Google Mail"
    And click on the "Done" button

    #adding split step to split the result of Db connection
    # Then check visibility of page "Add to Integration"
    And add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    #adding mustache template to format the mail
    # Then check visibility of page "Add to Integration"
    And add integration step on position "1"
    And select "Template" integration step
    And set the template type to "Mustache"
    #Because of #6316 the UI reports that it "Could not accept mustache.tpl"
    And upload template from resource "templates/mustache.tpl"
    And click on the "Done" button

    #add data mapper step for template
    # Then check visibility of page "Add to Integration"
    And add integration step on position "1"
    And select "Data Mapper" integration step
    And check visibility of data mapper ui
    And create data mapper mappings
      | first_name | firstname |
      | last_name  | surname   |
      | company    | company   |
    And scroll "top" "right"
    And click on the "Done" button

    #add mapping for email step
    # Then check visibility of page "Add to Integration"
    And add integration step on position "3"
    And select "Data Mapper" integration step
    And check visibility of data mapper ui
    And create data mapper mappings
      | message | text |
    And scroll "top" "right"
    And click on the "Done" button

    #finish and save integration
    When click on the "Save" link
    And set integration name "DB to gmail-template"
    And publish integration

    Then Integration "DB to gmail-template" is present in integrations list
    #wait for integration to get in active state
    And wait until integration "DB to gmail-template" gets into "Running" state
    And wait until integration DB to gmail-template processed at least 1 message

    Then check that email from "QE Google Mail" with subject "syndesis-template-test" and text "Joe Jackson works at Red Hat" exists

  @gh-6317
  @ENTESB-11393
  @template-code-editor
  Scenario: Verify that the code editor for templates is working correctly
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link
    And check that position of connection to fill is "Start"

    #select DB connection as 'from' point
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in invoke query input with "SELECT * FROM contact" value
    And click on the "Done" button
    And check that position of connection to fill is "Finish"

    #select GMail connection as 'to' point
    When select the "QE Google Mail" connection
    And select "Send Email" integration action
    And fill in values by element data-testid
      | subject | syndesis-template-test |
    And fill in data-testid field "to" from property "email" of credentials "QE Google Mail"
    And click on the "Done" button

    #adding split step to split the result of Db connection
    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    #adding mustache template to format the mail
    When add integration step on position "1"
    And select "Template" integration step
    Then set the template type to "Mustache"
    And check that there is warning on line 1 in the template editor 
    And upload template from resource "templates/code_editor_test.txt"


    And check that "{{test}}" is highlighted in the template editor
    And check that there is no warning on line 1 in the template editor
    And check that number of warnings in the template editor is 2
    And check that there is error on line 3 in the template editor
    #This will fail because of #6317
    And check that there is an error message with text "'{{wrongid1}}' does not conform to the format {{xyz}} at line: 3, column: 12" in the template editor

  @gh-6149
  @template-brackets
  Scenario Outline: Create a template surrounded by brackets
    #create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    And check that position of connection to fill is "Start"

    #Select DB connection as 'from' point
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then fill in invoke query input with "SELECT * FROM contact" value
    And click on the "Next" button
    And check that position of connection to fill is "Finish"

    #select GMail connection as 'to' point
    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    #adding <template_type> template to format the mail
    # Then check visibility of page "Add to Integration"
    And add integration step on position "1"
    And select "Template" integration step
    And set the template type to "Mustache"
    And set the template to "({{firstName}}) -{{lastName}}-"
    And click on the "Done" button

    #add data mapper step for template
    # Then check visibility of page "Add to Integration"
    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | first_name | firstName |
      | last_name  | lastName  |
    And scroll "top" "right"
    And click on the "Done" button

    #finish and save integration
    When click on the "Save" link
    And set integration name "Template-brackets"
    And publish integration

    # wait for integration to get in active state
    And wait until integration "Template-brackets" gets into "Running" state
    And wait until integration Template-brackets processed at least 1 message

    Then check that pod "i-Template-brackets" logs contain string "(Joe) -Jackson-"
