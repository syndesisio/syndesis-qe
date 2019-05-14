package io.syndesis.qe.pages.connections.wizard.phases;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisRootPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NameConnection extends AbstractConnectionWizardStep {

    private static final class Element {
        public static final By ROOT = By.cssSelector(".integration-editor-layout__content");
    }

    private static final class Input {
        public static final By DESCRIPTION = By.cssSelector("textarea[data-testid=\"description\"]");
        public static final By NAME = By.cssSelector("input[data-testid=\"name\"]");
    }

    private static final class Label {
        public static final By PAGE_TITLE = By.cssSelector("h2[innertext='Add Connection Details']");
    }

    @Override
    public void goToNextWizardPhase() {
        clickCreateButton();
    }

    private void clickCreateButton() {
        new SyndesisRootPage().getButton("Create").shouldBe(visible).click();
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).shouldBe(visible);
    }

    @Override
    public boolean validate() {
        return this.getRootElement().find(Label.PAGE_TITLE).is(visible);
    }

    public void setName(String name) {
        getRootElement().find(Input.NAME).shouldBe(visible).setValue(name);
    }

    public void setDescription(String description) {
        getRootElement().find(Input.DESCRIPTION).shouldBe(visible).setValue(description);
    }
}
