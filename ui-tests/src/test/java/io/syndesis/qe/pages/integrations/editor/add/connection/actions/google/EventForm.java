package io.syndesis.qe.pages.integrations.editor.add.connection.actions.google;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

/**
 * Page representing form of create/update action of Google Calendar Connector.
 * Currently support for writing into the date/time fields are missing (TODO: find out how to write to those).
 */
public class EventForm extends ConfigureAction {
    private Map<By, String> values = new HashMap<>();

    public EventForm setTitle(String title) {
        values.put(ByUtils.dataTestId("summary"), title);
        return this;
    }

    public EventForm setCalendarName(String calendarName) {
        values.put(ByUtils.dataTestId("calendarid"), calendarName);
        return this;
    }

    public EventForm setStartDate(String startDate) {
        values.put(ByUtils.dataTestId("startdate"), startDate);
        return this;
    }

    public EventForm setStartTime(String startTime) {
        values.put(ByUtils.dataTestId("starttime"), startTime);
        return this;
    }

    public EventForm setEndDate(String endDate) {
        values.put(ByUtils.dataTestId("enddate"), endDate);
        return this;
    }

    public EventForm setEndTime(String endTime) {
        values.put(ByUtils.dataTestId("endtime"), endTime);
        return this;
    }

    public EventForm setEventId(String eventId) {
        values.put(By.id("eventid"), eventId);
        return this;
    }

    public EventForm setDescription(String description) {
        values.put(ByUtils.dataTestId("description"), description);
        return this;
    }

    public EventForm setLocation(String location) {
        values.put(ByUtils.dataTestId("location"), location);
        return this;
    }

    public EventForm setAttendees(String... attendees) {
        values.put(ByUtils.dataTestId("attendees"), String.join(",", attendees));
        return this;
    }

    public void fill() {
        fillForm(values, getRootElement());
    }
}
