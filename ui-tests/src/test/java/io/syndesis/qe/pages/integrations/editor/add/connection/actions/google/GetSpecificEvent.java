package io.syndesis.qe.pages.integrations.editor.add.connection.actions.google;

import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.ConfigureAction;

import org.openqa.selenium.By;

import com.google.common.collect.ImmutableMap;

/**
 * Page representing Get a specific Event action of Google Calendar connector.
 */
public class GetSpecificEvent extends ConfigureAction {
    public void fillEventInput(String calendarId, String eventId) {
        //        this.selectFromDropDown("calendarid", calendarId);
        String selector = "";
        Form form = new Form($(By.cssSelector(".pf-c-page__main-section form")));
        ImmutableMap<String, String> data = ImmutableMap.<String, String>builder()
            .put("calendarid", calendarId)
            .put("eventid", eventId)
            .build();

        form.fillByTestId(data);
    }
}
