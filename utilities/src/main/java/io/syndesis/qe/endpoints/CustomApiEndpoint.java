package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;

import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.server.endpoint.v1.handler.connection.CustomConnectorHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 21, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Component
@Slf4j
public class CustomApiEndpoint {

    protected String apiPath = TestConfiguration.syndesisRestApiPath();
    protected Client client;

    public CustomApiEndpoint() {
        client = RestUtils.getClient();
    }

    //TODO(tplevko): find some solution for this. For now it doesn't work properly.
    public void createCAEUsingFile(ConnectorSettings conSettings, InputStream inputFile) {

        CustomConnectorHandler.CustomConnectorFormData cs = new CustomConnectorHandler.CustomConnectorFormData();
        cs.setConnectorSettings(conSettings);
        cs.setSpecification(inputFile);

        log.debug("POST: {}", getEndpointUrl("/connectors/custom/info"));
        Invocation.Builder invocation = client
                .target(getEndpointUrl("/connectors/custom/info"))
                .request(MediaType.MULTIPART_FORM_DATA)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral");
        invocation.post(Entity.entity(cs, MediaType.APPLICATION_JSON), JsonNode.class);
    }

    public void createCAEUsingUrl(ConnectorSettings conSettings) {

        log.debug("POST: {}", getEndpointUrl("/connectors/custom"));
        Invocation.Builder invocation = client
                .target(getEndpointUrl("/connectors/custom"))
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral");
        invocation.post(Entity.entity(conSettings, MediaType.APPLICATION_JSON), JsonNode.class);
    }

    public String getEndpointUrl(String endpointName) {
        String url = null;
        url = String.format("%s%s%s", RestUtils.getRestUrl(), apiPath, endpointName);
        return url;
    }
}
