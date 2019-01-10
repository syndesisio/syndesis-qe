# @sustainer: asmigala@redhat.com

@ui
@google-calendar
@database
@datamapper
@integrations-google-calendar
Feature: Google Calendar Connector

  Background: Clean application state
    Given clean application state
    And reset content of "TODO" table
    And log into the Syndesis
    And renew access token for "QE Google Calendar" google account
    And created connections
      | Google Calendar | QE Google Calendar | My Google Calendar Connector | SyndesisQE Google Calendar test |
    And navigate to the "Home" page
    And create calendars
      | google_account     | calendar_summary | calendar_description                      |
      | QE Google Calendar | syndesis-test1   | short-lived calendar for integration test |

  # Test Create Event action by creating an event existing in one calendar
  # in another one. This is to bypass setting date/time values in the form
  # which was not clear how to achieve in selenium.
  @google-calendar-copy-event
  Scenario: Test Copy Event
    Given navigate to the "Home" page
    And create calendars
      | google_account     | calendar_summary | calendar_description                             |
      | QE Google Calendar | syndesis-test2   | second short-lived calendar for integration test |
    And create following "all" events in calendar "syndesis-test2" with account "QE Google Calendar"
      | summary     | start_date | start_time | end_date   | end_time | description  | attendees              |
      | past_event1 | 2018-10-01 | 10:00:00   | 2018-10-01 | 11:00:00 | An old event | jbossqa.fuse@gmail.com |
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    When select the "Timer" connection
    And select "Simple Timer" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "My Google Calendar Connector" connection
    And select "Create Event" integration action
    And fill in create event form using calendar "syndesis-test1", summary "new_event" and description "about_the_event"
    And click on the "Done" button

    And add integration "connection" on position "0"
    And select the "My Google Calendar Connector" connection
    And select "Get a specific Event" integration action
    And fill in get specific event form using account "QE Google Calendar", calendar "syndesis-test2" and event "past_event1"
    And click on the "Done" button
    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When define constant "new_event" of type "String" in data mapper
    And define constant "about_the_event" of type "String" in data mapper
    And open data bucket "Constants"
    And create data mapper mappings
      | new_event       | title       |
      | about_the_event | description |
      | startDate       | startDate   |
      | endDate         | endDate     |
      | startTime       | startTime   |
      | endTime         | endTime     |
      | attendees       | attendees   |
      | location        | location    |
    And scroll "top" "right"
    And click on the "Done" button
    And add integration "step" on position "2"
    And select "Log" integration step
    And fill in values
      | Message Body | true |
    And click on the "Done" button

    And click on the "Save as Draft" button
    And set integration name "google_calendar_copy_event"
    And click on the "Publish" button
    Then check visibility of "google_calendar_copy_event" integration details
    And navigate to the "Integrations" page
    And Integration "google_calendar_copy_event" is present in integrations list
    And wait until integration "google_calendar_copy_event" gets into "Running" state
    And verify that event "new_event" with description "about_the_event" exists in calendar "syndesis-test1" using account "QE Google Calendar"

  # Tests Get a specific Event action by inserting the title of the task
  # into a Todo table
  @google-calendar-get-event
  Scenario: Test Get a specific Event
    Given navigate to the "Home" page
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary     | start_date | end_date   | description  |
      | past_event1 | 2018-10-01 | 2018-10-01 | An old event |
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    When select the "Timer" connection
    And select "Simple Timer" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into TODO(task, completed) values (:#task, 3)" value
    And click on the "Done" button
    And add integration "connection" on position "0"
    And select the "My Google Calendar Connector" connection
    And select "Get a specific Event" integration action
    And fill in get specific event form using account "QE Google Calendar", calendar "syndesis-test1" and event "past_event1"
    And click on the "Done" button
    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | title | task |
    And scroll "top" "right"
    And click on the "Done" button
    And click on the "Save as Draft" button
    And set integration name "google_calendar_get_a_specific_event"
    And click on the "Publish" button
    Then check visibility of "google_calendar_get_a_specific_event" integration details
    And navigate to the "Integrations" page
    And Integration "google_calendar_get_a_specific_event" is present in integrations list
    And wait until integration "google_calendar_get_a_specific_event" gets into "Running" state
    And validate that number of all todos with task "past_event1" is greater than "1"

  # Tests Update Event action for an action coming through the flow.
  # Partial update not yet possible due to #3814 and #3887. Mitigating by
  # using data mapper with partial mapping of fields, plus mapping constants
  # for the fields that are being actually updated.
  @google-calendar-update-event-coming-in
  Scenario: Test Update event coming in
    Given navigate to the "Home" page
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary     | start_date | start_time | end_date   | end_time | description  | attendees              |
      | past_event1 | 2018-10-01 | 10:00:00   | 2018-10-01 | 11:00:00 | An old event | jbossqa.fuse@gmail.com |
    When click on the "Create Integration" button to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    When select the "Timer" connection
    And select "Simple Timer" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"
    When select the "My Google Calendar Connector" connection
    And select "Update Event" integration action
    And fill in update event form using calendar "syndesis-test1", old summary "past_event1", summary "updated_summary" and description "this_event_has_ended" for user "QE Google Calendar"
    And click on the "Done" button
    And add integration "connection" on position "0"
    And select the "My Google Calendar Connector" connection
    And select "Get a specific Event" integration action
    And fill in get specific event form using account "QE Google Calendar", calendar "syndesis-test1" and event "past_event1"
    And click on the "Done" button
    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | startDate   | startDate   |
      | startTime   | startTime   |
      | endDate     | endDate     |
      | endTime     | endTime     |
      | attendees   | attendees   |
      | location    | location    |
    And scroll "top" "right"
    And click on the "Done" button
    And click on the "Save as Draft" button
    And set integration name "google_calendar_update_event_coming_in"
    And click on the "Publish" button
    Then check visibility of "google_calendar_update_event_coming_in" integration details
    And navigate to the "Integrations" page
    And Integration "google_calendar_update_event_coming_in" is present in integrations list
    And wait until integration "google_calendar_update_event_coming_in" gets into "Running" state
    And verify that event "updated_summary" exists in calendar "syndesis-test1" using account "QE Google Calendar"
    And verify that event "updated_summary" with description "this_event_has_ended" exists in calendar "syndesis-test1" using account "QE Google Calendar"

  # Parameterized tests for Get Events action. See examples section for actual config.
  @google-calendar-get-events
  Scenario Outline: Test get events action - <name> on <events_to_create> events
    Given navigate to the "Home" page
    And create following "<events_to_create>" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary       | start_date | start_time | end_date   | end_time | description     |
      | past_event1   | 2018-10-01 |            | 2018-10-01 |          | An old event    |
      | past_event2   | 2010-10-01 | 10:00:00   | 2010-10-01 | 11:00:00 | An old event2   |
      | future_event1 |            |            |            |          | A future event  |
      | future_event2 |            |            |            |          | A future event2 |

    When click on the "Create Integration" button to create a new integration.

    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "My Google Calendar Connector" connection
    And select "Get Events" integration action
    And fill in values
      | Delay                                                    | 30                        |
      | Calendar name                                            | syndesis-test1            |
      | Max results                                              | <max_results>             |
      | Consume from the current date ahead                      | <from_current_date_ahead> |
      | Consume from the last event update date on the next poll | <from_last_event_update>  |
      | Query for events                                         | <query_for_events>        |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into TODO(task, completed) values (:#task, 3)" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Log" integration step
    And fill in values
      | Message Body | true |
    And click on the "Done" button

    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | title | task |
    And scroll "top" "right"
    And click on the "Done" button

    And click on the "Save as Draft" button
    And set integration name "google_calendar_<name>_on_<events_to_create>_events"
    And click on the "Publish" button

    Then check visibility of "google_calendar_<name>_on_<events_to_create>_events" integration details
    And navigate to the "Integrations" page
    And Integration "google_calendar_<name>_on_<events_to_create>_events" is present in integrations list
    And wait until integration "google_calendar_<name>_on_<events_to_create>_events" gets into "Running" state
    And check that query "SELECT * FROM TODO" has <expected_num_rows_fst> output
    And reset content of "TODO" table
    And sleep for "30500" ms
    And check that query "SELECT * FROM TODO" has <expected_num_rows_snd> output

    Examples:
      | name          | events_to_create | max_results | from_current_date_ahead | from_last_event_update | query_for_events | expected_num_rows_fst | expected_num_rows_snd |
      | poll_all      | past             | 5           | false                   | false                  |                  | some                  | 2 rows                |
      | poll_updated  | past             | 5           | false                   | true                   |                  | some                  | no                    |
      | poll_upcoming | past             | 5           | true                    | false                  |                  | no                    | no                    |
      | poll_all      | future           | 5           | false                   | false                  |                  | some                  | 2 rows                |
      | poll_updated  | future           | 5           | false                   | true                   |                  | some                  | no                    |
      | poll_upcoming | future           | 5           | true                    | false                  |                  | some                  | 2 rows                |
      | poll_all      | all              | 5           | false                   | false                  |                  | some                  | 4 rows                |
      | poll_updated  | all              | 5           | false                   | true                   |                  | some                  | no                    |
      | poll_upcoming | all              | 5           | true                    | false                  |                  | some                  | 2 rows                |
      | poll_query    | all              | 5           | false                   | false                  | An old           | some                  | 2 rows                |

  # Tests Get events action in particular setup - poll only events updated from previous poll.
  @google-calendar-get-events-poll-updated-during-runtime
  Scenario: Test get events action - poll events updated during runtime
    Given navigate to the "Home" page
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary       | start_date | start_time | end_date   | end_time | description     |
      | past_event1   | 2018-10-01 |            | 2018-10-01 |          | An old event    |
      | past_event2   | 2010-10-01 | 10:00:00   | 2010-10-01 | 11:00:00 | An old event2   |
      | future_event1 |            |            |            |          | A future event  |
      | future_event2 |            |            |            |          | A future event2 |

    When click on the "Create Integration" button to create a new integration.

    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "My Google Calendar Connector" connection
    And select "Get Events" integration action
    And fill in values
      | Delay                                                    | 30             |
      | Calendar name                                            | syndesis-test1 |
      | Max results                                              | 10             |
      | Consume from the current date ahead                      | false          |
      | Consume from the last event update date on the next poll | true           |
      | Query for events                                         |                |
    And click on the "Done" button

    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into TODO(task, completed) values (:#task, 3)" value
    And click on the "Done" button

    And add integration "step" on position "0"
    And select "Log" integration step
    And fill in values
      | Message Body | true |
    And click on the "Done" button

    And add integration "step" on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | title | task |
    And scroll "top" "right"
    And click on the "Done" button

    And click on the "Save as Draft" button
    And set integration name "google_calendar_get_events_poll_runtime"
    And click on the "Publish" button

    Then check visibility of "google_calendar_get_events_poll_runtime" integration details
    And navigate to the "Integrations" page
    And Integration "google_calendar_get_events_poll_runtime" is present in integrations list
    And wait until integration "google_calendar_get_events_poll_runtime" gets into "Running" state
    And check that query "SELECT * FROM TODO" has 4 rows output
    And reset content of "TODO" table
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary       | start_date | start_time | end_date   | end_time | description     |
      | past_event3   | 2018-10-01 |            | 2018-10-01 |          | An old event    |
      | future_event3 |            |            |            |          | A future event3 |
    And update event "past_event1" in calendar "syndesis-test1" for user "QE Google Calendar" with values
      | description | new_description |
    And sleep for "35000" ms
    And check that query "SELECT * FROM TODO" has 3 rows output
    And validate that number of all todos with task "past_event3" is greater than "0"
    And validate that number of all todos with task "future_event3" is greater than "0"
    And validate that number of all todos with task "past_event1" is greater than "0"
