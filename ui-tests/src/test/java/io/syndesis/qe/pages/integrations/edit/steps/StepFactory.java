package io.syndesis.qe.pages.integrations.edit.steps;

public class StepFactory {
	public StepPage getStep(String stepType, String parameter) {
		if (stepType == null) {
			return null;
		}
		if (stepType.toUpperCase().equals("LOG")) {
			return new LogStepPage(parameter);
		} else if (stepType.toUpperCase().equals("BASIC FILTER")) {
			return new BasicFilterStepPage(parameter);
		} else if (stepType.toUpperCase().equals("ADVANCED FILTER")) {
			return new AdvancedFilterStepPage(parameter);
		}

		return null;
	}
}
