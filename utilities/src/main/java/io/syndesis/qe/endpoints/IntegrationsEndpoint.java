package io.syndesis.qe.endpoints;

import io.syndesis.model.integration.Integration;

/**
 * Integrations client endpoint.
 *
 * @author jknetl
 */
public class IntegrationsEndpoint extends AbstractEndpoint<Integration> {

	public IntegrationsEndpoint() {
		super(Integration.class, "/integrations");
	}
}
