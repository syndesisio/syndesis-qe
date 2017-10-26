package io.syndesis.qe.rest.endpoints;

import java.security.GeneralSecurityException;

import io.syndesis.model.connection.Connector;

/**
 * Connectors rest client endpoint.
 *
 * @author jknetl
 */
public class ConnectorsEndpoint extends AbstractEndpoint<Connector> {

	public ConnectorsEndpoint(String syndesisUrl) throws GeneralSecurityException {
		super(Connector.class, syndesisUrl, "connectors");
	}

	@Override
	public Connector create(Connector obj) {
		throw new UnsupportedOperationException("Connectors cannot be created using REST API.");
	}

	@Override
	public void delete(String id) {
		throw new UnsupportedOperationException("Connectors cannot be deleted using REST API.");
	}
}
