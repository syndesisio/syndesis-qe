# @sustainer: mmuzikar@redhat.com

@ui
@amq
@connections-crud-test
Feature: Connection - CRUD
# Enter feature description here

  Background:
    Given clean application state
    Given log into the Syndesis
    Given deploy ActiveMQ broker


  @validate-connection-credentials
  Scenario: Validate all credentials
    And validate credentials

  @smoke
  @connection-create-delete-test
  Scenario: Create and delete
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Red Hat AMQ" connection type
    Then check visibility of the "Validate" button

    When fill in "AMQ" connection details
    Then click on the "Validate" button
    Then check visibility of "Red Hat AMQ has been successfully validated" in alert-success notification

    Then scroll "top" "right"
    And click on the "Next" button
    And type "amq sample" into connection name
    And type "this connection is awsome" into connection description

    And click on the "Save" button
    Then check visibility of page "Connections"

    When opens the "amq sample" connection detail
    Then check visibility of "amq sample" connection details

    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When delete the "amq sample" connection
  # delete was not fast enough some times so sleep is necessary
  # Then sleep for "2000" ms
    Then check that "amq sample" connection is not visible

  @smoke
  @connection-kebab-menu-test
  Scenario: Kebab menu
    When navigate to the "Connections" page
  # is there any connection? If there are no default connections there is nothing
  #     so we have to add at least one connection first
    And click on the "Create Connection" link
    And select "Red Hat AMQ" connection type
    Then check visibility of the "Validate" button
    When fill in "AMQ" connection details

  # no validation as its not necessary for this scenario

    Then scroll "top" "right"
    And click on the "Next" button
    And type "amq-sample" into connection name
    And type "this connection is awesome" into connection description
    And click on the "Save" button
    Then check visibility of page "Connections"

  # now we know there is at least one connection
    Then check visibility of unveiled kebab menu of all connections, each of this menu consist of "View", "Edit" and "Delete" actions

  # garbage collection
    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When delete the "amq-sample" connection
  # Then sleep for "2000" ms
    Then check that "amq-sample" connection is not visible

  @smoke
  @connection-edit-view-test
  Scenario: Kebab menu edit and view
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Red Hat AMQ" connection type
    Then check visibility of the "Validate" button

    When fill in "AMQ" connection details
    Then click on the "Validate" button
    Then check visibility of "Red Hat AMQ has been successfully validated" in alert-success notification

    Then scroll "top" "right"
    And click on the "Next" button

    And type "amq-sample" into connection name
    And type "this connection is awesome" into connection description
    And click on the "Save" button
    Then check visibility of page "Connections"

    When click on the "Edit" kebab menu button of "amq-sample"
    Then check visibility of "amq-sample" connection details

    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When click on the "View" kebab menu button of "amq-sample"
    Then check visibility of "amq-sample" connection details

    When navigate to the "Connections" page
    Then check visibility of page "Connections"

    When delete the "amq-sample" connection
  # delete was not fast enough some times so sleep is necessary
  # Then sleep for "2000" ms
    Then check that "amq-sample" connection is not visible

  @reproducer
  @ENTESB-11253
  Scenario: Connection tooltips
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "Database" connection type
    Then check that page contains 4 tooltips
    And check that 1. tooltip contains text "JDBC URL of the database."
    And check that 2. tooltip contains text "Username for the database connection."
    And check that 3. tooltip contains text "Password for the database connection."
    And check that 4. tooltip contains text "Database schema."

  @reproducer
  @ENTESB-11603
  Scenario: The previous values are fill in
    And navigate to the "Connections" page
    And click on the "Create Connection" link
    And select "SFTP" connection type
    And fill in values by element data-testid
      | host     | my-host |
      | username | Joe     |
      | password | test    |
      | binary   | No      |
    When click on the "Next" button
    And click on element with data-testid "connection-creator-layout-back-button"
    Then validate values by element data-testid
      | host     | my-host |
      | username | Joe     |
      | binary   | false   |
    