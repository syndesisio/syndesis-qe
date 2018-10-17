package io.syndesis.qe.steps.other;

import cucumber.api.java.en.And;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.google.CreateEvent;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.google.GetSpecificEvent;
import io.syndesis.qe.utils.GoogleCalendarUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class GoogleCalendarSteps {
    @Autowired
    private GoogleCalendarUtils gcu;

    @And("^fill in get specific event form using account \"([^\"]*)\" calendar \"([^\"]*)\" and event \"([^\"]*)\"$")
    public void fillInGetSpecificEventFormUsingCalendarAndEventSummary(String account, String calendarSummary, String eventSummary) throws Throwable {
        GetSpecificEvent getSpecificEvent = new GetSpecificEvent();
        String calendarId = gcu.getPreviouslyCreatedCalendar(account, calendarSummary).getId();
        String eventId = gcu.getEventBySummary(account, calendarId, eventSummary).getId();
        getSpecificEvent.fillEventInput(calendarSummary, eventId);
    }

    @And("^fill in create event form using calendar \"([^\"]*)\" summary \"([^\"]*)\" and description \"([^\"]*)\"$")
    public void fillInCreateEventFormUsingCalendarSummaryAndDescription(String calendar, String summary, String description) throws Throwable {
        CreateEvent ce = new CreateEvent();
        ce.setCalendarName(calendar);
        ce.setTitle(summary);
        ce.setDescription(description);
    }

    @And("^fill in update event form using calendar \"([^\"]*)\" summary \"([^\"]*)\" and description \"([^\"]*)\" for user \"([^\"]*)\"$")
    public void fillInUpdateEventFormUsingCalendarSummaryAndDescription(String calendarName, String summary, String description, String ga) throws Throwable {
        CreateEvent ce = new CreateEvent();
        ce.setCalendarName(calendarName);
        ce.setTitle(summary);
        ce.setDescription(description);
    }
}
