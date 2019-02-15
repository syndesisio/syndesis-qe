package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;

public class ApiProviderOperationEditorPage extends SyndesisPageObject {

    public static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-integration-api-provider-operation-editor-page");
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
