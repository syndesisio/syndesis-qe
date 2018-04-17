package io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.FieldFiller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpDataType extends FieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    public Class getInputClass() {
        return FtpDataType.Input.class;
    }

    @Override
    public Class getSelectClass() {
        return FtpDataType.Select.class;
    }

    private static final class Element {
        public static final By TITLE = By.cssSelector("h3[innertext='Specify Output Data Type']");
    }

    private static final class Input {
        public static final By INPUT_NAME = By.name("name");
        public static final By INPUT_DESCRIPTION = By.name("description");
        public static final By TEXTAREA_SPECIFICATION = By.name("specification");
    }

    private static final class Select {
        public static final By SELECT_KIND = By.name("kind");
    }
}
