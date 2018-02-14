package io.syndesis.qe.pages.customizations.connectors.wizard.steps;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.interfaces.wizard.WizardStep;

public class GeneralConnectorInfo extends SyndesisPageObject implements WizardStep {

    private static class Button {

        public static By CREATE_CONNECTOR = By.xpath("//button[contains(.,'Create Connector')]");
    }

    private static class Element {
        public static By ROOT = By.cssSelector("syndesis-api-connector-info");
    }

    @Override
    public void goToNextWizardStep() {
        finish();
    }

    public void finish() {
        $(Button.CREATE_CONNECTOR).shouldBe(visible).click();
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(exist);
    }

    @Override
    public boolean validate() {
        return getRootElement().exists();
    }
}
