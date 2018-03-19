package io.syndesis.qe.bdd;

import java.util.Map;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.qe.endpoints.ConnectionsActionsEndpoint;

public abstract class AbstractStep {

    public ConnectorDescriptor getConnectorDescriptor(Action action, Map properties, String connectionId) {

        ConnectionsActionsEndpoint conActEndpoint = new ConnectionsActionsEndpoint(connectionId);
        return conActEndpoint.postParamsAction(action.getId().get(), properties);
    }
}
