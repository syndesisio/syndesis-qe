package io.syndesis.qe.pages;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.ListIterator;

import io.syndesis.qe.pages.interfaces.wizard.WizardStep;

public abstract class WizardPageObject extends SyndesisPageObject {

	private List<WizardStep> wizardSteps = null;
	private ListIterator<WizardStep> steps = null;
	private SyndesisPageObject currentStep = null;

	public void setSteps(List<WizardStep> stepPages) {
		wizardSteps = stepPages;
		initIteration();
	}

	public void setSteps(WizardStep[] stepPages) {
		wizardSteps = asList(stepPages);
		initIteration();
	}

	private void initIteration() {
		steps = wizardSteps.listIterator();
		steps.next();
		currentStep = (SyndesisPageObject) wizardSteps.get(0);
	}

	public void nextStep() {
		((WizardStep) currentStep).goToNextWizardStep();
		if (steps.hasNext()) {
			currentStep = (SyndesisPageObject) steps.next();
		}
	}

	public SyndesisPageObject getCurrentStep() {
		return currentStep;
	}
}
