# @sustainer: sveres@redhat.com

@ui
@database
@datamapper
@kafka
@integrations-db-to-kafka-to-db
Feature: Integration - DB to DB

  Background: Clean application state
    Given clean application state
    And deploy Kafka broker and add account
    And log into the Syndesis
    And reset content of "todo" table
    And reset content of "CONTACT" table
    And inserts into "CONTACT" table
      | Joe | Jackson | Red Hat | db |
#    this sleep is necessary, because AMQ streams autodiscovery process takes some time so streams are not available immediately
#    and unfortunatelly there is no indication of autodiscovery process status yet.
    When sleep for "90000" ms
    And created Kafka connection using AMQ streams auto detection
      | Kafka Message Broker | Kafka Autodetect | Kafka Auto Detect QE | Kafka Streams Auto Detection |

#
#  1. select - update
#
  @db-to-kafka-to-db
  Scenario: from database to kafka topic
#    A.db to kafka:
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    #Then check visibility of page "Periodic SQL Invocation"
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT first_name FROM CONTACT" value
    Then fill in period input with "10" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

    # select kafka connection as 'Finish Connection'
    Then check visibility of page "Choose a Finish Connection"
    When select the "Kafka Auto Detect QE" connection
    And select "Publish" integration action

    And fill in values by element data-testid
      | topic | auto-detect |
    And click on the "Next" button
    And fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Schema |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | autoDetectType |
    And fill text into text-editor
      | {"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"firstName":{"type":"string"}},"required":["firstName"]} |
    And click on the "Next" button

    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | first_name | firstName |
    And click on the "Done" button
    And publish integration
    And set integration name "db-to-kafka E2E"
    And publish integration


#    B.kafka to db:
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select kafka connection as 'Start Connection'
    Then check visibility of page "Choose a Finish Connection"
    When select the "Kafka Auto Detect QE" connection
    And select "Subscribe" integration action
    Then fill in values by element data-testid
      | topic | auto-detect |
    And click on the "Next" button
    And fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Schema |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | autoDetectType |
    And fill text into text-editor
      | {"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"firstName":{"type":"string"}},"required":["firstName"]} |
    And click on the "Next" button

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "INSERT INTO TODO(task) VALUES(:#task)" value
    And click on the "Next" button

    # add data mapper step
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | firstName | task |
    And click on the "Done" button

    And publish integration
    And set integration name "kafka-to-db E2E"
    And publish integration

    Then wait until integration "db-to-kafka E2E" gets into "Running" state
    Then wait until integration "kafka-to-db E2E" gets into "Running" state

    Then checks that query "SELECT task FROM TODO WHERE task = 'Joe' limit 1" has "1" output
