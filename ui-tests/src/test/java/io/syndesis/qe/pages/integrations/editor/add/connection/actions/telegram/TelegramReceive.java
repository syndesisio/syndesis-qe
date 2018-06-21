package io.syndesis.qe.pages.integrations.editor.add.connection.actions.telegram;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.FieldFiller;

public class TelegramReceive extends FieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    public Class getInputClass() {
        return TelegramReceive.Input.class;
    }

    @Override
    public Class getSelectClass() {
        return null;
    }

    private static final class Element {
        public static final By TITLE = By.cssSelector("h3[innertext='Receive Messages']");
    }

    private static final class Input {
        public static final By CHAT_ID = By.name("chatId");
    }
}
