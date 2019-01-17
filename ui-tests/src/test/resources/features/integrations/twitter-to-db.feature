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
    Given clean "contact" table
    Given clean all tweets in twitter_talky account
    Given log into the Syndesis
    Given created connections
      | Twitter    | Twitter Listener | Twitter Listener | SyndesisQE Twitter listener account |

  Scenario: Create integration from twitter to database
    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

      # select twitter connection as 'from' point
    When select the "Twitter Listener" connection
    And select "Mention" integration action
    Then check that position of connection to fill is "Finish"

    # select postgresDB connection as 'to' point
    Then check visibility of page "Choose a Finish Connection"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Done" button

      # add data mapper step
    Then check visibility of page "Add to Integration"
    When click on the "Add a Step" button
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    Then create data mapper mappings
      | text      | company    |
      | user.name | first_name |

#    And scroll "top" "right"
    And click on the "Done" button
    # finish and save integration
    When click on the "Save as Draft" button
    And set integration name "Twitter to DB integration"
    And click on the "Publish" button

    # assert integration is present in list
    Then check visibility of "Twitter to DB integration" integration details
    And navigate to the "Integrations" page
    And Integration "Twitter to DB integration" is present in integrations list
    # wait for integration to get in active state
    Then wait until integration "Twitter to DB integration" gets into "Running" state

    Then tweet a message from twitter_talky to "Twitter Listener" with text "Red Hat"
    And sleep for "30000" ms

    Then checks that query "select * from contact where company like 'Red Hat%'" has some output

    And clean all tweets in twitter_talky account
