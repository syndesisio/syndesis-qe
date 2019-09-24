#@sustainer: alice.rum@redhat.com

@ui
@database
@datamapper
@jira
@manual
Feature: Jira Connector

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And reset content of "todo" table
    And log into the Syndesis
    And created connections
      | Jira | Jira | Jira Testing | Test Jira Connection |
    And navigate to the "Home" page

  @jira-comment-issue
  Scenario: Comment on a Jira issue

    Given create a new jira issue in project "MTP"

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "select company from contact limit(1)" value
    And fill in period input with "10" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "Jira Testing" connection
    And select "Add Comment" integration action
    And fill in issuekey for previously created issue
    And click on the "Done" button

    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    When add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | company | comment |
    And scroll "top" "right"
    And click on the "Done" button

    When click on the "Save" link
    And set integration name "jira-add-comment"
    And publish integration
    Then Integration "jira-add-comment" is present in integrations list
    And wait until integration "jira-add-comment" gets into "Running" state
    And check new comment exists in previously created jira issue with text "Red Hat"
    And close previously created issue

  @jira-add-issue
  Scenario: Add a new jira issue
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "select company from contact limit(1)" value
    And fill in period input with "10" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "Jira Testing" connection
    And select "Add Issue" integration action
    And fill in values by element data-testid
      | projectkey       | MTP                                   |
      | issuesummary     | Test Jira Issue Created From Syndesis |
      | issuetypeid      | Bug                                   |
    And click on the "Done" button

    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    When add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | company | description |
    And scroll "top" "right"
    And click on the "Done" button

    When click on the "Save" link
    And set integration name "jira-add-issue"
    And publish integration
    Then Integration "jira-add-issue" is present in integrations list
    And wait until integration "jira-add-issue" gets into "Running" state
    And check that open issue with summary "Test Jira Issue Created From Syndesis" and description "Red Hat" exists
    And close all issues with summary "Test Jira Issue Created From Syndesis" and description "Red Hat"

  @jira-transition-issue
  Scenario: Comment on a Jira issue

    Given create a new jira issue in project "MTP"

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "select company from contact limit(1)" value
    And fill in period input with "10" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "Jira Testing" connection
    And select "Transition Issue" integration action
    And fill in values by element data-testid
      | issuetransitionid | 41 |
    And fill in issuekey for previously created issue
    And click on the "Done" button

    When click on the "Save" link
    And set integration name "jira-transition-issue"
    And publish integration
    Then Integration "jira-transition-issue" is present in integrations list
    And wait until integration "jira-transition-issue" gets into "Running" state
    And check that previously created jira is in status "Done"

  @jira-retrieve-new-comments
  Scenario: Retreive new comments from a Jira issue

    Given create a new jira issue in project "MTP"

    # Create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Jira Testing" connection
    And select "Retrieve New Comments" integration action
    And fill in values by element data-testid
      | jql   | project = 'MTP' and summary ~ 'test issue' and status = 'To Do' |
      | delay | 1000                                                            |
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | body | task |
    And scroll "top" "right"
    And click on the "Done" button

    When click on the "Save" link
    And set integration name "jira-retrieve-new-comments"
    And publish integration
    Then Integration "jira-retrieve-new-comments" is present in integrations list
    And wait until integration "jira-retrieve-new-comments" gets into "Running" state

    When comment previously created jira issue with text "Red Hat"
    Then check that query "select * from todo where task like '%Red Hat%'" has some output
    And close previously created issue

  @jira-retrieve-new-issues
  Scenario: Retrieve New Issues

    Given close all issues with summary "test issue"

    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Jira Testing" connection
    And select "Retrieve New Issues" integration action
    And fill in values by element data-testid
      | jql   | project = 'MTP' and status = 'To Do' |
      | delay | 1000                                 |
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
    And create data mapper mappings
      | description | task |
    And scroll "top" "right"
    And click on the "Done" button

    When click on the "Save" link
    And set integration name "jira-retrieve-new-issues"
    And publish integration
    Then Integration "jira-retrieve-new-issues" is present in integrations list
    And wait until integration "jira-retrieve-new-issues" gets into "Running" state

    When create a new jira issue in project "MTP"
    Then check that query "select * from todo where task like '%this is the test issue%'" has some output
    And close all issues with summary "test issue"

