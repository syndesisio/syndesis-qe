package io.syndesis.qe.pages.integrations.edit.actions;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionConfigureComponentFtpDataType extends ActionConfigureComponentFieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    Class getInputClass() {
        return ActionConfigureComponentFtpDataType.Input.class;
    }

    @Override
    Class getSelectClass() {
        return ActionConfigureComponentFtpDataType.Select.class;
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
