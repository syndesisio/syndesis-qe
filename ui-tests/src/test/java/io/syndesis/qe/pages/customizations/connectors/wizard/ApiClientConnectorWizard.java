package io.syndesis.qe.pages.customizations.connectors.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import io.syndesis.qe.pages.WizardPageObject;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.GeneralConnectorInfo;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.ReviewSwaggerActions;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.Security;
import io.syndesis.qe.pages.customizations.connectors.wizard.steps.UploadSwagger;
import io.syndesis.qe.pages.interfaces.wizard.WizardStep;

public class ApiClientConnectorWizard extends WizardPageObject {


	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-api-connectors-create");
	}

	public ApiClientConnectorWizard() {
		setSteps(new WizardStep[] {new UploadSwagger(), new ReviewSwaggerActions(), new Security(), new GeneralConnectorInfo()});
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
