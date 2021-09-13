package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class ModalDialogPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-modal-box");
        public static final By TITLE_OLD = By.className("pf-c-title"); // workaround until PF will be updated in AtlasMap
        public static final By TITLE = By.className("pf-c-modal-box__title");
        public static final By TEXT = By.className("pf-c-modal-box__body");
    }

    @Override
    public SelenideElement getRootElement() {
        $(Element.ROOT).waitUntil(visible, 30000);
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public String getTitleText() {
        SelenideElement title = getRootElement().find(Element.TITLE);
        return title.exists() ? title.shouldBe(visible).getText(): getRootElement().find(Element.TITLE_OLD).shouldBe(visible).getText();
    }

    public String getModalText() {
        return getRootElement().find(Element.TEXT).shouldBe(visible).getText();
    }
}
