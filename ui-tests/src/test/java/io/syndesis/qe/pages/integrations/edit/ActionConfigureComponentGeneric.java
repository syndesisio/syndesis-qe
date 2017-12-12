package io.syndesis.qe.pages.integrations.edit;

import cucumber.api.PendingException;

/**
 * Created by sveres on 12/11/17.
 */
public class ActionConfigureComponentGeneric extends ActionConfigureComponent {
	@Override
	public void fillInput(String value) {
		throw new PendingException();
	}

	public void fillInput(String fieldId, String value) {
		this.fillInput(fieldId, value);
	}
}
