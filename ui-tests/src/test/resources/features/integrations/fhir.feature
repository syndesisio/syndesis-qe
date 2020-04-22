# @sustainer: sveres@redhat.com

@ui
@fhir
Feature: Integration - FHIR - all actions

#  Gherkin does not support (and it is deprecated) to have one background step for all scenarios
#  so server needs to be deployed / undeployed each time.

  Background: Clean application state
    Given clean application state
    And add FHIR account
    And undeploy FHIR server
    And deploy FHIR server
    When delete all relevant entities on FHIR server
    And reset content of "todo" table
    And reset content of "CONTACT" table
    Given log into the Syndesis
    And created connections
      | FHIR | FHIR | FHIR | Description |

  @fhir-0-create
  Scenario: FHIR create operation

    And inserts into "CONTACT" table
      | Emil | Hacik | Red Hat | db |
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

#start connection
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT" value
    Then fill in period input with "5" value
    Then select "Minutes" from sql dropdown
    And click on the "Next" button

#finish connection
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |
    Then click on the "Next" button

#FHIR create
    When add integration step on position "0"
    When select the "FHIR" connection
    And select "Create" integration action
    And select resource type "Patient"
    And click on the "Next" button

#datamapper before FHIR create
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | first_name | Patient.name.given.value  |
      | last_name  | Patient.name.family.value |
    And click on the "Done" button

#run the integration
    When click on the "Save" link
    And set integration name "FHIR_read"
    And publish integration
    Then Integration "FHIR_read" is present in integrations list
    And wait until integration "FHIR_read" gets into "Running" state

    When sleep for "10000" ms
    And validate that patient with name "Emil Hacik" is in FHIR
    Then validate that logs of integration "FHIR_read" contains string "<name><tns:given value="Emil"/><tns:family value="Hacik"/></name>"

  @fhir-1-delete
  Scenario: FHIR delete operation

    When create patient with name "Duro Mrdar" on FHIR and put it into DB

    Then navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

  #start connection
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT lead_source FROM CONTACT WHERE last_name = 'Mrdar'" value
    Then fill in period input with "30" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

  #finish connection - FHIR delete
    When select the "FHIR" connection
    And select "Delete" integration action
    And select resource type "Patient"
    And click on the "Next" button

  #datamapper before FHIR delete
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | lead_source | id |
    And click on the "Done" button

  #run the integration
    When click on the "Save" link
    And set integration name "FHIR_delete"
    And publish integration
    Then Integration "FHIR_delete" is present in integrations list
    And wait until integration "FHIR_delete" gets into "Running" state

    When sleep for "10000" ms
    Then validate that patient with name "Duro Mrdar" is not in FHIR

#  just solve todo
  @ignore
  @fhir-2-patch
  Scenario: FHIR patch operation

    When create patient with name "Duro Mrdar" on FHIR and put it into DB
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

  #start connection
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT WHERE last_name = 'Mrdar'" value
    Then fill in period input with "10" value
    Then select "Minutes" from sql dropdown
    And click on the "Next" button

  #finish connection FHIR patch
    When select the "FHIR" connection
    And select "Patch" integration action
    And select resource type "Patient"
    And fill in values by element data-testid
#    todo: play little bit with exact form of this patch JSON:
#    does not work:
#      | patch | [{ "op": "replace", "path": "/HumanName.family", "value": "Mestanek" }] |
#      | patch | [{ "op": "replace", "path": "HumanName.family", "value": "Mestanek" }] |
#      | patch | { "op": "replace", "path": "HumanName.family", "value": "Mestanek" } |
#    to be tried:
      | patch | [{ "op": "replace", "path": "Patient", "value" : {"name" : "family", "value" : "Mestanek"} }] |
#      | patch | [{ "op": "replace", "path": "Patient", "value" : {"name" : "HumanName.family", "value" : "Mestanek"} }] |
    And click on the "Next" button

  #datamapper before FHIR patch
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | lead_source | id |
    And click on the "Done" button

  #run the integration
    When click on the "Save" link
    And set integration name "FHIR_patch"
    And publish integration
    Then Integration "FHIR_patch" is present in integrations list
    And wait until integration "FHIR_patch" gets into "Running" state

    When sleep for "10000" ms
    Then validate that last inserted patients name has been changed to "Duro Mestanek" in FHIR


  @fhir-3-read
  Scenario: FHIR read operation

    When create patient with name "Duro Mrdar" on FHIR and put it into DB
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

  #start connection
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT WHERE last_name = 'Mrdar'" value
    Then fill in period input with "30" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

#finish connection
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |
    Then click on the "Next" button

#FHIR read
    When add integration step on position "0"
    When select the "FHIR" connection
    And select "Read" integration action
    And select resource type "Patient"
    And click on the "Next" button

#datamapper before FHIR read
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
      | lead_source | id |
    And click on the "Done" button

#run the integration
    When click on the "Save" link
    And set integration name "FHIR_read"
    And publish integration
    Then Integration "FHIR_read" is present in integrations list
    And wait until integration "FHIR_read" gets into "Running" state

    When sleep for "10000" ms
    Then validate that logs of integration "FHIR_read" contains string "<name><tns:given value="Duro"/><tns:family value="Mrdar"/></name>"


  @fhir-4-search
  Scenario: FHIR search operation

    When create patient with name "Palo Matrtaj" on FHIR and put it into DB
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

  #start connection
    When select the "Timer" connection
    And select "Simple" integration action
    And fill in values by element data-testid
      | period          | 30      |
      | period-duration | Seconds |
    And click on the "Next" button

  #finish connection
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |
    Then click on the "Next" button

  #FHIR search
    When add integration step on position "0"
    When select the "FHIR" connection
    And select "Search" integration action
    And select resource type "Patient"
    And fill in values by element data-testid
      | query | given=Palo&family=Matrtaj |
    And click on the "Next" button

  #run the integration
    When click on the "Save" link
    And set integration name "FHIR_search"
    And publish integration
    Then Integration "FHIR_search" is present in integrations list
    And wait until integration "FHIR_search" gets into "Running" state

    When sleep for "10000" ms
    Then validate that logs of integration "FHIR_search" contains string "<name><family value="Matrtaj"/><given value="Palo"/></name>"


  @fhir-5-transaction
  Scenario: FHIR transaction operation

    When create patient with name "Duro Mrdar" on FHIR and put it into DB
    And invoke database query "UPDATE CONTACT SET first_name='Jozo', last_name='Matrtaj' WHERE last_name = 'Mrdar'"
    And create basic with language "magyar" on FHIR and put it into DB
    And invoke database query "UPDATE TODO SET task='slovak' WHERE task = 'magyar'"
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

  #start connection
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT WHERE last_name = 'Matrtaj'" value
    Then fill in period input with "30" value
    Then select "Seconds" from sql dropdown
    And click on the "Next" button

#finish Log connection
    When select the "Log" connection
    And fill in values by element data-testid
      | contextloggingenabled | true |
      | bodyloggingenabled    | true |
    Then click on the "Next" button

    #FHIR transaction
    When add integration step on position "0"
    When select the "FHIR" connection
    And select "Transaction" integration action
    And select contained resource types
      | Patient |
      | Basic   |
    And click on the "Next" button

  #second input connection
    When add integration step on position "0"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM TODO WHERE task = 'slovak'" value
    And click on the "Next" button

#datamapper before FHIR transaction
    When add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
#    must be `id-` in order not to collide with Patient.identifier
      | lead_source | Transaction.Patient.id-.value         |
      | first_name  | Transaction.Patient.name.given.value  |
      | last_name   | Transaction.Patient.name.family.value |
      | id          | Transaction.Basic.id-.value           |
      | task        | Transaction.Basic.language.value      |
    And click on the "Done" button

#run the integration
    When click on the "Save" link
    And set integration name "FHIR_transaction"
    And publish integration
    Then Integration "FHIR_transaction" is present in integrations list
    And wait until integration "FHIR_transaction" gets into "Running" state

    When sleep for "10000" ms
    Then validate that last inserted patients name has been changed to "Jozo Matrtaj" in FHIR
    Then validate that last inserted basics language has been changed to "slovak" in FHIR

  @fhir-6-update
  Scenario: FHIR update operation
    When create patient with name "Duro Mrdar" on FHIR and put it into DB
    And invoke database query "UPDATE CONTACT SET first_name='Jano', last_name='Matrtaj' WHERE last_name = 'Mrdar'"

    Then navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

  #start connection
    When select the "PostgresDB" connection
    And select "Periodic SQL invocation" integration action
    Then check "Next" button is "Disabled"
    Then fill in periodic query input with "SELECT * FROM CONTACT WHERE last_name = 'Matrtaj'" value
    Then fill in period input with "1" value
    Then select "Minutes" from sql dropdown
    And click on the "Next" button

  #finish connection: FHIR update
    When select the "FHIR" connection
    And select "Update" integration action
    And select resource type "Patient"
    And click on the "Next" button

  #datamapper before FHIR update
    When add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    And create data mapper mappings
#    must be `id-` in order not to collide with Patient.identified
      | lead_source | Patient.id-.value         |
      | first_name  | Patient.name.given.value  |
      | last_name   | Patient.name.family.value |
    And click on the "Done" button

  #run the integration
    When click on the "Save" link
    And set integration name "FHIR_update"
    And publish integration
    Then Integration "FHIR_update" is present in integrations list
    And wait until integration "FHIR_update" gets into "Running" state

    When sleep for "10000" ms
    Then validate that last inserted patients name has been changed to "Jano Matrtaj" in FHIR
    
