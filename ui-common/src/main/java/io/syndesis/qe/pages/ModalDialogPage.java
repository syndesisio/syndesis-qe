package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.form.Form;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

public class ModalDialogPage extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.className("pf-c-modal-box");
        public static final By TITLE = By.className("pf-c-modal-box__title");
        public static final By TEXT = By.className("pf-c-modal-box__body");
    }

    @Override
    public SelenideElement getRootElement() {
        $(Element.ROOT).shouldBe(visible, Duration.ofSeconds(30));

        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public String getTitleText() {
        return getRootElement().find(Element.TITLE).shouldBe(visible).getText();
    }

    public String getModalText() {
        return getRootElement().find(Element.TEXT).shouldBe(visible).getText();
    }
}
