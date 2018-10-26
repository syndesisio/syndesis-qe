package io.syndesis.qe.steps.other;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import cucumber.api.java.en.When;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.google.EventForm;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.google.GetSpecificEvent;
import io.syndesis.qe.utils.GoogleCalendarUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

public class GoogleCalendarSteps {
    @Autowired
    private GoogleCalendarUtils gcu;

    @When("^fill in get specific event form using account \"([^\"]*)\", calendar \"([^\"]*)\" and event \"([^\"]*)\"$")
    public void fillInGetSpecificEventFormUsingCalendarAndEventSummary(String account, String calendarSummary, String eventSummary) throws Throwable {
        GetSpecificEvent getSpecificEvent = new GetSpecificEvent();
        String calendarId = gcu.getPreviouslyCreatedCalendar(account, calendarSummary).getId();
        String eventId = gcu.getEventBySummary(account, calendarId, eventSummary).getId();
        getSpecificEvent.fillEventInput(calendarSummary, eventId);
    }

    @When("^fill in create event form using calendar \"([^\"]*)\", summary \"([^\"]*)\" and description \"([^\"]*)\"$")
    public void fillInCreateEventFormUsingCalendarSummaryAndDescription(String calendar, String summary, String description) throws Throwable {
        new EventForm()
                .setCalendarName(calendar)
                .setTitle(summary)
                .setDescription(description)
                .fill();
    }

    @When("^fill in update event form using calendar \"([^\"]*)\", summary \"([^\"]*)\" and description \"([^\"]*)\" for user \"([^\"]*)\"$")
    public void fillInUpdateEventFormUsingCalendarSummaryAndDescription(String calendarName, String summary, String description, String ga) throws Throwable {
        fillInUpdateEventForm(calendarName, null, summary, description);
    }

    @When("^fill in update event form using calendar \"([^\"]*)\", old summary \"([^\"]*)\", summary \"([^\"]*)\" and description \"([^\"]*)\" for user \"([^\"]*)\"$")
    public void fillInUpdateEventFormUsingCalendarIdSummaryAndDescription(String calendarName, String oldSummary, String summary, String description, String ga) throws Throwable {
        Calendar c = gcu.getPreviouslyCreatedCalendar(ga, calendarName);
        Assert.assertThat(String.format("Calendar %s is not amongst the created calendars.", calendarName), c, Matchers.notNullValue());
        Event e = gcu.getEventBySummary(ga, c.getId(), oldSummary);
        Assert.assertThat(String.format("Event %s is not present in calendar %s.", oldSummary, calendarName), e, Matchers.notNullValue());
        fillInUpdateEventForm(calendarName, e.getId(), summary, description);
    }

    /**
     * Fill in the form.
     *
     * @param calendarName
     * @param eventId
     * @param summary
     * @param description
     */
    private void fillInUpdateEventForm(String calendarName, String eventId, String summary, String description) {
        EventForm ce = new EventForm();
        ce.setCalendarName(calendarName);
        if (eventId != null) {
            ce.setEventId(eventId);
        }
        if (summary != null) {
            ce.setTitle(summary);
        }
        if (description != null) {
            ce.setDescription(description);
        }
        ce.fill();
    }
}
