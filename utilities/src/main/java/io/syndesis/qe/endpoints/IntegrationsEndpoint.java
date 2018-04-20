package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.common.util.Json;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Integrations client endpoint.
 *
 * @author jknetl
 */
@Component
@Slf4j
public class IntegrationsEndpoint extends AbstractEndpoint<Integration> {

    public IntegrationsEndpoint() {
        super(Integration.class, "/integrations");
    }

    /**
     * For publishing integration, it's required to perform simple PUT with no params to integrations/{id}/deployments
     */
    public void activateIntegration(String integrationId) {
        log.debug("PUT : {}", getEndpointUrl(Optional.of(integrationId + "/deployments")));
        final Invocation.Builder invocation = this.createInvocation(integrationId + "/deployments");
        JsonNode r = invocation.put(Entity.entity(new TargetStateRequest(), MediaType.APPLICATION_JSON), JsonNode.class);
    }

    /**
     * For unpublishing of integrations post {"targetState":"Unpublished"} to
     * integrations/{id}/deployments/{version}/targetState
     */
    public void deactivateIntegration(String integrationId, int deploymentId) {
        log.debug("POST : {}", getEndpointUrl(Optional.of(integrationId + "/deployments/" + deploymentId + "/targetState")));
        final Invocation.Builder invocation = this.createInvocation(integrationId + "/deployments/" + deploymentId + "/targetState");
        invocation.post(Entity.entity(new TargetStateRequest(IntegrationDeploymentState.Unpublished), MediaType.APPLICATION_JSON), JsonNode.class);
    }

    public IntegrationDeployment getCurrentIntegrationDeployment(String integrationId, int deploymentId) {
        log.debug("GET : {}", getEndpointUrl(Optional.of(integrationId + "/deployments/" + deploymentId)));
        final Invocation.Builder invocation = this.createInvocation(integrationId + "/deployments/" + deploymentId);
        final JsonNode response = invocation.get(JsonNode.class);
        IntegrationDeployment ts = null;
        try {
            ts = Json.reader().forType(IntegrationDeployment.class).readValue(response.toString());
        } catch (IOException ex) {
            log.error("" + ex);
        }
        return ts;
    }

    public Optional<String> getIntegrationId(String integrationName) {
        List<Integration> integrationsList = list();
        return integrationsList.stream().filter(i -> i.getName().contentEquals(integrationName)).findFirst().get().getId();
    }

    @Data
    private class TargetStateRequest {

        private IntegrationDeploymentState targetState;

        TargetStateRequest(IntegrationDeploymentState targetState) {
            this.targetState = targetState;
        }

        TargetStateRequest() {
        }
    }
}
