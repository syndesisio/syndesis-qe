package io.syndesis.qe.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Feb 14, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class IntegrationsDeploymentEndpoint extends AbstractEndpoint<IntegrationDeployment> {

    public IntegrationsDeploymentEndpoint(String integrationId) {
        super(IntegrationDeployment.class, "/integrations/" + integrationId + "/deployments");
    }

    public void activate() {
        log.debug("PUT : {}", getEndpointUrl());
        final Invocation.Builder invocation = client
                .target(getEndpointUrl())
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral");

        JsonNode r = invocation.put(Entity.entity(new TargetStateRequest(), MediaType.APPLICATION_JSON), JsonNode.class);
        log.info(r.asText());
    }

    public void deactivate(int deploymentId) {
        log.debug("POST : {}", getEndpointUrl());
        final Invocation.Builder invocation = client
                .target(getEndpointUrl() + "1/targetState")
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral");

        invocation.put(Entity.entity(new TargetStateRequest(IntegrationDeploymentState.Unpublished), MediaType.APPLICATION_JSON), JsonNode.class);
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
