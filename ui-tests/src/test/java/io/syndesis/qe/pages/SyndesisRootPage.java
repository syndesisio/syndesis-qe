package io.syndesis.qe.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.utils.Alert;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

public class SyndesisRootPage extends SyndesisPageObject {

    private static final class Link {
        public static final By HOME = By.cssSelector("pf-c-page__header-brand");
    }

    private static final class Element {
        public static final By ROOT = By.tagName("body");
        public static final By ALERT = By.cssSelector(".alert-danger");
    }

    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(Condition.exist);
    }

    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void goHome() {
        this.getRootElement().find(Link.HOME).shouldBe(visible).click();
    }

    public SelenideElement getDangerAlertElemet() {
        return getRootElement().find(Alert.DANGER.getBy());
    }
}
