package io.syndesis.qe.pages.integrations.editor.add.connection.actions.ftp;

import static com.codeborne.selenide.Condition.visible;

import org.openqa.selenium.By;

import io.syndesis.qe.pages.integrations.editor.add.connection.actions.fragments.FieldFiller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpUpload extends FieldFiller {

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }

    @Override
    public Class getInputClass() {
        return FtpUpload.Input.class;
    }

    @Override
    public Class getSelectClass() {
        return FtpUpload.Select.class;
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

