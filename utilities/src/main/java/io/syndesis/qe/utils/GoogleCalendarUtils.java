package io.syndesis.qe.utils;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class that provides Calendar API functionality. All methods needs specification of a testing account.
 */
@Component
public class GoogleCalendarUtils {
    @Autowired
    private GoogleAccounts accounts;

    /**
     * Map for internal calendar tracking.
     */
    private Map<String, List<Calendar>> calendars = new HashMap<>();

    private com.google.api.services.calendar.Calendar getClient(String ga) {
        return accounts.getGoogleAccountForTestAccount(ga).calendar();
    }

    /**
     * Method for internal tracking of created calendars.
     * @param user Account specification
     * @param created the Calendar instance to track
     */
    private void trackCreatedCalendar(String user, Calendar created) {
        if (!calendars.containsKey(user)) {
            calendars.put(user, new ArrayList<>());
        }
        calendars.get(user).add(created);
    }

    /**
     * Remove particular calendar from the internal tracker.
     * @param ga Account specification
     * @param id Id of the calendar to remove from tracker
     */
    private void untrackDeletedCalendar(String ga, String id) {
        Calendar toRemove = calendars.get(ga).stream().filter(t -> t.getId().equals(id)).findFirst().get();
        calendars.get(ga).remove(toRemove);
    }

    /**
     * Get a previously created calendar instance.
     * @param accountName Account specification with that the calendar was created
     * @param calendarName Calendar name (aka summary) to retreive
     * @return Calendar instance created before
     */
    public Calendar getPreviouslyCreatedCalendar(String accountName, String calendarName) {
        if (!this.calendars.containsKey(accountName)) {
            return null;
        }
        return calendars.get(accountName).stream()
                .filter(it -> it.getSummary().equals(calendarName))
                .findFirst().orElse(null);
    }

    /**
     * Method that assures cleanup after running the suite.
     * Removes all calendars created during runtime.
     * @throws IOException
     */
    @PreDestroy
    public void removeCalendars() throws IOException {
        for (Map.Entry<String, List<Calendar>> e : calendars.entrySet()) {
            // need a copy due to concurrent removal in deleteCalendar
            List<Calendar> toRemove = new ArrayList<>(e.getValue());
            for (Calendar c : toRemove) {
                deleteCalendar(e.getKey(), c.getId());
            }
        }
    }

    /**
     * Clear calendar contents
     * @param ga Google Account specification
     * @param id Id of the calendar to clear
     * @throws IOException
     */
    public void clearCalendar(String ga, String id) throws IOException {
        getClient(ga).calendars().clear(id).execute();
    }

    /**
     * Delete calendar.
     * @param ga Google Account specification
     * @param id Id of the calendar to delete
     * @throws IOException
     */
    public void deleteCalendar(String ga, String id) throws IOException {
        getClient(ga).calendars().delete(id).execute();
        untrackDeletedCalendar(ga, id);
    }

    /**
     * Get a calendar by its id.
     * @param ga Google Account specification
     * @param id Id of the calendar to get
     * @return Calendar instance with matching id
     * @throws IOException
     */
    public Calendar getCalendar(String ga, String id) throws IOException {
        return getClient(ga).calendars().get(id).execute();
    }

    /**
     * Method to insert (create) a calendar with given google account.
     * Use the instance returned, not the one provided as argument.
     * @param ga Google Account specification
     * @param newCalendar a calendar instance to be created
     * @return calendar instance with the created instance (id assigned, updated fields filled, etc) or null
     * @throws IOException
     */
    public Calendar insertCalendar(String ga, Calendar newCalendar) throws IOException {
        Calendar createdCalendar = getClient(ga).calendars().insert(newCalendar).execute();
        trackCreatedCalendar(ga, createdCalendar);
        return createdCalendar;
    }

    /**
     * Update the calendar. Calendar needs to have id assigned.
     * Use the instance returned, not the one provided as argument.
     * @param ga Google Account specification
     * @param updated the Calendar instance to update
     * @return updated Calendar instance
     * @throws IOException
     */
    public Calendar updateCalendar(String ga, Calendar updated) throws IOException {
        return getClient(ga).calendars().update(updated.getId(), updated).execute();
    }

    /**
     * Delete Event from calendar.
     * @param ga Google Account specification
     * @param calendarId Id of the calendar
     * @param eventId Id of the event
     * @throws IOException
     */
    public void deleteEvent(String ga, String calendarId, String eventId) throws IOException {
        getClient(ga).events().delete(calendarId, eventId).execute();
    }

    /**
     * Get event by id.
     * @param ga Google Account specification
     * @param calendarId Id of the calendar
     * @param eventId Id of the event
     * @return Event instance with given Id or null.
     * @throws IOException
     */
    public Event getEvent(String ga, String calendarId, String eventId) throws IOException {
        return getClient(ga).events().get(calendarId, eventId).execute();
    }

    /**
     * Import event placed in different calendar.
     * @param ga Google Account specification
     * @param calendarId target calendar Id
     * @param toImport event originating in different calendar
     * @return Event instance - the new clone of the event
     * @throws IOException
     */
    public Event importEvent(String ga, String calendarId, Event toImport) throws IOException {
        return getClient(ga).events().calendarImport(calendarId, toImport).execute();
    }

    /**
     * Insert (create) an event.
     * Use the instance returned, not the one in the argument.
     * @param ga Google Account specification
     * @param calendarId Id of target calendar.
     * @param toInsert Event instance to be created.
     * @return newly created Event instance
     * @throws IOException
     */
    public Event insertEvent(String ga, String calendarId, Event toInsert) throws IOException {
        return getClient(ga).events().insert(calendarId, toInsert).execute();
    }

    /**
     * Update an existing event.
     * Use the instance returned, not the one in the argument.
     * @param ga Google Account specification
     * @param calendarId Id of the calendar
     * @param toUpdate Event instance to be updated
     * @return updated Event instance
     * @throws IOException
     */
    public Event updateEvent(String ga, String calendarId, Event toUpdate) throws IOException {
        return getClient(ga).events().update(calendarId, toUpdate.getId(), toUpdate).execute();
    }

    /**
     * Get calendar by its name (aka summary). The first calendar with matching name is being returned.
     * @param ga Google Account specification
     * @param calendarName name of the calendar
     * @return Calendar instance with matching name (aka summary)
     * @throws IOException
     */
    public Calendar getCalendarByName(String ga, String calendarName) throws IOException {
        // Iterate through entries in calendar list
        String pageToken = null;
        do {
            CalendarList calendarList = getClient(ga).calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                if (calendarListEntry.getSummary().equals(calendarName)) {
                    return getCalendar(ga, calendarListEntry.getId());
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);
        return null;
    }

    /**
     * Get Event by its title (aka summary).
     * @param ga Google Account specification
     * @param calendarId Id of the source calendar
     * @param eventSummary Summary of the event to find
     * @return first Event instance matching the given summary.
     * @throws IOException
     */
    public Event getEventBySummary(String ga, String calendarId, String eventSummary) throws IOException {
        Event e = null;
        String pageToken = null;
        do {
            Events events = getClient(ga).events().list(calendarId).setPageToken(pageToken).execute();
            Optional<Event> op = events.getItems().stream().filter(it -> it.getSummary().equals(eventSummary)).findFirst();
            if (op.isPresent()) {
                e = op.get();
            }
            pageToken = events.getNextPageToken();
        } while (e == null && pageToken != null);
        return e;
    }
}
