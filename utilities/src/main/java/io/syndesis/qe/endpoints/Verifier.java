package io.syndesis.qe.endpoints;

import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.Map;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.RestUtils;
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
                .target(RestUtils.getRestUrl() + apiPath + connection + ENDPOINT_NAME)
                .request(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-User", "pista")
                .header("X-Forwarded-Access-Token", "kral")
                .header("SYNDESIS-XSRF-TOKEN", "awesome");
        String r = invocation.post(Entity.json(new JSONObject(properties).toString())).readEntity(String.class);
        if (r.isEmpty()) {
            throw new RuntimeException("Unable to verify parameters for " + connection + " (empty response)!");
        }
        return r;
    }
}
