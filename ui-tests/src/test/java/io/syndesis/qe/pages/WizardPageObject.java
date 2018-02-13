package io.syndesis.qe.pages;

import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.util.List;

import io.syndesis.qe.logic.common.wizard.WizardPhase;
import lombok.Getter;

public abstract class WizardPageObject extends SyndesisPageObject {

    private List<WizardPhase> steps = null;
    @Getter
    private int currentPosition = 0;

    public void setSteps(List<WizardPhase> stepPages) {
        steps = stepPages;
    }

    public void setSteps(WizardPhase[] stepPages) {
        steps = asList(stepPages);
    }

    public void nextStep() {
        steps.get(currentPosition).goToNextWizardPhase();
        currentPosition++;
    }

    public SyndesisPageObject getCurrentStep() {
        return (SyndesisPageObject) steps.get(currentPosition);
    }

    public void addStep(WizardPhase step, int zeroBasedPosition) {
        steps.add(zeroBasedPosition,step);
    }

    public void replaceStep(WizardPhase step, int zeroBasedPosition) {
        steps.set(zeroBasedPosition,step);
    }
}
