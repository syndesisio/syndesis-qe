package io.syndesis.qe.pages.connections.detail;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sveres on 11/10/17.
 */
@Slf4j
public class ConnectionDetail extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector("pf-c-page__main");
        public static final By CONNECTION_DETAIL = By.cssSelector(".pf-c-page__main-section");
        public static final By CONNECTION_NAME = By.cssSelector(".inline-text-readwidget.connection-details-header__connectionName");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return $(Element.ROOT).is(visible);
    }

    public SelenideElement connectionDetailElement() {
        return $(Element.CONNECTION_DETAIL);
    }

    public String connectionName() {
        return $(Element.CONNECTION_DETAIL).$(Element.CONNECTION_NAME).shouldBe(visible).getText();
    }
}
