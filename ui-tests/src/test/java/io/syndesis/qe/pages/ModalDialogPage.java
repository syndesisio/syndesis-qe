package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class ModalDialogPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-modal-box");
        public static final By HEADER = By.className("modal-header");
        public static final By TITLE = By.className("pf-c-title");
        public static final By BODY = By.className("modal-body");
        public static final By FOOTER = By.className("modal-footer");

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
        return $(Element.TITLE).shouldBe(visible).getText();
    }
}
