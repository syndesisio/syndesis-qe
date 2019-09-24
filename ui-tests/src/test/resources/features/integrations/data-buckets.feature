# @sustainer: sveres@redhat.com

@ui
@dropbox
@twitter
@gmail
@oauth
@database
@datamapper
@integrations-data-buckets
Feature: Integration - Databucket

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And delete emails from "QE Google Mail" with subject "Red Hat"
    And log into the Syndesis
    And navigate to the "Settings" page
    And fill all oauth settings
    And create connections using oauth
      | Twitter | Twitter Listener |
      | Gmail   | QE Google Mail   |
    And created connections
      | Dropbox | QE Dropbox | QE Dropbox | SyndesisQE Dropbox test |
    And navigate to the "Home" page

#
#  1. hover with mouse - error check
#
  @data-buckets-check-popups
  Scenario: Error mark indicator
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Twitter Listener" connection
    And select "Mention" integration action
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"
    And check visibility of page "Choose a Finish Connection"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "UPDATE TODO SET completed=1 WHERE TASK = :#TASK" value
    And click on the "Next" button
    And check that text "Add a data mapping step" is "visible" in step warning inside of step number "2"
    And check that there is no warning inside of step number "1"

    When open integration flow details
    Then check that in connection info popover for step number "1" is following text
      | Action | Mention | Data Type | Twitter Mention |
    And check that in connection info popover for step number "2" is following text
      | Action | Invoke SQL | Data Type | SQL Parameter |

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | user.screenName | TASK |
    And scroll "top" "right"
    And click on the "Done" button
    And open integration flow details
    Then check that in connection info popover for step number "1" is following text
      | Action | Mention | Data Type | Twitter Mention |
    And check that in connection info popover for step number "3" is following text
      | Action | Invoke SQL | Data Type | SQL Parameter |
    And check that there is no warning inside of step number "3"
    And check that there is no warning inside of step number "1"
    And check that there is no warning inside of step number "2"

#
#  2. check that data buckets are available
#
  @gh-5042
  @data-buckets-usage
  Scenario: Create
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # first database connection - get some information to be used by second datamapper
    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"

    When fill in periodic query input with "select * from contact limit(1)" value
    And fill in period input with "10" value
    And select "Minutes" from sql dropdown
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    When select the "QE Google Mail" connection
    And select "Send Email" integration action
    And fill in data-testid field "to" from property "email" of credentials "QE Google Mail"
    And click on the "Done" button

    # add another connection
    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "select company as firma from contact limit(1);" value
    And click on the "Next" button

    #And check that there is no warning inside of step number "2"
    When open integration flow details
    Then check that in connection info popover for step number "2" is following text
      | Action | Invoke SQL | Data Type | n/a |

    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When open data bucket "1 - SQL Result"
    And open data bucket "2 - SQL Result"
    And open data mapper collection mappings
    And create data mapper mappings
      | company | text    |
      | firma   | subject |
    And scroll "top" "right"
    And click on the "Done" button
    Then check that there is no warning inside of step number "3"

    When click on the "Save" link
    And set integration name "Integration_with_buckets"
    And publish integration
    Then Integration "Integration_with_buckets" is present in integrations list
    And wait until integration "Integration_with_buckets" gets into "Running" state
    And check that email from "QE Google Mail" with subject "Red Hat" and text "Red Hat" exists
    And delete emails from "QE Google Mail" with subject "Red Hat"


#
#  3. check that data buckets work with long integration
#
  @gh-5042
  @data-buckets-usage-2
  Scenario: Create extended

    ##################### start step ##################################
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    Then check "Next" button is "Disabled"

    When fill in periodic query input with "select company as firma from contact limit(1);" value
    And fill in period input with "10" value
    And select "Seconds" from sql dropdown
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    ##################### finish step ##################################

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "select * from contact where company = :#COMPANY and first_name = :#MYNAME and last_name = :#MYSURNAME and lead_source = :#LEAD limit(1);" value
    And click on the "Next" button

    ##################### step NEW step ##################################

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "select first_name as myName from contact where company = :#COMPANY and last_name = :#MYSURNAME limit(1);" value
    And click on the "Next" button

    ##################### step NEW step step ##################################

    When add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "select last_name as mySurname from contact where company = :#COMPANY limit(1);" value
    And click on the "Next" button

    ##################### step step step NEW step ##################################

    When add integration step on position "2"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "select lead_source as myLeadSource from CONTACT where company = :#COMPANY and first_name = :#MYNAME and last_name = :#MYSURNAME limit(1);" value
    And click on the "Next" button

    ##################### step step step step NEW step ##################################

    When add integration step on position "3"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into CONTACT values (:#COMPANY , :#MYNAME , :#MYSURNAME , :#LEAD, '1999-01-01')" value
    And click on the "Next" button

    ##################### check warnings ##################################

    Then check that there is no warning inside of step number "1"
    And check that text "Add a data mapping step" is "visible" in step warning inside of steps
      | 2 | 3 | 4 | 5 | 6 |

    ##################### mappings ##################################
    #################################################################

    ##################### step step step step step MAPPING step ##################################

    When add integration step on position "4"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And perform action with data bucket
      | 1 - SQL Result | open  |
      | 2 - SQL Result | open  |
      | 3 - SQL Result | open  |
      | 4 - SQL Result | open  |
      | 4 - SQL Result | close |
      | 4 - SQL Result | open  |
    And close data bucket "4 - SQL Result"
    And open data bucket "4 - SQL Result"

    When open data mapper collection mappings
    And create data mapper mappings
      | firma        | COMPANY   |
      | myname       | MYNAME    |
      | mysurname    | MYSURNAME |
      | myleadsource | LEAD      |
    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step step step MAPPING step step ##################################

    When add integration step on position "3"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |
      | 3 - SQL Result | open |
      | 4 - SQL Result | open |

    When open data mapper collection mappings
    And create data mapper mappings
      | firma        | COMPANY   |
      | mysurname    | MYNAME    |
      | myname       | MYSURNAME |
      | myleadsource | LEAD      |
    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step step MAPPING step step ##################################

    When add integration step on position "2"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |
      | 3 - SQL Result | open |

    When open data mapper collection mappings
    And create data mapper mappings
      | firma     | COMPANY   |
      | myname    | MYNAME    |
      | mysurname | MYSURNAME |
    And scroll "top" "right"
    And click on the "Done" button

    ##################### step step MAPPING step step step ##################################

    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And perform action with data bucket
      | 1 - SQL Result | open |
      | 2 - SQL Result | open |


    When open data mapper collection mappings
    And create data mapper mappings
      | firma     | COMPANY   |
      | mysurname | MYSURNAME |
    And scroll "top" "right"
    And click on the "Done" button

    ##################### step MAPPING step step step step ##################################

    When scroll "top" "left"
    And add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And open data bucket "1 - SQL Result"

    When open data mapper collection mappings
    And create data mapper mappings
      | firma | COMPANY |
    And scroll "top" "right"
    And click on the "Done" button

    ##################### check warnings ##################################

    Then check that there is no warning inside of steps in range from "1" to "11"

    ##################### start the integration ##################################

    When click on the "Save" link
    And set integration name "Integration_with_buckets"
    And publish integration
    Then Integration "Integration_with_buckets" is present in integrations list
    And wait until integration "Integration_with_buckets" gets into "Running" state

    ##################### Verify database state ##################################

    # stay to invoke at least one cycle of integration
    When sleep for jenkins delay or "10" seconds
    Then check that query "select * from contact where company = 'Joe' and first_name = 'Red Hat' and last_name = 'Jackson' and lead_source = 'db'" has some output

    When delete the "Integration_with_buckets" integration
    # waiting on pod does not work atm, it needs exact integration name but I don't know the hash, TODO
    # but it does not matter, we only need to delete it so there are no leftover entries in database after delete step
    Then Wait until there is no integration pod with name "i-integration-with-buckets"
    And invoke database query "delete from contact where first_name = 'Red Hat'"
