package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.utils.Alert;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Editor extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".integration-editor-layout");
    }

    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public SelenideElement getDangerAlertElemet() {
        return getRootElement().find(Alert.DANGER.getBy());
    }
}
