package io.syndesis.qe.pages.integrations.editor.add;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChooseStep extends SyndesisPageObject {
    private static final class Element {

        public static final By ROOT = By.cssSelector("syndesis-integration-step-select");
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

    public void chooseStep(String stepName) {
        log.info("searching for step {}", stepName);
        this.getRootElement().find(By.cssSelector(String.format("div.step[title='%s']", stepName))).shouldBe(visible).click();
    }
}
