package io.syndesis.qe.pages.integrations.editor.add.connection.actions.jms;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.FieldFiller;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 12/19/17.
 */
@Slf4j
public class JmsPublish extends FieldFiller {

    private static final class Element {
        public static final By TITLE = By.cssSelector("div[innertext='Publish Messages']");
    }

    private static final class Input {
        public static final By DESTINATION_NAME = By.id("destinationName");
        public static final By PERSISTENT = By.id("persistent");
    }

    private static final class Select {
        public static final By DESTINATION_TYPE = By.id("destinationType");
    }


    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    public Class getInputClass() {
        return JmsPublish.Input.class;
    }

    @Override
    public Class getSelectClass() {
        return JmsPublish.Select.class;
    }
}
