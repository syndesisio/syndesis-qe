package io.syndesis.qe.pages.integrations.edit;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@Slf4j
public class ListActionsComponent extends SyndesisPageObject {

    private static final class Element {

        public static final By ROOT = By.cssSelector("syndesis-list-actions");
        public static final By NAME = By.className("name");
        public static final By ACTION_CONFIGURE = By.cssSelector("syndesis-integration-action-configure");
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }

    public void selectAction(String name) {
        log.info("Searching for integration action {}", name);
        getElementContainingText(Element.NAME, name).shouldBe(visible).click();
    }
}
