package io.syndesis.qe.pages.integrations.editor.add.connection.actions.google;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;

/**
 * Page representing Get a specific Event action of Google Calendar connector.
 */
public class GetSpecificEvent extends ConfigureAction {
    public void fillEventInput(String calendarId, String eventId) {
        this.selectFromDropDown("calendarId", calendarId);
        this.fillInput("eventId", eventId);
    }
}
