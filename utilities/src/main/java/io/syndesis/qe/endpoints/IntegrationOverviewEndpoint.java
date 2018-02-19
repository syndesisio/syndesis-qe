package io.syndesis.qe.endpoints;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.qe.model.IntegrationOverview;
import lombok.extern.slf4j.Slf4j;

/**
 * Feb 16, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class IntegrationOverviewEndpoint extends AbstractEndpoint<IntegrationOverview> {

    public IntegrationOverviewEndpoint(String integrationId) {
        super(IntegrationDeployment.class, "/integrations/" + integrationId);
    }

    public IntegrationOverview getOverview() {
        log.debug("GET : {}", getEndpointUrl() + "/overview");
        final Invocation.Builder invocation = client
                .target(getEndpointUrl() + "/overview")
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral");

        final JsonNode response = invocation.get(JsonNode.class);

        return transformJsonNode(response, IntegrationOverview.class);
    }
}
