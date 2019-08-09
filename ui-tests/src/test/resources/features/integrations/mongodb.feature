# @sustainer: asmigala@redhat.com

@ui
@mongodb
@database
@integrations-mongodb
Feature: MongoDB

  Background: Clean application state
    Given clean application state
    And deploy MongoDB 3.6 database
    And connect to MongoDB "mongodb36"
    And log into the Syndesis
    And created connections
      | MongoDB | mongodb36 | Mongo36 | Mongo description |


  @mongodb-insert
  @mongodb-consume
  Scenario: MongoDB insert
    Given create mongodb capped collection "capped_for_insert" with size 1000 and max 3
    And create mongodb collection "insert_collection"
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Mongo36" connection
    And select "Mongo consumer" integration action
    And fill in values by element data-testid
      | collection               | capped_for_insert |
      | tailtrackincreasingfield | _id               |
    And click on the "Next" button

    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action
    And fill in values by element data-testid
      | collection | insert_collection |
      | operation  | Insert            |
    And click on the "Next" button

    And publish integration
    And set integration name "Mongo to Mongo insert"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Mongo to Mongo insert" gets into "Running" state

    When insert the following documents into mongodb collection "capped_for_insert"
      | _id | value |
      | id1 | v1    |

    Then verify that mongodb collection "insert_collection" has 1 document matching
      | _id | value |
      | id1 | v1    |
    And validate that logs of integration "Mongo to Mongo insert" contains string "\"value\" : \"v1\""


  @mongodb-update
  Scenario: MongoDB update
    When create mongodb collection "update_collection"
    And insert the following documents into mongodb collection "update_collection"
      | _id | value |
      | id1 | old   |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | mongodb-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action
    And fill in values by element data-testid
      | collection | update_collection |
      | operation  | Update            |
    And click on the "Next" button

    And publish integration
    And set integration name "Webhook to Mongo update"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo update" gets into "Running" state
    And sleep for 20 seconds

    When invoke post request to webhook in integration webhook-to-mongo-update with token mongodb-webhook and body:
      """
         [{"_id": "id1"}, {"$set": {"value": "new"}}]
      """

    Then verify that mongodb collection "update_collection" has 1 document matching
      | _id | value |
      | id1 | new   |


  @mongodb-find-all
  Scenario: MongoDB find all
    When create mongodb collection "findall_collection"
    And insert the following documents into mongodb collection "findall_collection"
      | _id | value |
      | id1 | v1    |
      | id2 | v2    |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | mongodb-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action
    And fill in values by element data-testid
      | collection | findall_collection |
      | operation  | Find all           |
    And click on the "Next" button

    And publish integration
    And set integration name "Webhook to Mongo find all"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo find all" gets into "Running" state
    And sleep for 20 seconds

    When invoke post request to webhook in integration webhook-to-mongo-find-all with token mongodb-webhook and body:
      """
      """

    And validate that logs of integration "Webhook to Mongo find all" contains string:
      """
        [[{ "_id" : "id1", "value" : "v1" }, { "_id" : "id2", "value" : "v2" }]]
      """

  @mongodb-remove
  Scenario: MongoDB remove
    When create mongodb collection "remove_collection"
    And insert the following documents into mongodb collection "remove_collection"
      | _id | value |
      | id1 | v1    |
      | id2 | v2    |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | mongodb-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action
    And fill in values by element data-testid
      | collection | remove_collection |
      | operation  | Remove            |
    And click on the "Next" button

    And publish integration
    And set integration name "Webhook to Mongo remove"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo remove" gets into "Running" state
    And sleep for 20 seconds

    When invoke post request to webhook in integration webhook-to-mongo-remove with token mongodb-webhook and body:
      """
        {"_id": "id1"}
      """

    Then verify that mongodb collection "remove_collection" has 0 document matching
      | _id |
      | id1 |
    Then verify that mongodb collection "remove_collection" has 1 document matching
      | _id |
      | id2 |


  @mongodb-count
  Scenario: MongoDB count
    When create mongodb collection "count_collection"
    And insert the following documents into mongodb collection "count_collection"
      | id  | value |
      | id1 | v1    |
      | id2 | v2    |
      | id3 | v1    |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | mongodb-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action
    And fill in values by element data-testid
      | collection | count_collection |
      | operation  | Count            |
    And click on the "Next" button

    And publish integration
    And set integration name "Webhook to Mongo count"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo count" gets into "Running" state
    And sleep for 20 seconds

    When invoke post request to webhook in integration webhook-to-mongo-count with token mongodb-webhook and body:
      """
        {"value": "v1"}
      """

    And validate that logs of integration "Webhook to Mongo count" contains string:
      """
        {"count": 2}
      """

  # save behaves like upsert - if an _id is given and the item exists,
  # it will update, other will create
  @mongodb-save
  Scenario: MongoDB save
    When create mongodb collection "save_collection"
    And insert the following documents into mongodb collection "save_collection"
      | _id | value |
      | id1 | old   |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | mongodb-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action
    And fill in values by element data-testid
      | collection | save_collection |
      | operation  | Save            |
    And click on the "Next" button

    And publish integration
    And set integration name "Webhook to Mongo save"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo save" gets into "Running" state
    And sleep for 20 seconds

    # update
    When invoke post request to webhook in integration webhook-to-mongo-save with token mongodb-webhook and body:
      """
         {"_id": "id1", "value": "new"}
      """
    Then verify that mongodb collection "save_collection" has 1 document matching
      | _id | value |
      | id1 | new   |

    # insert
    When invoke post request to webhook in integration webhook-to-mongo-save with token mongodb-webhook and body:
      """
         {"_id": "id2", "value": "v2"}
      """
    Then verify that mongodb collection "save_collection" has 1 document matching
      | _id | value |
      | id2 | v2    |


  @mongodb-find-by-id
  Scenario: MongoDB find-by-id
    When create mongodb collection "find-by-id_collection"
    And insert the following documents into mongodb collection "find-by-id_collection"
      | _id | value |
      | id1 | v1    |
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | mongodb-webhook |
    And click on the "Next" button
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Mongo producer" integration action

    # workaround for unselectable Next when using the default
    And fill in values by element data-testid
      | operation | Save |

    And fill in values by element data-testid
      | collection | find-by-id_collection |
      | operation  | Find by id            |
    And click on the "Next" button

    And publish integration
    And set integration name "Webhook to Mongo find by id"
    And publish integration

    Then navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo find by id" gets into "Running" state
    And sleep for 20 seconds

    # update
    When invoke post request to webhook in integration webhook-to-mongo-find-by-id with token mongodb-webhook and body:
      """
         id1
      """
    And validate that logs of integration "Webhook to Mongo find by id" contains string:
      """
        { "_id" : "id1", "value" : "v1" }
      """
