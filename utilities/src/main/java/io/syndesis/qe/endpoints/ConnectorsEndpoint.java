package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.common.model.connection.Connector;

/**
 * Connectors rest client endpoint.
 *
 * @author jknetl
 */
@Component
public class ConnectorsEndpoint extends AbstractEndpoint<Connector> {

    public ConnectorsEndpoint() {
        super(Connector.class, "/connectors");
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
