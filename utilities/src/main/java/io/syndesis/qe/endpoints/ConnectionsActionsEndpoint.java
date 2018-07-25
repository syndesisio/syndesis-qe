package io.syndesis.qe.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.Map;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feb 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class ConnectionsActionsEndpoint extends AbstractEndpoint<ConnectorDescriptor> {

    public ConnectionsActionsEndpoint(String connectionId) {
        super(Action.class, "/connections/" + connectionId + "/actions");
    }

    public ConnectorDescriptor postParamsAction(String actionName, Map<String, String> body) {
        log.debug("POST, destination : {}", getEndpointUrl() + "/" + actionName);
        final Invocation.Builder invocation = createInvocation(actionName);
        final JsonNode response = invocation.post(Entity.entity(body, MediaType.APPLICATION_JSON), JsonNode.class);
        return transformJsonNode(response, ConnectorDescriptor.class);
    }
}
