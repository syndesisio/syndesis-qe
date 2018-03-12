package io.syndesis.qe.pages.integrations.edit.actions;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionConfigureComponentFtpUpload extends ActionConfigureComponentFieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    Class getInputClass() {
        return ActionConfigureComponentFtpUpload.Input.class;
    }

    @Override
    Class getSelectClass() {
        return ActionConfigureComponentFtpUpload.Select.class;
    }

    private static final class Element {
        public static final By TITLE = By.cssSelector("h3[innertext='Upload']");
    }

    private static final class Input {
        public static final By INPUT_FILENAME = By.name("fileName");
        public static final By INPUT_DIRECTORYNAME = By.name("directoryName");
        public static final By INPUT_TEMPPREFIX = By.name("tempPrefix");
        public static final By INPUT_TEMPFILENAME = By.name("tempFileName");

    }

    private static final class Select {
        public static final By SELECT_FILEEXIST = By.name("fileExist");
    }

}

