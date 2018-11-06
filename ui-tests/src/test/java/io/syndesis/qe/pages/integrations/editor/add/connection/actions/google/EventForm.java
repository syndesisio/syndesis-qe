package io.syndesis.qe.pages.integrations.editor.add.connection.actions.google;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;
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
        values.put(By.id("summary"), title);
        return this;
    }

    public EventForm setCalendarName(String calendarName) {
        values.put(By.id("calendarId"), calendarName);
        return this;
    }

    public EventForm setEventId(String eventId) {
        values.put(By.id("eventId"), eventId);
        return this;
    }

    public EventForm setDescription(String description) {
        values.put(By.id("description"), description);
        return this;
    }

    public EventForm setLocation(String location) {
        values.put(By.id("location"), location);
        return this;
    }

    public EventForm setAttendees(String... attendees) {
        values.put(By.id("attendees"), String.join(",", attendees));
        return this;
    }

    public void fill() {
        fillForm(values, getRootElement());
    }
}
