package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Invocation;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.qe.model.IntegrationOverview;
import lombok.extern.slf4j.Slf4j;

/**
 * Feb 16, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
@Component
public class IntegrationOverviewEndpoint extends AbstractEndpoint<IntegrationOverview> {

    public IntegrationOverviewEndpoint() {
        super(IntegrationDeployment.class, "/integrations");
    }

    public IntegrationOverview getOverview(String integrationId) {
        log.debug("GET : {}", getEndpointUrl() + integrationId + "/overview");
        final Invocation.Builder invocation = this.createInvocation(integrationId + "/overview");
        final JsonNode response = invocation.get(JsonNode.class);

        return transformJsonNode(response, IntegrationOverview.class);
    }
}
