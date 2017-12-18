package io.syndesis.qe.pages.customizations.api_client_connectors.wizard;

import static java.util.Arrays.asList;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import io.syndesis.qe.pages.SyndesisPageObject;
import io.syndesis.qe.pages.java_interfaces.wizard.WizardCancelable;
import io.syndesis.qe.pages.java_interfaces.wizard.WizardSucceedable;

public class ApiClientConnectorWizard extends SyndesisPageObject implements WizardCancelable {

	private static final class Element {
		public static final By ROOT = By.cssSelector("syndesis-api-connector-create");
	}

	private List<WizardSucceedable> wizardSteps = new ArrayList(asList(
			new UploadSwagger(), new ReviewSwaggerActions(), new Security(), new GeneralConnectorInfo()));

	private ListIterator<WizardSucceedable> steps = wizardSteps.listIterator();

	private SyndesisPageObject currentStep = (SyndesisPageObject) wizardSteps.get(0);

	public ApiClientConnectorWizard() {
		steps.next();
	}

	public void nextStep() {
		((WizardSucceedable) currentStep).nextWizardStep();
		if (steps.hasNext()) {
			currentStep = (SyndesisPageObject) steps.next();
		}
	}

	public SyndesisPageObject getCurrentStep() {
		System.out.println(currentStep.getClass());
		return currentStep;
	}

	@Override
	public SelenideElement getRootElement() {
		return null;
	}

	@Override
	public boolean validate() {
		return false;
	}

	@Override
	public void cancelWizard() {

	}
}
