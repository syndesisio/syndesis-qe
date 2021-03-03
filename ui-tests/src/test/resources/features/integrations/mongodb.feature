# @sustainer: mkralik@redhat.com

@ui
@mongodb
@webhook
@database
@integrations-mongodb
@long-running
Feature: MongoDB

  Background: Clean application state
    Given clean application state
    And reset content of "todo" table
    And deploy MongoDB 3.6 database
    And connect to MongoDB "mongodb36"
    And log into the Syndesis
    And created connections
      | MongoDB | mongodb36 | Mongo36 | Mongo description |


  @mongodb-consume-stream
  Scenario: MongoDB consume stream
    When create mongodb collection "insert_stream"
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Mongo36" connection
    And select "Retrieve documents stream-" integration action
    And fill in values by element data-testid
      | collection               | insert_stream |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "1"
    And select "Split" integration step
    And click on the "Next" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | value | task |
    And click on the "Done" button

    And publish integration
    And set integration name "Mongo to DB consume stream"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Mongo to DB consume stream" gets into "Running" state
    And sleep for 10 seconds

    And insert the following documents into mongodb collection "insert_stream"
      | _id | value |
      | id1 | v1    |
      | id2 | v2    |
    And wait until integration Mongo to DB consume stream processed at least 2 messages

    Then check that query "select * from todo where task='v1'" has 1 row output
    And check that query "select * from todo where task='v2'" has 1 row output
    And check that query "select * from todo where task='v3'" has 0 row output
    And check that query "select * from todo where task='v4'" has 0 row output
    And validate that logs of integration "Mongo to DB consume stream" contains string:
      """
        [[{ "_id" : "id1", "value" : "v1" }]]
      """
    And validate that logs of integration "Mongo to DB consume stream" contains string:
      """
        [[{ "_id" : "id2", "value" : "v2" }]]
      """

    When insert the following documents into mongodb collection "insert_stream"
      | _id | value |
      | id3 | v3    |
      | id4 | v4    |
    And wait until integration Mongo to DB consume stream processed at least 4 messages
    Then check that query "select * from todo where task='v1'" has 1 row output
    And check that query "select * from todo where task='v2'" has 1 row output
    And check that query "select * from todo where task='v3'" has 1 row output
    And check that query "select * from todo where task='v4'" has 1 row output
    And validate that logs of integration "Mongo to DB consume stream" contains string:
      """
        [[{ "_id" : "id4", "value" : "v4" }]]
      """
    And validate that logs of integration "Mongo to DB consume stream" contains string:
      """
        [[{ "_id" : "id4", "value" : "v4" }]]
      """

  @mongodb-insert
  @mongodb-consume-capped
  Scenario: MongoDB insert and consume tail
    Given create mongodb capped collection "capped_for_insert" with size 1000 and max 3
    And create mongodb collection "insert_collection"
    And navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "Mongo36" connection
    And select "Retrieve documents tail-" integration action
    And fill in values by element data-testid
      | collection               | capped_for_insert |
      | tailtrackincreasingfield | _id               |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | [{"_id": "id1", "value": "new"},{"_id": "id2", "value": "new"}] |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | inputInstance |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Insert" integration action
    And fill in values by element data-testid
      | collection | insert_collection |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Split" integration step
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | value | value |
    And click on the "Done" button

    And publish integration
    And set integration name "Mongo to Mongo insert"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Mongo to Mongo insert" gets into "Running" state

    And insert the following documents into mongodb collection "capped_for_insert"
      | _id | value |
      | id1 | v1    |
    And wait until integration Mongo to Mongo insert processed at least 1 message

    Then verify that mongodb collection "insert_collection" has 1 document matching
      | value |
      | v1    |
    And validate that logs of integration "Mongo to Mongo insert" contains string:
      """
        "value" : "v1" }
      """
    And verify that mongodb collection "insert_collection" has 0 document matching
      | value |
      | v2    |

    When insert the following documents into mongodb collection "capped_for_insert"
      | _id | value |
      | id2 | v2    |
    Then verify that mongodb collection "insert_collection" has 1 document matching
      | value |
      | v2    |
    And validate that logs of integration "Mongo to Mongo insert" contains string:
      """
        "value" : "v2" }
      """

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
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"_id":"id1", "value":"new"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | inputInstance |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Update" integration action
    And fill in values by element data-testid
      | collection       | update_collection              |
      | filter           | {"_id": ":#id"}                |
      | updateexpression | {"$set": {"value": ":#value"}} |

    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | _id   | id    |
      | value | value |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to Mongo update"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo update" gets into "Running" state

    Then verify that mongodb collection "update_collection" has 1 document matching
      | _id | value |
      | id1 | old   |

    When invoke post request to webhook in integration Webhook to Mongo update with token mongodb-webhook and body:
      """
         {"_id":"id1", "value":"new"}
      """
    Then verify that mongodb collection "update_collection" has 1 document matching
      | _id | value |
      | id1 | new   |
    And validate that logs of integration "Webhook to Mongo update" contains string:
      """
        [1]
      """

    When invoke post request to webhook in integration Webhook to Mongo update with token mongodb-webhook and body:
      """
         {"_id":"id3", "value":"new"}
      """
    And validate that logs of integration "Webhook to Mongo update" contains string:
      """
        []
      """

  # Webhook - Mongo findAll - DB
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

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Find" integration action
    And fill in values by element data-testid
      | collection | findall_collection |
    And click on the "Next" button

    And add integration step on position "1"
    And select "Log" integration step
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "2"
    And select "Split" integration step
    And click on the "Next" button

    And add integration step on position "3"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | value | task |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to Mongo find all"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo find all" gets into "Running" state

    When invoke post request to webhook in integration Webhook to Mongo find all with token mongodb-webhook and body:
      """
      """

    Then validate that logs of integration "Webhook to Mongo find all" contains string:
      """
        [[{ "_id" : "id1", "value" : "v1" }, { "_id" : "id2", "value" : "v2" }]]
      """

    And check that query "select * from todo where task='v1'" has 1 row output
    And check that query "select * from todo where task='v2'" has 1 row output

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
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"value": "v1"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | inputInstance |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Delete" integration action
    And fill in values by element data-testid
      | collection | remove_collection    |
      | filter     | {"value": ":#value"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | value | value |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to Mongo remove"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo remove" gets into "Running" state

    When invoke post request to webhook in integration Webhook to Mongo remove with token mongodb-webhook and body:
      """
        {"value": "v1"}
      """

    Then verify that mongodb collection "remove_collection" has 0 document matching
      | _id | value |
      | id1 | v1    |
    And verify that mongodb collection "remove_collection" has 1 document matching
      | _id | value |
      | id2 | v2    |
    And validate that logs of integration "Webhook to Mongo remove" contains string:
      """
        [1]
      """

    When invoke post request to webhook in integration Webhook to Mongo remove with token mongodb-webhook and body:
      """
        {"value": "v1"}
      """
    And verify that mongodb collection "remove_collection" has 1 document matching
      | _id | value |
      | id2 | v2    |
    And validate that logs of integration "Webhook to Mongo remove" contains string:
      """
        [0]
      """

  @mongodb-count
  Scenario: MongoDB count
    When create mongodb collection "count_collection"
    And insert the following documents into mongodb collection "count_collection"
      | _id | value |
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
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"value": "v1"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | inputInstance |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Count" integration action
    And fill in values by element data-testid
      | collection | count_collection     |
      | filter     | {"value": ":#value"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | value | value |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to Mongo count"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo count" gets into "Running" state

    And invoke post request to webhook in integration Webhook to Mongo count with token mongodb-webhook and body:
      """
        {"value": "v1"}
      """
    Then validate that logs of integration "Webhook to Mongo count" contains string:
      """
        [2]
      """

    When invoke post request to webhook in integration Webhook to Mongo count with token mongodb-webhook and body:
      """
        {"value": "v2"}
      """
    Then validate that logs of integration "Webhook to Mongo count" contains string:
      """
        [1]
      """

    When invoke post request to webhook in integration Webhook to Mongo count with token mongodb-webhook and body:
      """
        {"value": "v3"}
      """
    Then validate that logs of integration "Webhook to Mongo count" contains string:
      """
        [0]
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
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"_id": "id1", "value": "new"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | inputInstance |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Upsert" integration action
    And fill in values by element data-testid
      | collection | save_collection                     |
      | filter     | {"_id": ":#id", "value": ":#value"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | _id   | id    |
      | value | value |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to Mongo save"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo save" gets into "Running" state

    Then verify that mongodb collection "save_collection" has 1 document matching
      | _id | value |
      | id1 | old   |

    # update
    When invoke post request to webhook in integration Webhook to Mongo save with token mongodb-webhook and body:
      """
         {"_id": "id1", "value": "new"}
      """
    Then verify that mongodb collection "save_collection" has 1 document matching
      | _id | value |
      | id1 | new   |
    And verify that mongodb collection "save_collection" has 0 document matching
      | _id | value |
      | id2 | v2    |

    # insert
    When invoke post request to webhook in integration Webhook to Mongo save with token mongodb-webhook and body:
      """
         {"_id": "id2", "value": "v2"}
      """
    Then verify that mongodb collection "save_collection" has 1 document matching
      | _id | value |
      | id2 | v2    |

  # Webhook - Mongo find by id - DB
  @mongodb-find-by-id
  Scenario: MongoDB find-by-id
    When create mongodb collection "find-by-id_collection"
    And insert the following documents into mongodb collection "find-by-id_collection"
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
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"inputId": "1"} |
    And fill in values by element data-testid
      | describe-data-shape-form-name-input | inputInstance |
    And click on the "Next" button
    Then check visibility of page "Choose a Finish Connection"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Mongo36" connection
    And select "Find" integration action
    And fill in values by element data-testid
      | collection | find-by-id_collection |
      | filter     | {"_id": ":#id"}       |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | inputId | id |
    And click on the "Done" button

    And add integration step on position "2"
    And select the "Log" connection
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "3"
    And select "Split" integration step
    And click on the "Next" button

    And add integration step on position "4"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | value | task |
    And click on the "Done" button

    And publish integration
    And set integration name "Webhook to Mongo find by id"
    And publish integration

    And navigate to the "Integrations" page
    And wait until integration "Webhook to Mongo find by id" gets into "Running" state

    And invoke post request to webhook in integration Webhook to Mongo find by id with token mongodb-webhook and body:
      """
         {"inputId": "id1"}
      """
    Then validate that logs of integration "Webhook to Mongo find by id" contains string:
      """
        { "_id" : "id1", "value" : "v1" }
      """
    And check that query "select * from todo where task='v1'" has 1 row output
    And check that query "select * from todo where task='v2'" has no output

    # Try to find an element which doesn't exist
    When invoke post request to webhook in integration Webhook to Mongo find by id with token mongodb-webhook and body:
      """
         {"inputId": "id3"}
      """
    Then validate that logs of integration "Webhook to Mongo find by id" contains string:
      """
        []
      """
