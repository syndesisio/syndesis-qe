package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.common.model.connection.Connection;

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
        Optional<String> connectionId = list().stream().filter(i -> i.getName().contentEquals(connectionName)).findFirst().get().getId();
        return get(connectionId.get());
    }
}
