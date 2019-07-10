package io.syndesis.qe.pages.integrations.editor.apiprovider.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.WizardPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class ApiProviderWizard extends WizardPageObject {
    private static final class Element {
        public static final By ROOT = By.cssSelector("syndesis-api-connectors-create");
    }

    public ApiProviderWizard() {
        setSteps(new WizardPhase[]{new UploadApiProviderSpecification(), new ReviewApiProviderActions()});
    }

    @Override
    public SelenideElement getRootElement() {
        return $(Element.ROOT).should(exist);
    }

    @Override
    public boolean validate() {
        return getRootElement().is(visible);
    }
}
