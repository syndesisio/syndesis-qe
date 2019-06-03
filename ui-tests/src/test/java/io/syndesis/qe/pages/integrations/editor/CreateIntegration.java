package io.syndesis.qe.pages.integrations.editor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateIntegration extends SyndesisPageObject {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".card-pf");
    }

    private static final class Input {
        public static final By NAME = By.cssSelector("input[name='name']");
    }

    private static final class TextArea {
        public static final By DESCRIPTION = By.cssSelector("textarea[name='description']");
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

    public void setName(String name) {
        log.debug("Setting integration name to {}", name);
        this.getRootElement().find(Input.NAME).shouldBe(visible).sendKeys(name);
    }

    public void setDescription(String description) {
        log.debug("Setting integration description to {}", description);
        this.getRootElement().find(TextArea.DESCRIPTION).shouldBe(visible).sendKeys(description);
    }
}
