# @sustainer: mcada@redhat.com

@ui
@dropbox
@twitter
@gmail
@database
@datamapper
@integrations-data-buckets
Feature: Integration - Databucket

  Background: Clean application state
    Given clean application state
    Given reset content of "contact" table
    And delete emails from "jbossqa.fuse@gmail.com" with subject "Red Hat"
    Given log into the Syndesis

    Given created connections
      | Twitter | Twitter Listener | Twitter Listener   | SyndesisQE Twitter listener account |
      | DropBox | QE Dropbox       | QE Dropbox         | SyndesisQE DropBox test             |
      | Gmail   | QE Google Mail   | My GMail Connector | SyndesisQE GMail test               |
    And navigate to the "Home" page

#
#  1. hover with mouse - error check
#
  @data-buckets-check-popups
  Scenario: Error mark indicator
    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select twitter connection
    When select the "Twitter Listener" connection
    And select "Mention" integration action

    Then check that position of connection to fill is "Finish"

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
    And click on the "Done" button

    # check pop ups
    #Then check that text "Output: Twitter Mention" is "visible" in hover table over "start" step
    #Then check that text "Add a datamapper step" is "visible" in hover table over "finish" step
    Then check that text "Add a data mapping step" is "visible" in step warning inside of step number "3"
    And check that there is no warning inside of step number "1"

    And open integration flow details
    And check that in connection info popover for step number "1" is following text
      | Twitter Listener | Action | Mention | Data Type | Twitter Mention |

    And check that in connection info popover for step number "3" is following text
      | PostgresDB | Action | Invoke SQL | Data Type | SQL Parameter |



    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then create data mapper mappings
      | user.screenName | TASK |

    And scroll "top" "right"
    And click on the "Done" button

    # check pop ups
    And open integration flow details
    And check that in connection info popover for step number "1" is following text
      | Twitter Listener | Action | Mention | Data Type | Twitter Mention |

    And check that in connection info popover for step number "5" is following text
      | PostgresDB | Action | Invoke SQL | Data Type | SQL Parameter |

    And check that there is no warning inside of step number "5"
    And check that there is no warning inside of step number "1"
    And check that there is no warning inside of step number "3"

#
#  2. check that data buckets are available
#
  @data-buckets-usage
  Scenario: Create

    # create integration
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # first database connection - get some information to be used by second datamapper
    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    #Then check visibility of page "Periodic SQL Invocation"
    #@wip this (disabled) functionality is not yet available
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "select * from contact" value
    Then fill in period input with "10" value
    Then select "Minutes" from sql dropdown
    #@wip time_unit_id to be specified after new update is available:
    #Then select "Miliseconds" from "time_unit_id" dropdown
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    When select the "My GMail Connector" connection
    And select "Send Email" integration action
    And fill in values
      | Email to | jbossqa.fuse@gmail.com |
    And click on the "Done" button

    # add another connection
    When add integration step on position "0"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    # wip this query doesnt work ftb #698
    Then fill in invoke query input with "select company as firma from contact limit 1;" value
    # there is no done button:
    And click on the "Next" button

    #And check that there is no warning inside of step number "2"
    And open integration flow details
    And check that in connection info popover for step number "3" is following text
      | Name | PostgresDB | Action | Invoke SQL | Data Type | SQL Result |

    Then add second step between STEP and FINISH connection
    # add data mapper step
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then open data bucket "1 - SQL Result"
    Then open data bucket "2 - SQL Result"

    Then create data mapper mappings
      | company | text    |
      | firma   | subject |

    And scroll "top" "right"
    And click on the "Done" button

    And check that there is no warning inside of step number "5"

    ###################### step step NEW step ##################################
    ##### test that 2 following SQL select steps do not break integration ######

    And add integration step on position "2"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "select * from contact  limit 1;" value
    And click on the "Next" button

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Integration_with_buckets"
    And publish integration
    # assert integration is present in list
    And Integration "Integration_with_buckets" is present in integrations list
    # wait for integration to get in active state
    Then wait until integration "Integration_with_buckets" gets into "Running" state

    #give gmail time to receive mail
    When sleep for "10000" ms
    Then check that email from "jbossqa.fuse@gmail.com" with subject "Red Hat" and text "Red Hat" exists
    And delete emails from "jbossqa.fuse@gmail.com" with subject "Red Hat"


#
#  3. check that data buckets work with long integration
#
  @data-buckets-usage-2
  Scenario: Create extended

    ##################### start step ##################################
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # first database connection - get some information to be used by second datamapper
    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "select company as firma from contact limit 1;" value
    Then fill in period input with "10" value
    Then select "Seconds" from sql dropdown

    And click on the "Next" button


    ##################### finish step ##################################
    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "select * from contact where company = :#COMPANY and first_name = :#MYNAME and last_name = :#MYSURNAME and lead_source = :#LEAD" value
    And click on the "Next" button



    ##################### step NEW step ##################################
    And add integration step on position "0"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "select first_name as myName from contact where company = :#COMPANY and last_name = :#MYSURNAME limit 1;" value
    And click on the "Next" button


    ##################### step NEW step step ##################################

    And add integration step on position "0"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "select last_name as mySurname from contact where company = :#COMPANY limit 1;" value
    And click on the "Next" button

    ##################### step step step NEW step ##################################

    And add integration step on position "2"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "select lead_source as myLeadSource from CONTACT where company = :#COMPANY and first_name = :#MYNAME and last_name = :#MYSURNAME limit 1;" value
    And click on the "Next" button

    ##################### step step step step NEW step ##################################

    And add integration step on position "3"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "insert into CONTACT values (:#COMPANY , :#MYNAME , :#MYSURNAME , :#LEAD, '1999-01-01')" value
    And click on the "Next" button

    ##################### check warnings ##################################

    And check that there is no warning inside of step number "1"

    Then check that text "Add a data mapping step" is "visible" in step warning inside of steps: 3, 5, 7, 9, 11



    ##################### mappings ##################################
    #################################################################

    ##################### step step step step step MAPPING step ##################################

    And add integration step on position "4"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open  |
      | 2 - SQL Result | open  |
      | 3 - SQL Result | open  |
      | 4 - SQL Result | open  |
      | 4 - SQL Result | close |
      | 4 - SQL Result | open  |


    #check if you can close and reopen it
    Then close data bucket "4 - SQL Result"
    Then open data bucket "4 - SQL Result"

    Then create data mapper mappings
      | firma        | COMPANY   |
      | myname       | MYNAME    |
      | mysurname    | MYSURNAME |
      | myleadsource | LEAD      |


    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step step step MAPPING step step ##################################

    And add integration step on position "3"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |
      | 3 - SQL Result | open |
      | 4 - SQL Result | open |

    #change something so it is unique entry
    Then create data mapper mappings
      | firma        | COMPANY   |
      | mysurname    | MYNAME    |
      | myname       | MYSURNAME |
      | myleadsource | LEAD      |


    And scroll "top" "right"
    And click on the "Done" button


    ##################### step step step MAPPING step step ##################################
    And add integration step on position "2"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |
      | 3 - SQL Result | open |


    Then create data mapper mappings
      | firma     | COMPANY   |
      | myname    | MYNAME    |
      | mysurname | MYSURNAME |

    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step MAPPING step step step ##################################
    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |


    Then create data mapper mappings
      | firma     | COMPANY   |
      | mysurname | MYSURNAME |


    And scroll "top" "right"
    And click on the "Done" button

    ##################### step MAPPING step step step step ##################################
    And scroll "top" "left"
    And add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    Then open data bucket "1 - SQL Result"

    When create mapping from "firma" to "COMPANY"

    And scroll "top" "right"
    And click on the "Done" button



    ##################### check warnings ##################################

    And check that there is no warning inside of steps in range from "1" to "11"

    ##################### start the integration ##################################

    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Integration_with_buckets"
    And publish integration

    # assert integration is present in list
    And Integration "Integration_with_buckets" is present in integrations list

    # wait for integration to get in active state
    Then wait until integration "Integration_with_buckets" gets into "Running" state

    # stay to invoke at least one cycle of integration
    And sleep for "10000" ms


    ##################### Verify database state ##################################
    Then check that query "select * from contact where company = 'Joe' and first_name = 'Red Hat' and last_name = 'Jackson' and lead_source = 'db'" has some output

    Then delete the "Integration_with_buckets" integration

    # waiting on pod does not work atm, it needs exact integration name but I don't know the hash, TODO
    # but it does not matter, we only need to delete it so there are no leftover entries in database after delete step
    And Wait until there is no integration pod with name "i-integration-with-buckets"

    # clean-up
    And invoke database query "delete from contact where first_name = 'Red Hat'"