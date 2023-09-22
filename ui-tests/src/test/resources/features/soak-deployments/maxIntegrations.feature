# @sustainer: mkralik@redhat.com

@ui
@oauth
@long-running
@max-integrations
Feature: Test maximum integration

  Background: Clean application state and prepare what is needed
    Given log into the Syndesis
    And navigate to the "Home" page
    And clean application state

  @10-integrations-basic
  Scenario: Test whether 10 integrations are successfully deployed - basic scenario
    When navigate to the "Home" page
    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration1"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration2"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration3"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration4"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration5"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration6"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration7"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration8"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration9"
    And publish integration
    And navigate to the "Integrations" page

    And click on the "Create Integration" link to create a new integration
    And check that position of connection to fill is "Start"
    And select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | RedHat integration |
    And click on the "Next" button
    And publish integration
    And set integration name "Integration10"
    And publish integration
    And navigate to the "Integrations" page

    And wait until integration "Integration1" gets into "Running" state
    And wait until integration "Integration2" gets into "Running" state
    And wait until integration "Integration3" gets into "Running" state
    And wait until integration "Integration4" gets into "Running" state
    And wait until integration "Integration5" gets into "Running" state
    And wait until integration "Integration6" gets into "Running" state
    And wait until integration "Integration7" gets into "Running" state
    And wait until integration "Integration8" gets into "Running" state
    And wait until integration "Integration9" gets into "Running" state
    And wait until integration "Integration10" gets into "Running" state

    And wait until integration Integration1 processed at least 1 message
    And wait until integration Integration2 processed at least 1 message
    And wait until integration Integration3 processed at least 1 message
    And wait until integration Integration4 processed at least 1 message
    And wait until integration Integration5 processed at least 1 message
    And wait until integration Integration6 processed at least 1 message
    And wait until integration Integration7 processed at least 1 message
    And wait until integration Integration8 processed at least 1 message
    And wait until integration Integration9 processed at least 1 message
    And wait until integration Integration10 processed at least 1 message

    Then validate that logs of integration "Integration1" contains string "RedHat integration"
    And validate that logs of integration "Integration2" contains string "RedHat integration"
    And validate that logs of integration "Integration3" contains string "RedHat integration"
    And validate that logs of integration "Integration4" contains string "RedHat integration"
    And validate that logs of integration "Integration5" contains string "RedHat integration"
    And validate that logs of integration "Integration6" contains string "RedHat integration"
    And validate that logs of integration "Integration7" contains string "RedHat integration"
    And validate that logs of integration "Integration8" contains string "RedHat integration"
    And validate that logs of integration "Integration9" contains string "RedHat integration"
    And validate that logs of integration "Integration10" contains string "RedHat integration"

  @prod
  @chrome-only
  @10-integrations-complex
  Scenario: Test whether 10 integrations are successfully deployed - complex scenario
    # pre-scenario setup
    When delete contact from SF with email: "test@maxIntegration.feature"
    Then check SF "does not contain" contact with a email: "test@maxIntegration.feature"
    When send message "StartingNewTest" on channel "max-integrations"
    And reset content of "contact" table
    And reset content of "todo" table
    And delete emails from "QE Google Mail" with subject "MaxIntegrations"
#    And delete all direct messages received by "Twitter Listener" with text "MaxIntegrations"
    And delete incidents with "MaxIntegrations" number

    #todo app
    When wait for Todo to become ready
    And Set Todo app credentials
    And click on the "Customizations" link
    And navigate to the "API Client Connectors" page
    And click on the "Create API Connector" link
    And check visibility of page "Upload Swagger Specification"
    And upload TODO API swagger from URL
    And click on the "Next" button
    Then check visibility of page "Review Actions"
    When click on the "Next" link
    Then check visibility of page "Specify Security"
    #fill in dummy credentials
    And fill in values by element ID
      | username | dummy |
      | password | dummy |
    When click on the "Next" button
    And fill in values by element data-testid
      | name     | Todo connector |
      | basepath | /api           |
    And fill in TODO API host URL
    And click on the "Save" button
    And navigate to the "Home" page

#    #AMQ, Slack
    And deploy ActiveMQ broker
    And created connections
      | Red Hat AMQ    | AMQ_PROD   | AMQ             | AMQ on OpenShift       |
      | Slack          | QE Slack   | QE Slack        | SyndesisQE Slack test  |
      | Todo connector | todo       | TODO connection | no validation          |
      | ServiceNow     | Servicenow | ServiceNow      | Service-Now connection |

    And navigate to the "Settings" page
    And fill "Salesforce" oauth settings "QE Salesforce"
    And create connections using oauth
      | Salesforce | QE Salesforce |

#    And clean all tweets in twitter_talky account
#    And navigate to the "Settings" page
#    And fill "Twitter" oauth settings "Twitter Listener"
#    And create connections using oauth
#      | Twitter | Twitter Listener |

    And deploy MongoDB 3.6 database
    And connect to MongoDB "mongodb36"
    And created connections
      | MongoDB | mongodb36 | Mongo36 | Mongo description |
    And create mongodb collection "save_collection"

    And navigate to the "Settings" page
    And fill "Gmail" oauth settings "QE Google Mail"
    And create connections using oauth
      | Gmail | My GMail Connector |

    And navigate to the "Home" page

    # Integration1 Webhook->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "Webhook" connection
    And select "Incoming Webhook" integration action
    And fill in values by element data-testid
      | contextpath | test-webhook |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration2 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration1_webhook"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration1_webhook" gets into "Running" state

    # Integration2 Amq->Slack->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration2 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration3 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "QE Slack" connection
    And select "Channel" integration action
    And select "max-integrations" from slack channel dropdown
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "message" with value "MaxIntegrations" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | message | message |
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration2_slack"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration2_slack" gets into "Running" state

    # Integration3 Amq->DB->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration3 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration4 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into contact values ('MaxIntegrations', 'Jackson', 'MaxIntegrations', 'db', '2018-03-23');" value
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration3_db"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration3_db" gets into "Running" state

    # Integration4 Amq->Log->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration4 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration5 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "Log" connection
    And fill in values by element data-testid
      | customtext | MaxIntegrations |
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration4_log"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration4_log" gets into "Running" state

    # Integration5 Amq->Salesforce->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration5 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration6 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "QE Salesforce" connection
    And select "Create or update record" integration action
    And select "Contact" from "sobjectname" dropdown
    And click on the "Next" button
    And select "TwitterScreenName" from "sobjectidname" dropdown
    And click on the "Done" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "message" with value "MaxIntegrations" of type "String" in data mapper
    And define constant "email" with value "test@maxIntegration.feature" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | message | TwitterScreenName__c |
      | message | Description          |
      | message | FirstName            |
      | message | LastName             |
      | email   | Email                |
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration5_sf"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration5_sf" gets into "Running" state

    # Integration6 Amq->Twitter->AmQ
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration6 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration7 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

#    And add integration step on position "0"
#    And select the "Twitter Listener" connection
#    And select "Send" integration action
#    And fill in values by element data-testid
#      | message | MaxIntegrations |
#    And fill username for "twitter_talky" account
#    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration6_tw"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration6_tw" gets into "Running" state

    # Integration7 Amq->Mongo->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration7 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration8 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
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
    And define constant "id" with value "MaxIntegrations" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | id | id    |
      | id | value |
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration7_mongo"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration7_mongo" gets into "Running" state

    # Integration8 Amq->Gmail->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration8 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration9 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    And select the "My GMail Connector" connection
    And select "Send Email" integration action
    And fill in values by element data-testid
      | subject | maxIntegrations |
    And fill in data-testid field "to" from property "email" of credentials "QE Google Mail"
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "message" with value "MaxIntegrations" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | message | text |
    And scroll "top" "right"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration8_gmail"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration8_gmail" gets into "Running" state

    # Integration9 Amq->serviceNow->Amq
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration9 |
      | destinationtype | Queue        |
    And click on the "Next" button
    And click on the "Next" button

    And select the "AMQ" connection
    And select "Publish messages" integration action
    And fill in values by element data-testid
      | destinationname | integration10 |
      | destinationtype | Queue         |
    And click on the "Next" button
    And force fill in values by element data-testid
      | describe-data-shape-form-kind-input | JSON Instance |
    And fill text into text-editor
      | {"previous":"ok"} |
    And click on the "Next" button

    And add integration step on position "0"
    When select the "ServiceNow" connection
    And select "Add Record" integration action
    And select "Qa Create Incident" from "table" dropdown
    Then click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step

    And define constant "maxDesc" with value "MaxIntegrationsDescription" of type "String" in data mapper
    And define constant "imp" with value "1" of type "Integer" in data mapper
    And define constant "urgency" with value "2" of type "String" in data mapper
    And define constant "desc" with value "description" of type "String" in data mapper
    And define constant "input" with value "ui" of type "String" in data mapper
    And create data mapper mappings
      | maxDesc | u_description       |
      | imp     | u_impact            |
      | urgency | u_urgency           |
      | desc    | u_short_description |
      | input   | u_user_input        |
    And define modified service now number "MaxIntegrations" and map it to "u_number"
    And click on the "Done" button

    And add integration step on position "2"
    And select "Data Mapper" integration step
    And define constant "ok" with value "ok" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | ok | previous |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration9_serviceNow"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration9_serviceNow" gets into "Running" state

    # Integration10 Amq -> API Client Connector
    When click on the "Create Integration" link to create a new integration
    And select the "AMQ" connection
    And select "Subscribe for Messages" integration action
    And fill in values by element data-testid
      | destinationname | integration10 |
      | destinationtype | Queue         |
    And click on the "Next" button
    And click on the "Next" button

    And select the "TODO connection" connection
    And select "Create new task" integration action
    And click on the "Next" button

    And add integration step on position "0"
    And select "Data Mapper" integration step
    And define constant "text" with value "MaxIntegrations" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | text | body.task |
    And click on the "Done" button

    And publish integration
    And set integration name "Integration10_todo"
    And publish integration
    And navigate to the "Integrations" page
    And wait until integration "Integration10_todo" gets into "Running" state

    When invoke post request to webhook in integration Integration1_webhook with token test-webhook and body {}
    And wait until integration Integration1_webhook processed at least 1 message
    And wait until integration Integration2_slack processed at least 1 message
    And wait until integration Integration3_db processed at least 1 message
    And wait until integration Integration4_log processed at least 1 message
    And wait until integration Integration5_sf processed at least 1 message
    And wait until integration Integration6_tw processed at least 1 message
    And wait until integration Integration7_mongo processed at least 1 message
    And wait until integration Integration8_gmail processed at least 1 message
    And wait until integration Integration9_serviceNow processed at least 1 message
    And wait until integration Integration10_todo processed at least 1 messages

    #Verification
    Then check that last slack message equals "MaxIntegrations" on channel "max-integrations"
    And check that query "select * from contact where company = 'MaxIntegrations'" has some output
    And validate that logs of integration "Integration4" contains string "MaxIntegrations"
    And check SF "contains" contact with a email: "test@maxIntegration.feature"
    And verify that mongodb collection "save_collection" has 1 document matching
      | _id             | value           |
      | MaxIntegrations | MaxIntegrations |
    And check that email from "QE Google Mail" with subject "maxIntegrations" and text "MaxIntegrations" exists
    And verify that incident with "MaxIntegrations" number has "MaxIntegrationsDescription" description
    And check that query "select * from todo where task='MaxIntegrations'" has 1 row output

    When delete contact from SF with email: "test@maxIntegration.feature"
    And delete emails from "QE Google Mail" with subject "maxIntegrations"


