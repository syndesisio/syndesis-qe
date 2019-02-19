# @sustainer: mcada@redhat.com

@ui
@twitter
@smoke
Feature: Connection - CRUD
# Enter feature description here

  Background:
    Given clean application state
    Given log into the Syndesis


  @validate-connection-credentials
  Scenario: Credentials
    And validate credentials

  @connection-create-delete-test
  Scenario: Create & delete
    And navigate to the "Connections" page
    And click on the "Create Connection" button
    And select "Twitter" connection type
    Then check visibility of the "Validate" button

    When fill in "Twitter Listener" connection details
    Then click on the "Validate" button
    Then check visibility of "Twitter has been successfully validated." in alert-success notification

    Then scroll "top" "right"
    And click on the "Next" button

    And fill Name Connection form
      | Connection Name | my sample tw conn         |
      | Description     | this connection is awsome |
#    And type "my sample tw conn" into connection name
#    And type "this connection is awesome" into connection description
    And click on the "Create" button
    Then check visibility of page "Connections"

    When opens the "my sample tw conn" connection detail
    Then check visibility of "my sample tw conn" connection details

    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When delete the "my sample tw conn" connection
  # delete was not fast enough some times so sleep is necessary
  # Then sleep for "2000" ms
    Then check that "my sample tw conn" connection is not visible

  @connection-kebab-menu-test
  Scenario: Kebab menu
    When navigate to the "Connections" page
  # is there any connection? If there are no default connections there is nothing
  #     so we have to add at least one connection first
    And click on the "Create Connection" button
    And select "Twitter" connection type
    Then check visibility of the "Validate" button
    When fill in "Twitter Listener" connection details

  # no validation as its not necessary for this scenario

    Then scroll "top" "right"
    And click on the "Next" button
    And type "my sample tw conn" into connection name
    And type "this connection is awesome" into connection description
    And click on the "Create" button
    Then check visibility of page "Connections"

  # now we know there is at least one connection
    Then check visibility of unveiled kebab menu of all connections, each of this menu consist of "View", "Edit" and "Delete" actions

  # garbage collection
    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When delete the "my sample tw conn" connection
  # Then sleep for "2000" ms
    Then check that "my sample tw conn" connection is not visible


  @connection-edit-view-test
  Scenario: Kebab menu edit & view
    When navigate to the "Connections" page
    And click on the "Create Connection" button
    And select "Twitter" connection type
    Then check visibility of the "Validate" button

    When fill in "Twitter Listener" connection details
    Then click on the "Validate" button
    Then check visibility of "Twitter has been successfully validated." in alert-success notification

    Then scroll "top" "right"
    And click on the "Next" button
    And type "my sample tw conn" into connection name
    And type "this connection is awesome" into connection description
    And click on the "Create" button
    Then check visibility of page "Connections"

    When click on the "Edit" kebab menu button of "my sample tw conn"
    Then check visibility of "my sample tw conn" connection details

    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When click on the "View" kebab menu button of "my sample tw conn"
    Then check visibility of "my sample tw conn" connection details

    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When delete the "my sample tw conn" connection
  # delete was not fast enough some times so sleep is necessary
  # Then sleep for "2000" ms
    Then check that "my sample tw conn" connection is not visible
