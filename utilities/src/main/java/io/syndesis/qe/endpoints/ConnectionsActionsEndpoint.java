package io.syndesis.qe.endpoints;

import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.qe.utils.TestUtils;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.Map;

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
        JsonNode response;
        try {
            response = invocation.post(Entity.entity(body, MediaType.APPLICATION_JSON), JsonNode.class);
        } catch (BadRequestException ex) {
            log.error("Bad request, trying again in 15 seconds");
            TestUtils.sleepIgnoreInterrupt(15000L);
            response = invocation.post(Entity.entity(body, MediaType.APPLICATION_JSON), JsonNode.class);
        }
        return transformJsonNode(response, ConnectorDescriptor.class);
    }

    public String getStoredProcedureTemplate(String storedProcedureName, boolean start) {
        final String url = String.format("/%s", start ? "sql-stored-start-connector" : "sql-stored-connector");
        log.debug("POST, destination: {}", url);
        final Invocation.Builder invocation = createInvocation(url);
        JsonNode response = invocation.post(
            Entity.entity("{\"procedureName\":\"" + storedProcedureName + "\"}", MediaType.APPLICATION_JSON),
            JsonNode.class
        );

        return transformJsonNode(response, ConnectorDescriptor.class).getPropertyDefinitionSteps().get(0).getProperties().get("template").getDefaultValue();
    }
}
