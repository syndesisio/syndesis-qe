package io.syndesis.qe.validation;

import io.syndesis.qe.utils.GoogleCalendarUtils;
import io.syndesis.qe.utils.IntegrationUtils;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCalendarSteps {

    @Autowired
    private GoogleCalendarUtils gcu;

    @Autowired
    private IntegrationUtils integrationUtils;

    @When("create calendars")
    public void createCalendar(DataTable calendarsData) throws IOException {
        List<Map<String, String>> valueRows = calendarsData.asMaps(String.class, String.class);
        for (Map<String, String> row : valueRows) {
            String testAccount = row.get("google_account");
            String calendarSummary = gcu.getAliasedCalendarName(row.get("calendar_summary"));
            Calendar c = gcu.getPreviouslyCreatedCalendar(testAccount, calendarSummary);
            // remove a previously created calendar with matching summary (aka title)
            if (c != null) {
                gcu.deleteCalendar(testAccount, c.getId());
            }
            c = new Calendar();
            c.setSummary(calendarSummary);
            c.setDescription(row.get("calendar_description"));
            c = gcu.insertCalendar(testAccount, c);
        }
    }

    @When("create following {string} events in calendar {string} with account {string}")
    public void createFollowingEventsInCalendarWithAccount(String eventTime, String calendarName, String account, DataTable events)
        throws IOException {
        List<Map<String, String>> valueRows = events.asMaps(String.class, String.class);
        String prefix = (("all".equalsIgnoreCase(eventTime)) ? "" : eventTime).trim();
        for (Map<String, String> row : valueRows) {
            String eventName = row.get("summary").trim();
            if (!eventName.startsWith(prefix)) {
                continue;
            }
            String eventDescription = row.get("description");
            String attendeesString = row.get("attendees");
            Event e = new Event();
            e.setSummary(eventName);
            e.setStart(getDateOrDateTime("start", row));
            e.setEnd(getDateOrDateTime("end", row));
            e.setDescription(eventDescription);
            if (attendeesString != null) {
                List<EventAttendee> attendees = new ArrayList<>();
                for (String s : attendeesString.split(",")) {
                    EventAttendee eA = new EventAttendee();
                    eA.setEmail(s.trim());
                    attendees.add(eA);
                }
                e.setAttendees(attendees);
            }
            gcu.insertEvent(account, gcu.getPreviouslyCreatedCalendar(account, gcu.getAliasedCalendarName(calendarName)).getId(), e);
        }
    }

    /**
     * Method that returns EventDateTime instance based on the row defined in table for the step.
     *
     * @param prefix either "start" or "end"
     * @param row the row with data (expecting presence of either prefix+"_date" or prefix+"_time"),
     * if none provided, time is defined as now()+24h for "start" and now()+25h for "end" times
     * @return EventDateTime instance with either prefix+"_date" or prefix+"_time" fields set
     */
    private EventDateTime getDateOrDateTime(String prefix, Map<String, String> row) {
        EventDateTime edt = new EventDateTime();
        String dateValueIdentifier = prefix + "_date";
        String timeValueIdentifier = prefix + "_time";
        String dateValue = row.get(dateValueIdentifier);
        String timeValue = row.get(timeValueIdentifier);

        if (dateValue != null && !dateValue.isEmpty()) { // if date value are provided set it
            if (timeValue != null && !timeValue.isEmpty()) {
                edt.setDateTime(DateTime.parseRfc3339(dateValue + "T" + timeValue));
            } else {
                edt.setDate(DateTime.parseRfc3339(dateValue));
            }
        } else { // if date value not provided set time in future
            // offset of 24 or 25 hours to future: start time now()+24, end time now()+25
            long millisToFuture = (("start".equalsIgnoreCase(prefix) ? 0 : 1) + 24) * 60 * 60 * 1000;
            edt.setDateTime(new DateTime(System.currentTimeMillis() + millisToFuture));
        }
        return edt;
    }

    @When("update event {string} in calendar {string} for user {string} with values")
    public void updateEventInCalendarForUserWithValues(String eventSummary, String calendarName, String account, DataTable properties)
        throws Throwable {
        String aliasedCalendarName = gcu.getAliasedCalendarName(calendarName);
        String calendarId = gcu.getPreviouslyCreatedCalendar(account, aliasedCalendarName).getId();
        Event e = gcu.getEventBySummary(account, calendarId, eventSummary);
        if (e == null) {
            throw new IllegalStateException(String.format("Looking for non-existent event %s in calendar %s", eventSummary, aliasedCalendarName));
        }

        for (List<String> list : properties.cells()) {
            String key = list.get(0);
            String value = list.get(1);
            e.set(key, value);
        }
        gcu.updateEvent(account, calendarId, e);
    }

    @When("^wait until google calendar integration (.*) with considerlastupdate on (.*) processed at least (\\w+) new messages?")
    public void waitForNewMessage(String integrationName, boolean considerlastupdate, int numberOfMessages) {
        if (considerlastupdate) {
            // there aren't any new event
            return;
        }
        integrationUtils.waitForNewMessage(integrationName, numberOfMessages, 60);
    }
}
