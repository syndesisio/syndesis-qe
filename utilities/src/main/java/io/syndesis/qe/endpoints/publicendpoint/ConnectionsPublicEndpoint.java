package io.syndesis.qe.endpoints.publicendpoint;

import static org.junit.Assert.fail;

import io.syndesis.common.model.connection.ConnectionOverview;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Properties;

@Component
@Lazy
public class ConnectionsPublicEndpoint extends PublicEndpoint {

    public ConnectionsPublicEndpoint() {
        super("/connections");
    }

    /**
     * Update properties for particular connection
     * endpoint -> POST ​/public​/connections​/{id}​/properties
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#configureConnection(SecurityContext, String, Map)}
     */
    public ConnectionOverview updateConnectionProperties(String connection, Properties properties, boolean refreshIntegration) {
        Invocation.Builder invocation =
            this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s/properties?refreshIntegrations=%s", connection, refreshIntegration)));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode propertiesJson = mapper.convertValue(properties, JsonNode.class);

        Response response = invocation.post(Entity.entity(propertiesJson, MediaType.APPLICATION_JSON));
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus() + ". The response is " + response.getStatusInfo());
        }
        return response.readEntity(ConnectionOverview.class);
    }
}
