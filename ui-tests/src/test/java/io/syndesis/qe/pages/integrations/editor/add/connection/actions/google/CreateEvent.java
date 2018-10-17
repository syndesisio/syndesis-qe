package io.syndesis.qe.pages.integrations.editor.add.connection.actions.google;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;

/**
 * Page representing Create Event action of Google Calendar Connector.
 * Currently all the date/time fields are missing.
 */
public class CreateEvent extends ConfigureAction {
    public void setTitle(String title) {
        this.fillTextarea("summary", title);
    }
    public void setCalendarName(String calendarName) {
        this.selectFromDropDown("calendarId", calendarName);
    }
    public void setEventId(String eventId) {
        this.fillInput("eventId", eventId);
    }
    public void setDescription(String description) {
        this.fillInput("description", description);
    }
    public void setLocation(String location) {
        this.fillInput("location", location);
    }
    public void setAttendees(String... attendees) {
        this.fillInput("attendees", String.join(",", attendees));
    }
}
