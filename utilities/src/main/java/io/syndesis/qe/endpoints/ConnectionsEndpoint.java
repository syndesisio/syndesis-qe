package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.model.connection.Connection;

/**
 * Connections rest client endpoint.
 *
 * @author jknetl
 */
@Component
public class ConnectionsEndpoint extends AbstractEndpoint<Connection> {

    public ConnectionsEndpoint() {
        super(Connection.class, "/connections");
    }
}
