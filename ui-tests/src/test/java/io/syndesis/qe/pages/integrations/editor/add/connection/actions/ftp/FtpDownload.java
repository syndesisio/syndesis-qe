package io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.FieldFiller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpDownload extends FieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    public Class getInputClass() {
        return FtpDownload.Input.class;
    }

    @Override
    public Class getSelectClass() {
        return FtpDownload.Select.class;
    }

    private static final class Element {
        public static final By TITLE = By.cssSelector("h3[innertext='download']");
    }

    private static final class Input {
        public static final By INPUT_FILENAME = By.name("fileName");
        public static final By INPUT_DIRECTORYNAME = By.name("directoryName");
        public static final By INPUT_INITIALDELAY = By.name("initialDelay");
        public static final By INPUT_DELAY = By.name("delay");

    }

    private static final class Select {
        //('Yes', 'No')
        public static final By SELECT_DELETE = By.name("delete");

    }
}
