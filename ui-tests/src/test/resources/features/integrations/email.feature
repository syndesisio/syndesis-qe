#@sustainer: alice.rum@redhat.com

@ui
@database
@datamapper
@email
Feature: Email connector

  Background: Clean application state
    Given clean application state
    And reset content of "todo" table
    And reset content of "contact" table
    And delete emails from "jbossqa.fuse.email@gmail.com" with subject "syndesis-tests"
    And log into the Syndesis
    And navigate to the "Home" page

  @email-send
  Scenario Outline: Send an e-mail

    Given created connections
      | Send Email (smtp) | Email SMTP With <security> | Send Email with <security> QE | Send email test |

    # Create integration
    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration.
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
    When select the "Send Email with <security> QE" connection
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

    # Publish integration and check that there is something on gmail account sent from tested connector
    When click on the "Save" button
    And set integration name "email-send-qe-integration"
    And publish integration
    Then Integration "email-send-qe-integration" is present in integrations list
    And wait until integration "email-send-qe-integration" gets into "Running" state
    And check that email from "jbossqa.fuse.email@gmail.com" with subject "syndesis-tests" and text "Red Hat" exists
    And delete emails from "jbossqa.fuse.email@gmail.com" with subject "syndesis-tests"

    Examples:
      | security |
      | SSL      |
      | STARTTLS |


  @email-receive  
  Scenario Outline: Receive Email with SSL

    Given created connections
      | Receive Email (imap or pop3) | Email <protocol> With SSL | Receive Email with <protocol> QE | Receive email test |

    When navigate to the "Home" page
    And click on the "Create Integration" button to create a new integration
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    # First connection is email receive (imap or pop3), by default it only fetches unread emails
    When select the "Receive Email with <protocol> QE" connection
    Then select "Receive Email" integration action
    And fill in values
      | delay          | 30 |
      | maximum emails | 10 |
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    # Second connection is insert into TO-DO table in database
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into todo(task, completed) values(:#task, 0)" value
    And click on the "Next" button
    Then check visibility of page "Add to Integration"

    # Integration step: data mapper, which maps email content to 'task' field in TO-DO table
    When add integration step on position "0"
    And select "Data Mapper" integration step
    And open data mapper collection mappings
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
    Then check that query "select * from todo where task like '%Red Hat%'" has some output

    Examples:
      | protocol |
      | IMAP     |
      | POP3     |

