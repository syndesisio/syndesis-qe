package io.syndesis.qe.pages;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.ListIterator;

import io.syndesis.qe.pages.interfaces.wizard.WizardStep;
import lombok.Getter;

@Getter
public abstract class WizardPageObject extends SyndesisPageObject {

    private List<WizardStep> wizardSteps = null;
    private ListIterator<WizardStep> stepsIterator = null;
    private SyndesisPageObject currentStep = null;

    private static WizardPageObject INSTANCE;

    public void setSteps(List<WizardStep> stepPages) {
        wizardSteps = stepPages;
        initIteration();
    }

    public void setSteps(WizardStep[] stepPages) {
        wizardSteps = asList(stepPages);
        initIteration();
    }

    private void initIteration() {
        stepsIterator = wizardSteps.listIterator();
        stepsIterator.next();
        currentStep = (SyndesisPageObject) wizardSteps.get(0);
    }

    public void nextStep() {
        ((WizardStep) currentStep).goToNextWizardStep();
        if (stepsIterator.hasNext()) {
            currentStep = (SyndesisPageObject) stepsIterator.next();
        }
    }

    public SyndesisPageObject getCurrentStep() {
        return currentStep;
    }
}
