# @sustainer: mkralik@redhat.com

@ui
@database
@datamapper
@kafka
@integrations-db-to-kafka-to-db
@notIgnoreOpenIssue
@ENTESB-13113
Feature: Integration - DB to DB via Kafka

  Background: Clean application state
    Given clean application state
    And deploy Kafka broker
    And extract broker certificate
    And log into the Syndesis
    And reset content of "todo" table
    And reset content of "CONTACT" table

#    this sleep is necessary, because AMQ streams autodiscovery process takes some time so streams are not available immediately
#    and unfortunatelly there is no indication of autodiscovery process status yet. see issue ENTESB-13113
    When sleep for "150000" ms

#
#  1. select - update
#
  @db-to-kafka-to-db
  Scenario Outline:  From database to kafka <type> topic
#    A.db to kafka:
    When created Kafka connection using "<type>" security with name "Kafka Auto Detect QE <type>"
    And insert into "CONTACT" table
      | Joe-<type> | Jackson | Red Hat | db |

    And navigate to the "Home" page
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
    When select the "Kafka Auto Detect QE <type>" connection
    And select "Publish" integration action

    And fill in values by element data-testid
      | topic | auto-detect-<type> |
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
    And set integration name "db-to-kafka-<type> E2E"
    And publish integration

#    B.kafka to db:
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # select kafka connection as 'Start Connection'
    When select the "Kafka Auto Detect QE <type>" connection
    And select "Subscribe" integration action
    Then fill in values by element data-testid
      | topic | auto-detect-<type> |
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
    And set integration name "kafka-to-db-<type> E2E"
    And publish integration

    And wait until integration "db-to-kafka-<type> E2E" gets into "Running" state
    And wait until integration "kafka-to-db-<type> E2E" gets into "Running" state
    And wait until integration db-to-kafka-<type> E2E processed at least 1 message
    And wait until integration kafka-to-db-<type> E2E processed at least 1 message

    Then check that query "SELECT task FROM TODO WHERE task = 'Joe-tls' limit 1" has 1 row output

    Examples:
      | type  |
      | TLS   |
      | PLAIN |
