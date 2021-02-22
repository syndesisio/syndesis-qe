# @sustainer: asmigala@redhat.com

@ui
@google-calendar
@oauth
@database
@datamapper
@integrations-google-calendar
@long-running
Feature: Google Calendar Connector

  Background: Clean application state
    Given clean application state
    And reset content of "TODO" table
    And log into the Syndesis
    And renew access token for "QE Google Calendar" google account

    And navigate to the "Settings" page
    And fill "Google Calendar" oauth settings "QE Google Calendar"
    And create connections using oauth
      | Google Calendar | My Google Calendar Connector |

    And navigate to the "Home" page
    And create calendars
      | google_account     | calendar_summary | calendar_description                      |
      | QE Google Calendar | syndesis-test1   | short-lived calendar for integration test |

  # Test Create Event action by creating an event existing in one calendar
  # in another one. This is to bypass setting date/time values in the form
  # which was not clear how to achieve in selenium.
  @ENTESB-12856
  @google-calendar-copy-event
  Scenario: Test Copy Event
    Given navigate to the "Home" page
    And create calendars
      | google_account     | calendar_summary | calendar_description                             |
      | QE Google Calendar | syndesis-test2   | second short-lived calendar for integration test |
    And create following "all" events in calendar "syndesis-test2" with account "QE Google Calendar"
      | summary     | start_date | start_time | end_date   | end_time | description  | attendees              |
      | past_event1 | 2018-10-01 | 10:00:00   | 2018-10-01 | 11:00:00 | An old event | jbossqa.fuse@gmail.com |
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    When select the "Timer" connection
    And select "Simple" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"

    When select the "My Google Calendar Connector" connection
    And select "Create Event" integration action
    And fill in create event form using calendar "syndesis-test1", summary "new_event" and description "about_the_event"
    And click on the "Next" button

    And add integration step on position "0"
    And select the "My Google Calendar Connector" connection
    And select "Get a specific Event" integration action
    And fill in get specific event form using account "QE Google Calendar", calendar "syndesis-test2" and event "past_event1"
    And click on the "Next" button
    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When define constant "new_event" of type "String" in data mapper
    And define constant "about_the_event" of type "String" in data mapper
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
    And add integration step on position "2"
    And select "Log" integration step
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "google_calendar_copy_event"
    And publish integration
    Then Integration "google_calendar_copy_event" is present in integrations list
    And wait until integration "google_calendar_copy_event" gets into "Running" state
    And wait until integration google_calendar_copy_event processed at least 1 message

    And verify that event "new_event" with description "about_the_event" exists in calendar "syndesis-test1" using account "QE Google Calendar"

  # Tests Get a specific Event action by inserting the title of the task
  # into a Todo table
  @google-calendar-get-event
  Scenario: Test Get a specific Event
    Given navigate to the "Home" page
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary     | start_date | end_date   | description  |
      | past_event1 | 2018-10-01 | 2018-10-01 | An old event |
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    When select the "Timer" connection
    And select "Simple" integration action
    And click on the "Next" button
    Then check that position of connection to fill is "Finish"
    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into TODO(task, completed) values (:#task, 3)" value
    And click on the "Next" button
    And add integration step on position "0"
    And select the "My Google Calendar Connector" connection
    And select "Get a specific Event" integration action
    And fill in get specific event form using account "QE Google Calendar", calendar "syndesis-test1" and event "past_event1"
    And click on the "Next" button
    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | title | task |
    And scroll "top" "right"
    And click on the "Done" button
    And click on the "Save" link
    And set integration name "google_calendar_get_a_specific_event"
    And publish integration
    Then Integration "google_calendar_get_a_specific_event" is present in integrations list
    And wait until integration "google_calendar_get_a_specific_event" gets into "Running" state
    And wait until integration google_calendar_get_a_specific_event processed at least 1 message

    And validate that number of all todos with task "past_event1" is greater than 0

  # Tests Update Event action for an action coming through the flow.
  # Partial update not yet possible due to #3814 and #3887. Mitigating by
  # using data mapper with partial mapping of fields, plus mapping constants
  # for the fields that are being actually updated.
  @ENTESB-12856
  @google-calendar-update-event-coming-in
  Scenario: Test Update event coming in
    Given navigate to the "Home" page
    And create following "all" events in calendar "syndesis-test1" with account "QE Google Calendar"
      | summary     | start_date | start_time | end_date   | end_time | description  | attendees              |
      | past_event1 | 2018-10-01 | 10:00:00   | 2018-10-01 | 11:00:00 | An old event | jbossqa.fuse@gmail.com |
    When click on the "Create Integration" link to create a new integration.
    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"
    When select the "Timer" connection
    And select "Simple" integration action
    And click on the "Done" button
    Then check that position of connection to fill is "Finish"
    When select the "My Google Calendar Connector" connection
    And select "Update Event" integration action
    And fill in update event form using calendar "syndesis-test1", old summary "past_event1", summary "updated_summary" and description "this_event_has_ended" for user "QE Google Calendar"
    And click on the "Done" button
    And add integration step on position "0"
    And select the "My Google Calendar Connector" connection
    And select "Get a specific Event" integration action
    And fill in get specific event form using account "QE Google Calendar", calendar "syndesis-test1" and event "past_event1"
    And click on the "Next" button
    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui
    When create data mapper mappings
      | startDate | startDate |
      | startTime | startTime |
      | endDate   | endDate   |
      | endTime   | endTime   |
      | attendees | attendees |
      | location  | location  |
    And scroll "top" "right"
    And click on the "Done" button
    And click on the "Save" link
    And set integration name "google_calendar_update_event_coming_in"
    And publish integration
    Then Integration "google_calendar_update_event_coming_in" is present in integrations list
    And wait until integration "google_calendar_update_event_coming_in" gets into "Running" state
    And wait until integration google_calendar_update_event_coming_in processed at least 1 message

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

    When click on the "Create Integration" link to create a new integration.

    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "My Google Calendar Connector" connection
    And select "Get Events" integration action
    And fill in aliased calendar values by data-testid
      | delay              | 30                        |
      | calendarid         | syndesis-test1            |
      | maxresults         | <max_results>             |
      | consumefromnow     | <from_current_date_ahead> |
      | considerlastupdate | <from_last_event_update>  |
      | query              | <query_for_events>        |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into TODO(task, completed) values (:#task, 3)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select "Log" integration step
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Done" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | title | task |
    And scroll "top" "right"
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "google_calendar_<name>_on_<events_to_create>_events"
    And publish integration

    Then Integration "google_calendar_<name>_on_<events_to_create>_events" is present in integrations list
    And wait until integration "google_calendar_<name>_on_<events_to_create>_events" gets into "Running" state
    And wait until integration google_calendar_<name>_on_<events_to_create>_events processed at least <integration_messages> message

    And check that query "SELECT * FROM TODO" has <expected_num_rows_fst> output
    And reset content of "TODO" table
    And wait until google calendar integration google_calendar_<name>_on_<events_to_create>_events with considerlastupdate on <from_last_event_update> processed at least <integration_messages> new messages
    And check that query "SELECT * FROM TODO" has <expected_num_rows_snd> output

    Examples:
      | name          | events_to_create | max_results | from_current_date_ahead | from_last_event_update | query_for_events | expected_num_rows_fst | expected_num_rows_snd | integration_messages |
      | poll_all      | past             | 5           | false                   | false                  |                  | some                  | 2 rows                | 1                    |
      | poll_updated  | past             | 5           | false                   | true                   |                  | some                  | no                    | 1                    |
      | poll_upcoming | past             | 5           | true                    | false                  |                  | no                    | no                    | 0                    |
      | poll_all      | future           | 5           | false                   | false                  |                  | some                  | 2 rows                | 1                    |
      | poll_updated  | future           | 5           | false                   | true                   |                  | some                  | no                    | 1                    |
      | poll_upcoming | future           | 5           | true                    | false                  |                  | some                  | 2 rows                | 1                    |
      | poll_all      | all              | 5           | false                   | false                  |                  | some                  | 4 rows                | 1                    |
      | poll_updated  | all              | 5           | false                   | true                   |                  | some                  | no                    | 1                    |
      | poll_upcoming | all              | 5           | true                    | false                  |                  | some                  | 2 rows                | 1                    |
      | poll_query    | all              | 5           | false                   | false                  | An old           | some                  | 2 rows                | 1                    |

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

    When click on the "Create Integration" link to create a new integration.

    Then check visibility of visual integration editor
    And check that position of connection to fill is "Start"

    When select the "My Google Calendar Connector" connection
    And select "Get Events" integration action
    And fill in aliased calendar values by data-testid
      | delay              | 30             |
      | calendarid         | syndesis-test1 |
      | maxresults         | 10             |
      | consumefromnow     | false          |
      | considerlastupdate | true           |
      | query              |                |
    And click on the "Next" button

    Then check that position of connection to fill is "Finish"

    When select the "PostgresDB" connection
    And select "Invoke SQL" integration action
    And fill in invoke query input with "insert into TODO(task, completed) values (:#task, 3)" value
    And click on the "Next" button

    And add integration step on position "0"
    And select "Log" integration step
    And fill in values by element data-testid
      | bodyloggingenabled | true |
    And click on the "Next" button

    And add integration step on position "1"
    And select "Data Mapper" integration step
    Then check visibility of data mapper ui

    When create data mapper mappings
      | title | task |
    And scroll "top" "right"
    And click on the "Done" button

    And click on the "Save" link
    And set integration name "google_calendar_get_events_poll_runtime"
    And publish integration

    Then Integration "google_calendar_get_events_poll_runtime" is present in integrations list
    And wait until integration "google_calendar_get_events_poll_runtime" gets into "Running" state
    And wait until integration google_calendar_get_events_poll_runtime processed at least 1 message

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
    And validate that number of all todos with task "past_event3" is greater than 0
    And validate that number of all todos with task "future_event3" is greater than 0
    And validate that number of all todos with task "past_event1" is greater than 0

