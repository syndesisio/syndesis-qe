package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class AddToIntegration extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".integration-editor-layout");
        public static final By TITLE = By.cssSelector("h1[innertext='Add to Integration']");
    }
    @Override
    public SelenideElement getRootElement() {
        SelenideElement elementRoot = $(Element.ROOT).shouldBe(visible);
        return elementRoot;
    }

    @Override
    public boolean validate() {
        return this.getRootElement().find(Element.TITLE).is(visible);
    }
}
