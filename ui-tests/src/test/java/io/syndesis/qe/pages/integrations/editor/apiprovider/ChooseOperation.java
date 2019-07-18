package io.syndesis.qe.pages.integrations.editor.apiprovider;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class ChooseOperation extends SyndesisPageObject {

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
}
