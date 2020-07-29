package io.syndesis.qe.pages.integrations.editor.add;

import static com.codeborne.selenide.Condition.appears;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChooseStep extends SyndesisPageObject {
    private static final class Element {

        public static final By ROOT = By.className("pf-l-gallery");
        public static final By STEP_ICON = By.cssSelector("syndesis-integration-flow-view-step div.icon");
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
        this.getRootElement().find(By.cssSelector(String.format("h2[title='%s']", stepName))).shouldBe(visible).click();
    }

    public void chooseStep(int index) {
        log.info("choosing step on position {}", index);
        this.getRootElement().$$(Element.STEP_ICON)
            .shouldHave(CollectionCondition.sizeGreaterThan(index))
            .get(index).shouldBe(visible, enabled).click();
    }

    public void chooseStepByDescription(String description) {
        log.info("searching for step by description {}", description);
        this.getRootElement().find(By.cssSelector(String.format("p[title='%s']", description))).shouldBe(visible).click();
    }

    /**
     * Waiting until step page is shown after click
     * e.g. Data Mapper is shown after click on Data Mapper step with small delay which causes test fail.
     *
     * @param stepIdentificationElement - element which identify step page
     * @param timeoutMillis - timeout
     */
    public void waitForStepAppears(By stepIdentificationElement, long timeoutMillis) {
        $(stepIdentificationElement).waitUntil(appears, timeoutMillis);
    }
}
