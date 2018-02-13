package io.syndesis.qe.endpoints;

import io.syndesis.model.connection.Connection;

/**
 * Connections rest client endpoint.
 *
 * @author jknetl
 */
public class ConnectionsEndpoint extends AbstractEndpoint<Connection> {

    public ConnectionsEndpoint() {
        super(Connection.class, "/connections");
    }
}
