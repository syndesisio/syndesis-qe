@data-buckets
Feature: Test functionality of data buckets during creation of integration

  Background: Clean application state
    Given clean application state
    Given reset content of "contact" table
    Given "Camilla" logs into the Syndesis

    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |
      | Salesforce | QE Salesforce    | QE Salesforce    | SyndesisQE salesforce test          |
      | DropBox    | QE Dropbox       | QE Dropbox       | SyndesisQE DropBox test             |
    And "Camilla" navigates to the "Home" page

#
#  1. hover with mouse - error check
#
  @data-buckets-check-popups
  Scenario: Check that there is error without data mapper step
    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # select twitter connection
    When Camilla selects the "Twitter Listener" connection
    And she selects "Mention" integration action
    And clicks on the "Done" button

    Then she is prompted to select a "Finish" connection from a list of available connections

    # select postgresDB connection as 'to' point
    Then Camilla is presented with the Syndesis page "Choose a Finish Connection"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
    And clicks on the "Done" button

    # check pop ups
    #Then she checks that text "Output: Twitter Mention" is "visible" in hover table over "start" step
    #Then she checks that text "Add a datamapper step" is "visible" in hover table over "finish" step
    Then she checks that text "Add a data mapping step" is "visible" in step warning inside of step number "2"
    And she checks that there is no warning inside of step number "0"

    And she checks that in connection info popover for step number "0" is following text
      | Connection Properties | Twitter Listener | Action | Mention | Output Data Type | Twitter Mention |

    And she checks that in connection info popover for step number "2" is following text
      | Connection Properties | Name | PostgresDB | Action | Invoke SQL | Input Data Type | SQL Parameter |



    # add data mapper step
    When Camilla clicks on the "Add a Step" button
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    When she creates mapping from "user.screenName" to "TASK"
    And scroll "top" "right"
    And click on the "Done" button

    # check pop ups

    And she checks that in connection info popover for step number "0" is following text
      | Connection Properties | Twitter Listener | Action | Mention | Output Data Type | Twitter Mention |

    And she checks that in connection info popover for step number "4" is following text
      | Connection Properties | Name | PostgresDB | Action | Invoke SQL | Input Data Type | SQL Parameter |

    And she checks that there is no warning inside of step number "4"
    And she checks that there is no warning inside of step number "0"
    And she checks that there is no warning inside of step number "2"

#
#  2. check that data buckets are available
#
  @data-buckets-usage
  Scenario: Check that data buckets can be used
    # clean salesforce before tests
    Given clean SF contacts related to TW account: "Twitter Listener"

    # create integration
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # first database connection - get some information to be used by second datamapper
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    #Then Camilla is presented with the Syndesis page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "select * from contact" value
    Then she fills period input with "10000" value
    #@wip time_unit_id to be specified after new update is available:
    #Then she selects "Miliseconds" from "time_unit_id" dropdown
    And clicks on the "Done" button


    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "QE Salesforce" connection
    And she selects "Create or update record" integration action
    And she selects "Contact" from "sObjectName" dropdown
    And Camilla clicks on the "Next" button
    And she selects "TwitterScreenName" from "sObjectIdName" dropdown
    And Camilla clicks on the "Done" button



    # add another connection
    When Camilla clicks on the "Add a Connection" button
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    # wip this query doesnt work ftb #698
    Then she fills invoke query input with "select company as firma from contact limit 1;" value
    # there is no done button:
    And clicks on the "Done" button


    Then she is presented with the "Add a Step" button

    #And she checks that there is no warning inside of step number "2"
    And she checks that in connection info popover for step number "2" is following text
      | Connection Properties | Name | PostgresDB | Action | Invoke SQL | Output Data Type | SQL Result |

    Then she adds second step between STEP and FINISH connection
    # add data mapper step
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then she opens data bucket "1 - SQL Result"
    Then she opens data bucket "2 - SQL Result"

    When she creates mapping from "company" to "TwitterScreenName__c"
    When she creates mapping from "last_name" to "LastName"
    When she creates mapping from "first_name" to "FirstName"

    When she creates mapping from "firma" to "Description"

    And scroll "top" "right"
    And click on the "Done" button

    And she checks that there is no warning inside of step number "4"

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Integration_with_buckets"
    And click on the "Publish" button
    # assert integration is present in list
    Then Camilla is presented with "Integration_with_buckets" integration details
    And "Camilla" navigates to the "Integrations" page

    And Integration "Integration_with_buckets" is present in integrations list
    # wait for integration to get in active state
    Then she waits until integration "Integration_with_buckets" gets into "Published" state


    # validate salesforce contacts
    Then check that contact from SF with last name: "Jackson" has description "Red Hat"
    # clean-up in salesforce
    Then delete contact from SF with last name: "Jackson"




#
#  3. check that data buckets work with long integration
#
  @data-buckets-usage-2
  Scenario: Check that data buckets can be used

    ##################### start step ##################################
    And clicks on the "Create Integration" button to create a new integration.
    Then she is presented with a visual integration editor
    And she is prompted to select a "Start" connection from a list of available connections

    # first database connection - get some information to be used by second datamapper
    When Camilla selects the "PostgresDB" connection
    And she selects "Periodic SQL Invocation" integration action
    Then she checks "Done" button is "Disabled"
    Then she fills periodic query input with "select company as firma from contact limit 1;" value
    Then she fills period input with "10000" value

    And clicks on the "Done" button


    ##################### finish step ##################################
    Then she is prompted to select a "Finish" connection from a list of available connections

    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "select * from contact where company = :#COMPANY and first_name = :#MYNAME and last_name = :#MYSURNAME and lead_source = :#LEAD" value
    And clicks on the "Done" button



    ##################### step NEW step ##################################
    And She adds integration "connection" on position "0"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "select first_name as myName from contact where company = :#COMPANY and last_name = :#MYSURNAME limit 1;" value
    And clicks on the "Done" button


    ##################### step NEW step step ##################################

    And She adds integration "connection" on position "0"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "select last_name as mySurname from contact where company = :#COMPANY limit 1;" value
    And clicks on the "Done" button

    ##################### step step step NEW step ##################################

    And She adds integration "connection" on position "2"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "select lead_source as myLeadSource from contact where company = :#COMPANY and first_name = :#MYNAME and last_name = :#MYSURNAME limit 1;" value
    And clicks on the "Done" button

    ##################### step step step step NEW step ##################################

    And She adds integration "connection" on position "3"
    When Camilla selects the "PostgresDB" connection
    And she selects "Invoke SQL" integration action
    Then she fills invoke query input with "insert into contact values (:#COMPANY , :#MYNAME , :#MYSURNAME , :#LEAD, '1999-01-01')" value
    And clicks on the "Done" button

    ##################### check warnings ##################################

    And she checks that there is no warning inside of step number "0"

    Then check that text "Add a data mapping step" is "visible" in step warning inside of steps: 2, 4, 6, 8, 10



    ##################### mappings ##################################
    #################################################################

    ##################### step step step step step MAPPING step ##################################

    And She adds integration "step" on position "4"
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open  |
      | 2 - SQL Result | open  |
      | 3 - SQL Result | open  |
      | 4 - SQL Result | open  |
      | 4 - SQL Result | close |
      | 4 - SQL Result | open  |


    #check if you can close and reopen it
    Then She closes data bucket "4 - SQL Result"
    Then She opens data bucket "4 - SQL Result"

    Then create mappings from following table
      | firma        | COMPANY   |
      | myname       | MYNAME    |
      | mysurname    | MYSURNAME |
      | myleadsource | LEAD      |


    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step step step MAPPING step step ##################################

    And She adds integration "step" on position "3"
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |
      | 3 - SQL Result | open |
      | 4 - SQL Result | open |

    #change something so it is unique entry
    Then create mappings from following table
      | firma        | COMPANY   |
      | mysurname    | MYNAME    |
      | myname       | MYSURNAME |
      | myleadsource | LEAD      |


    And scroll "top" "right"
    And click on the "Done" button


    ##################### step step step MAPPING step step ##################################
    And She adds integration "step" on position "2"
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |
      | 3 - SQL Result | open |


    Then create mappings from following table
      | firma     | COMPANY   |
      | myname    | MYNAME    |
      | mysurname | MYSURNAME |

    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step MAPPING step step step ##################################
    And She adds integration "step" on position "1"
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |


    Then create mappings from following table
      | firma     | COMPANY   |
      | mysurname | MYSURNAME |


    And scroll "top" "right"
    And click on the "Done" button

    ##################### step MAPPING step step step step ##################################
    And She adds integration "step" on position "0"
    And she selects "Data Mapper" integration step
    Then she is presented with data mapper ui

    Then She opens data bucket "1 - SQL Result"

    When she creates mapping from "firma" to "COMPANY"

    And scroll "top" "right"
    And click on the "Done" button



    ##################### check warnings ##################################

    And she checks that there is no warning inside of steps in range from "0" to "10"

    ##################### start the integration ##################################

    # finish and save integration
    When click on the "Save as Draft" button
    And she sets the integration name "Integration_with_buckets"
    And click on the "Publish" button

    # assert integration is present in list
    Then Camilla is presented with "Integration_with_buckets" integration details
    And "Camilla" navigates to the "Integrations" page
    And Integration "Integration_with_buckets" is present in integrations list

    # wait for integration to get in active state
    Then she waits until integration "Integration_with_buckets" gets into "Published" state

    # stay to invoke at least one cycle of integration
    And she stays there for "10000" ms


    ##################### Verify database state ##################################
    Then She checks that query "select * from contact where company = 'Joe' and first_name = 'Red Hat' and last_name = 'Jackson' and lead_source = 'db'" has some output

    Then Camilla deletes the "Integration_with_buckets" integration

    # waiting on pod does not work atm, it needs exact integration name but I don't know the hash, TODO
    # but it does not matter, we only need to delete it so there are no leftover entries in database after delete step
    And Wait until there is no integration pod with name "i-integration-with-buckets"

    # clean-up
    And She invokes database query "delete from contact where first_name = 'Red Hat'"