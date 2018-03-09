package io.syndesis.qe.rest.tests.integrations;

import java.util.Map;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.qe.endpoints.ConnectionsActionsEndpoint;

/**
 * Mar 8, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
public abstract class AbstractStep {

    public ConnectorDescriptor getConnectorDescriptor(Action action, Map properties, String connectionId) {

        ConnectionsActionsEndpoint conActEndpoint = new ConnectionsActionsEndpoint(connectionId);
        return conActEndpoint.postParamsAction(action.getId().get(), properties);
    }
}
