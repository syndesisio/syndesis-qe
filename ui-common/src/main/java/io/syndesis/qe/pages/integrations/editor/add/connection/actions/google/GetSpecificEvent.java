package io.syndesis.qe.pages.integrations.editor.add.connection.actions.google;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;
import io.syndesis.qe.utils.TestUtils;

/**
 * Page representing Get a specific Event action of Google Calendar connector.
 */
public class GetSpecificEvent extends ConfigureAction {
    public void fillEventInput(String calendarId, String eventId) {
        // let ui loading all calendar names
        TestUtils.sleepIgnoreInterrupt(4000);
        this.selectFromDropDown("calendarid", calendarId);
        this.fillInputById("eventid", eventId);
    }
}
