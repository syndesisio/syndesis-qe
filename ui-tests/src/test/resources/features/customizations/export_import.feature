#@sustainer: mmuzikar@redhat.com

@ui
@customizations-export-import
Feature: Exporting and importing integrations using customizations (OpenAPI connectors & extensions)

    Background: 
        Given clean application state
        And log into the Syndesis
        And Set Todo app credentials
        And clean "todo" table

    @customizations-export
    Scenario: Creating and exporting an integration with API connector and extension
        When navigate to the "Customizations" page
        And create new API connector
            | source   | file          | swagger/connectors/todo.json  |
            | security | authType      | HTTP Basic Authentication     |
            | details  | connectorName | TODO-API                      |
            | details  | host          | http://todo.syndesis.svc:8080 |
            | details  | baseUrl       | /api                          |
        And click on the "Extensions" link
        And click on the "Import Extension" button
        And upload extension with name "syndesis-extension-body" from syndesis-extensions dir
        And click on the "Import Extension" button
        And navigate to the "Connections" page
        And created connections
            | TODO-API | todo | TODO-app | no validation |
        
        When navigate to the "Home" page
        And click on the "Create Integration" button
        Then check visibility of visual integration editor

        And check that position of connection to fill is "Start"
        When select the "Timer" connection
        And select "Simple Timer" integration action
        And click on the "Done" button

        Then check that position of connection to fill is "Finish"
        When select the "TODO-app" connection
        And select "Create new task" integration action

        And add integration step on position "0"
        And select the "Set Body" connection
        And fill in values
            | Body | { "body": { "task": "Testing extensions", "completed": "1" } } |
        And click on the "Done" button
        
        And click on the "Save" button
        And set integration name "Extension-export-test"
        #Modal shows up if you really want to exit the integration editor, so it's quicker to publish it and it 
        #never gets to run anyway because it gets exported and then the application state is clean
        And click on the "Publish" button

        #In rare chances there was nothing to export because there supposedly wasn't anything saved
        And sleep for jenkins delay or "1" seconds
        And export the integration
        And navigate to the "Integrations" page
        #For some reason just clearing the database in step clear application state does not fully delete the integration
        #causing imported integrations not to deploy (this doesn't happen when redeploying, it is only a quirk of our testsuite)
        And wait for integration with name: "Extension-export-test" to become active
        And delete the "Extension-export-test" integration

    @customizations-import-fresh
    Scenario: Importing integration from @customizations-export
        #Application should be in clean state -> customizations should be imported with the integration
        When navigate to the "Integrations" page
        And click on the "Import" button
        And import integration from relative file path "tmp/download/Extension-export-test-export.zip"

        And navigate to the "Integrations" page
        Then Integration "Extension-export-test" is present in integrations list

        When navigate to the "Connections" page
        And select the "TODO-app" connection
        And click on the "Edit" button
        And fill in "todo" connection details from connection edit page
        And click on the "Save" button

        When navigate to the "Integrations" page
        And select the "Extension-export-test" integration
        And click on the "Edit Integration" button
        And click on the "Publish" button

        And navigate to the "Integrations" page
        And wait for integration with name: "Extension-export-test" to become active
        Then validate that number of all todos with task "Testing extensions" is greater than "0"


    @customizations-import-resource
    Scenario: Importing integration from resources
        #Integration should still be importable between different versions of Syndesis
        #Application should be in clean state -> customizations should be imported with the integration
        When navigate to the "Integrations" page
        And click on the "Import" button
        And import integration from relative file path "src/test/resources/integrations/Extension-export-test-export.zip"

        And navigate to the "Integrations" page
        Then Integration "Extension-export-test" is present in integrations list

        And navigate to the "Connections" page
        And select the "TODO-app" connection
        And click on the "Edit" button
        And fill in "todo" connection details from connection edit page
        And click on the "Save" button

        When navigate to the "Integrations" page
        And select the "Extension-export-test" integration
        And click on the "Edit Integration" button
        And click on the "Publish" button

        And navigate to the "Integrations" page
        And wait for integration with name: "Extension-export-test" to become active
        Then validate that number of all todos with task "Testing extensions" is greater than "0"
    
    @customizations-duplicate-extension
    @gh-5420
    Scenario: Duplicating extensions when importing integrations should not be possible
        When navigate to the "Customizations" page
        And click on the "Extensions" link
        And click on the "Import Extension" button
        And upload extension with name "syndesis-extension-body" from syndesis-extensions dir
        And click on the "Import Extension" button

        And navigate to the "Integrations" page
        And click on the "Import" button
        And import integration from relative file path "src/test/resources/integrations/Extension-export-test-export.zip"

        And navigate to the "Customizations" page
        And click on the "Extensions" link
        #Imported integration contains set body and delay extension (delay is set to 0) to test if both extensions get imported correctly
        Then check that there are 2 extensions present in list
