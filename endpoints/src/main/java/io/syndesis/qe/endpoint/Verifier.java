package io.syndesis.qe.endpoint;

import io.syndesis.qe.TestConfiguration;

import org.json.JSONObject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to verify connection with given parameters.
 */
@Slf4j
public final class Verifier {
    private static final String ENDPOINT_NAME = "/verifier";
    private static final String apiPath = TestConfiguration.syndesisRestApiPath() + "/connectors/";
    private static Client client;

    private Verifier() {
    }

    /**
     * Verifies connection with given properties.
     * @param connection connection id
     * @param properties connection parameters
     * @return verifier endpoint response as string
     */
    public static String verify(String connection, Map<String, String> properties) {
        if (client == null) {
            client = RestUtils.getClient();
        }
        log.debug("Validating connection {}", connection);
        final Invocation.Builder invocation = client
                .target(TestConfiguration.syndesisLocalRestUrl() + apiPath + connection + ENDPOINT_NAME)
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral")
                .header("SYNDESIS-XSRF-TOKEN", "awesome");
        String r;
        try {
            r = invocation.post(Entity.json(new JSONObject(properties).toString())).readEntity(String.class);
        } catch (ProcessingException ex) {
            log.info("Unable to invoke request, try again");
            r = invocation.post(Entity.json(new JSONObject(properties).toString())).readEntity(String.class);
        }
        if (r.isEmpty()) {
            throw new RuntimeException("Unable to verify parameters for " + connection + " (empty response)!");
        }
        return r;
    }
}
