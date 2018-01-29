package io.syndesis.qe.pages.customizations.connectors.wizard;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;

import static java.util.Arrays.asList;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.WizardPageObject;
import io.syndesis.qe.pages.interfaces.wizard.WizardStep;

public class ApiClientConnectorWizard extends WizardPageObject {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-api-connector-create");
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
