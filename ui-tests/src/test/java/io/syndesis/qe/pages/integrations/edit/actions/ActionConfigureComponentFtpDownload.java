package io.syndesis.qe.pages.integrations.edit.actions;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionConfigureComponentFtpDownload extends ActionConfigureComponentFieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    Class getInputClass() {
        return ActionConfigureComponentFtpDownload.Input.class;
    }

    @Override
    Class getSelectClass() {
        return ActionConfigureComponentFtpDownload.Select.class;
    }

    private static final class Element {
        public static final By TITLE = By.cssSelector("h3[innertext='download']");
    }

    private static final class Input {
        public static final By INPUT_FILENAME = By.cssSelector("input[name='fileName']");
        public static final By INPUT_DIRECTORYNAME = By.cssSelector("input[name='directoryName']");
        public static final By INPUT_INITIALDELAY = By.cssSelector("input[name='initialDelay']");
        public static final By INPUT_DELAY = By.cssSelector("input[name='delay']");
    }

    private static final class Select {
        //('Yes', 'No')
        public static final By SELECT_DELETE = By.cssSelector("input[name='delete']");
    }
}
