package io.syndesis.qe.pages.connections.wizard.phases;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.pages.SyndesisRootPage;
import io.syndesis.qe.utils.ByUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.codeborne.selenide.SelenideElement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NameConnection extends AbstractConnectionWizardStep {

    private static final class Element {
        public static final By ROOT = ByUtils.dataTestId("connector-configuration-form");
    }

    private static final class Input {
        public static final By DESCRIPTION = ByUtils.dataTestId("textarea", "description");
        public static final By NAME = ByUtils.dataTestId("input", "name");
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
        return this.getRootElement().exists();
    }

    public void setName(String name) {
        clearInput(getRootElement().find(Input.NAME).shouldBe(visible)).setValue(name);
    }

    public void setDescription(String description) {
        clearInput(getRootElement().find(Input.DESCRIPTION).shouldBe(visible)).setValue(description);
    }

    private SelenideElement clearInput(SelenideElement input) {
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME));
        input.sendKeys(Keys.BACK_SPACE);
        input.shouldBe(visible).clear();
        return input;
    }
}
