package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Editor extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".integration-editor-layout");
        public static final By ALERT = By.cssSelector(".alert-danger");
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

    public SelenideElement getAlertElemet() {
        return getRootElement().find(Element.ALERT);
    }
}
