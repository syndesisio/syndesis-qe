#@sustainer: alice.rum@redhat.com

@ui
@database
@datamapper
@email
Feature: Email connector

  Background: Clean application state
    Given clean application state
    And reset content of "todo" table
    And delete emails from "jbossqa.fuse.email@gmail.com" with subject "syndesis-tests"
    And log into the Syndesis
    And created connections
      | Send Email (smtp)            | Email SMTP With SSL | Send Email with SSL QE     | Send email ssl test    |
      | Receive Email (imap or pop3) | Email IMAP With SSL | Receive Email with IMAP QE | Receive email ssl test |
    And navigate to the "Home" page

  @email-send
  Scenario: Send an e-mail

    # Create integration
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # First connection periodic sql from contacts table
    When select the "PostgresDB" connection
    And select "Periodic SQL Invocation" integration action
    And fill in periodic query input with "select company from contact limit(1)" value
    And fill in period input with "10" value
    And select "Minutes" from sql dropdown
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # Second connection is email send (smtp)
    When select the "Send Email with SSL QE" connection
    And select "Send Email" integration action
    And fill in values
      | Email to      | jbossqa.fuse@gmail.com       |
      | Email from    | jbossqa.fuse.email@gmail.com |
      | Email subject | syndesis-tests               |
    And click on the "Done" button
    Then check visibility of page "Add to Integration"

    # Two integration steps - split and data mapper for email contents
    When add integration step on position "0"
    And select "Split" integration step
    Then check visibility of page "Add to Integration"

    When add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | company | content |
    And scroll "top" "right"
    And click on the "Done" button
    Then check visibility of page "Add to Integration"

    # Publish integration
    When click on the "Save" button
    And set integration name "email-send-qe-integration"
    And publish integration
    Then Integration "email-send-qe-integration" is present in integrations list
    And wait until integration "email-send-qe-integration" gets into "Running" state

    # Wait integration to do a first periodic query and check that email has arrived
    When sleep for "10000" ms
    Then Integration "email-send-qe-integration" is present in integrations list
    Then check that email from "jbossqa.fuse.email@gmail.com" with subject "syndesis-tests" and text "Red Hat" exists
    
  @email-receive  
  Scenario: Receive Email throught IMAP with SSL
    
    When click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # First connection is email receive (imap or pop3), by default it only fetches unread emails
    When select the "Receive Email with IMAP QE" connection
    Then select "Receive Email" integration action
    And fill in values
      | delay          | 30 |
      | maximum emails | 10 |
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # Second connection is insert into TODO table in database
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button
    Then check visibility of page "Add to Integration"

    # Two integration steps: split and data mapper, which maps email content to 'task' field in TODO table
    When add integration step on position "0"
    And select "Split" integration step
    Then check visibility of page "Add to Integration"

    When add integration step on position "1"
    And select "Data Mapper" integration step
    And create data mapper mappings
      | content | task |
    And scroll "top" "right"
    And click on the "Done" button
    Then check visibility of page "Add to Integration"

    # Publish integration
    When click on the "Save" button
    And set integration name "email-receive-qe-integration"
    And publish integration
    Then Integration "email-receive-qe-integration" is present in integrations list
    And wait until integration "email-receive-qe-integration" gets into "Running" state

    # Send email to connector, wait for it to be received and check that it's content got into database
    When send an e-mail to "jbossqa.fuse.email@gmail.com"
    And sleep for "60000" ms
    Then check that query "select * from todo where task like '%Red Hat%'" has some output

