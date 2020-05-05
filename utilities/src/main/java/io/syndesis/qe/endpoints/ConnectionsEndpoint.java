package io.syndesis.qe.endpoints;

import io.syndesis.common.model.connection.Connection;

import org.springframework.stereotype.Component;

import java.util.Optional;

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

    public Connection getConnectionByName(String connectionName) {
        Optional<Connection> connection = list().stream().filter(c -> c.getName().equals(connectionName)).findFirst();
        return connection.orElse(null);
    }
}
