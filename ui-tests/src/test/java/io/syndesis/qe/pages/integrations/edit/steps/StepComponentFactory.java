package io.syndesis.qe.pages.integrations.edit.steps;

import io.syndesis.qe.pages.integrations.edit.steps.StepComponent.StepType;

public class StepComponentFactory {

    public StepComponent getStep(String stepType, String parameter) {
        if (stepType == null) {
            return null;
        }
        if (stepType.toUpperCase().equals(StepType.LOG)) {
            return new LogStepComponent(parameter);
        } else if (stepType.toUpperCase().equals(StepType.BASIC_FILTER)) {
            return new BasicFilterStepComponent(parameter);
        } else if (stepType.toUpperCase().equals(StepType.ADVANCED_FILTER)) {
            return new AdvancedFilterStepComponent(parameter);
        }

        return null;
    }
}
