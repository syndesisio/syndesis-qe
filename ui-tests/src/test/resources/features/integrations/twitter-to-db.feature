# @sustainer: mmajerni@redhat.com

@ui
@doc-tutorial
@twitter
@database
@datamapper
@integrations-twitter-to-db
Feature: Integration - Twitter to Database

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And clean all tweets in twitter_talky account
    And log into the Syndesis
    And navigate to the "Settings" page
    And fill "Twitter" oauth settings "Twitter Listener"

    And create connections using oauth
      | Twitter | Twitter Listener |


  Scenario: Create integration from twitter to database
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select twitter connection as 'from' point
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Done" button

      # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | text      | company    |
      | user.name | first_name |
    And click on the "Done" button

    # finish and save integration
    When publish integration
    And set integration name "Twitter to DB integration"
    And publish integration
    Then Integration "Twitter to DB integration" is present in integrations list
    # wait for integration to get in active state
    And wait until integration "Twitter to DB integration" gets into "Running" state

    When tweet a message from twitter_talky to "Twitter Listener" with text "Red Hat"
    And sleep for "30000" ms
    Then checks that query "select * from contact where company like 'Red Hat%'" has some output
    And clean all tweets in twitter_talky account
