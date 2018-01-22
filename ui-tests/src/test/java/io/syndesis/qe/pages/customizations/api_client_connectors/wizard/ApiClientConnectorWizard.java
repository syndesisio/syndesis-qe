package io.syndesis.qe.pages.customizations.api_client_connectors.wizard;

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
		return $(Element.ROOT).should(exist);
	}

	@Override
	public boolean validate() {
		try {
			$(Element.ROOT).should(exist);
			return true;
		} catch (WebDriverException wde) {
			return false;
		}
	}

	@Override
	public void cancelWizard() {

	}
}
