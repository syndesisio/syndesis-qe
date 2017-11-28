package io.syndesis.qe.pages.integrations.edit.steps;

public class StepComponentFactory {
	public StepComponent getStep(String stepType, String parameter) {
		if (stepType == null) {
			return null;
		}
		if (stepType.toUpperCase().equals("LOG")) {
			return new LogStepComponent(parameter);
		} else if (stepType.toUpperCase().equals("BASIC FILTER")) {
			return new BasicFilterStepComponent(parameter);
		} else if (stepType.toUpperCase().equals("ADVANCED FILTER")) {
			return new AdvancedFilterStepComponent(parameter);
		}

		return null;
	}
}
