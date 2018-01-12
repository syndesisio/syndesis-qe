package io.syndesis.qe.endpoints;

import java.security.GeneralSecurityException;

import io.syndesis.model.integration.Integration;

/**
 * Integrations client endpoint.
 *
 * @author jknetl
 */
public class IntegrationsEndpoint extends AbstractEndpoint<Integration> {

	public IntegrationsEndpoint() throws GeneralSecurityException {
		super(Integration.class, "integrations");
	}
}
