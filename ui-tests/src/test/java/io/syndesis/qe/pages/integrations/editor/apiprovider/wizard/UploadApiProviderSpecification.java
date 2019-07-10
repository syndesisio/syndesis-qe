package io.syndesis.qe.pages.integrations.editor.apiprovider.wizard;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import io.syndesis.qe.fragments.common.form.ApiSpecificationForm;
import io.syndesis.qe.logic.common.wizard.WizardPhase;
import io.syndesis.qe.pages.SyndesisPageObject;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

public class UploadApiProviderSpecification extends SyndesisPageObject implements WizardPhase {

    private ApiSpecificationForm apiSpecificationForm = new ApiSpecificationForm();

    private static final class Element {
        public static final By ROOT = By.cssSelector(".open-api-select-method");
    }

    private static class Button {
        public static By NEXT = By.xpath("//button[contains(.,'Next')]");
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

    public void upload(String source, String url) {
        apiSpecificationForm.upload(source, url);
    }
}
