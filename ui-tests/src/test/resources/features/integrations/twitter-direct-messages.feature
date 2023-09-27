# @sustainer: alice.rum@redhat.com

@disabled
@ui
@twitter
@oauth
@database
@datamapper
@integrations-twitter-direct-messages
Feature: Integration - Twitter Direct Messages

  Background: Clean application state
    Given clean application state
    And reset content of "contact" table
    And log into the Syndesis
    And navigate to the "Settings" page
    And fill "Twitter" oauth settings "Twitter Listener"

    And create connections using oauth
      | Twitter | Twitter Listener |

  @twitter-direct-messages-receive
  Scenario: Receive direct message on twitter
    Given delete all direct messages received by "Twitter Listener" with text "Red Hat"

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Twitter Listener" connection
    And select "Retrieve" integration action
    And fill in values by element data-testid
      | delay | 10 |
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO CONTACT(first_name, company) VALUES(:#first_name, :#company)" value
    And click on the "Next" button

    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | text     | company    |
      | senderId | first_name |
    And click on the "Done" button

    When send direct message from twitter_talky to "Twitter Listener" with text "Red Hat"
    And publish integration
    And set integration name "twitter-direct-messages-test-receive"
    And publish integration

    And Integration "twitter-direct-messages-test-receive" is present in integrations list
    And wait until integration "twitter-direct-messages-test-receive" gets into "Running" state
    And wait until integration twitter-direct-messages-test-receive processed at least 1 message

    Then check that contact table contains contact where first name is senderId for "Twitter Talky" account and company is "Red Hat"

  @twitter-direct-messages-send
  Scenario: Send direct message on twitter
    Given delete all direct messages received by "Twitter Talky" with text "Red Hat"

    # create integration
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # from is periodic sql select
    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "select company from contact limit 1" value
    And fill in period input with "10" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button

    # to is twitter send direct message
    When select the "Twitter Listener" connection
    And select "Send" integration action
    And fill in values by element data-testid
      | message | temp message |
    And fill username for "twitter_talky" account
    And click on the "Next" button

    # add Split step
    When add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    # data mapper maps sql result to twitter DM
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | company | message |
    And click on the "Done" button

    When publish integration
    And set integration name "twitter-direct-messages-test-send"
    And publish integration
    And insert into "contact" table
      | Joe | Jackson | Red Hat | db |

    # start integration and check that DM exists
    And Integration "twitter-direct-messages-test-send" is present in integrations list
    And wait until integration "twitter-direct-messages-test-send" gets into "Running" state
    And wait until integration twitter-direct-messages-test-send processed at least 1 message

    Then check that account "Twitter Talky" has DM from user "Twitter Listener" with text "Red Hat"
