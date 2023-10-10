# @sustainer: tplevko@redhat.com

@rest
@salesforce
@database
@datamapper
Feature: Integration - Salesforce

  Background: Clean application state
    Given clean SF, removes all leads with email: "jdoeprod@sf-producers.feature,joedoeprod@sf-producers.feature,test@integration-sf-db.feature,test@salesfoce-to-database.feature"
    And remove all records from table "todo"
    And create SalesForce connection

  @integration-sf-db @integration-sf-db-create
  Scenario: On create to DB
    When create SF "salesforce-on-create" action step with properties
      | sObjectName | Lead |
    And start mapper definition with name: "mapping 1"
    Then MAP using Step 1 and field "/Company" to "/company"
    Then MAP using Step 1 and field "/Email" to "/email"
    Then COMBINE using Step 1 and strategy "Space" into "/first_and_last_name" and sources
      | /FirstName | /LastName |
    And create finish DB invoke stored procedure "add_lead" action step
    When create integration with name: "SF create to DB rest test"
    Then wait for integration with name: "SF create to DB rest test" to become active
    Then create SF lead with first name: "John", last name: "Doe", email: "test@integration-sf-db.feature" and company: "ACME"
    And wait until integration SF create to DB rest test processed at least 1 message
    Then validate DB created new lead with first name: "John", last name: "Doe", email: "test@integration-sf-db.feature"

  @integration-sf-db @integration-sf-db-delete
  Scenario: On delete to DB
    Then create SF lead with first name: "John", last name: "Doe", email: "test@integration-sf-db.feature" and company: "ACME"
    When create SF "salesforce-on-delete" action step with properties
      | sObjectName | Lead |
    And start mapper definition with name: "mapping 1"
    Then MAP using Step 1 and field "/id" to "/<>/todo"
    And create finish DB invoke sql action step with query "INSERT INTO TODO(task) VALUES(:#todo)"
    When create integration with name: "SF delete to DB rest test"
    Then wait for integration with name: "SF delete to DB rest test" to become active
    Then delete lead from SF with email: "test@integration-sf-db.feature"
    And wait until integration SF delete to DB rest test processed at least 1 message
    Then validate SF on delete to DB created new task

  @integration-sf-db @integration-sf-db-update
  Scenario: On update to DB
    Then create SF lead with first name: "John", last name: "Doe", email: "test@integration-sf-db.feature" and company: "ACME"
    When create SF "salesforce-on-update" action step with properties
      | sObjectName | Lead |
    And start mapper definition with name: "mapping 1"
    Then COMBINE using Step 1 and strategy "Space" into "/<>/todo" and sources
      | /FirstName | /LastName | /Email |
    And create finish DB invoke sql action step with query "INSERT INTO TODO(task) VALUES(:#todo)"
    When create integration with name: "SF update to DB rest test"
    Then wait for integration with name: "SF update to DB rest test" to become active
    Then update SF lead with email "test@integration-sf-db.feature" to first name: "Joe", last name "Carrot", email "test@integration-sf-db.feature", company name "EMCA"
    And wait until integration SF update to DB rest test processed at least 1 message
    Then validate DB created new lead with first name: "Joe", last name: "Carrot", email: "test@integration-sf-db.feature"
