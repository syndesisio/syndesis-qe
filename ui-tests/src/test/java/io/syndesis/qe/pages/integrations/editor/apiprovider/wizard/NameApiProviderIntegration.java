package io.syndesis.qe.pages.integrations.editor.apiprovider.wizard;

import com.codeborne.selenide.SelenideElement;
import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.SyndesisPageObject;
import org.openqa.selenium.By;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;

public class NameApiProviderIntegration  extends SyndesisPageObject implements WizardPhase {

    private static final class Element {
        public static final By ROOT = By.cssSelector("api-provider-creation-step-name");
    }

    private static class Button {
        public static By NEXT = By.xpath("//button[contains(.,'Save and continue')]");
    }

    private static class Input {
        public static final By NAME = By.cssSelector("input[name='nameInput']");
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

    @Override
    public void goToNextWizardPhase() {
        $(Button.NEXT).shouldBe(visible).click();
    }

    public void setName(String name) {
        this.getRootElement().find(Input.NAME).shouldBe(visible).sendKeys(name);
    }
}
