package io.syndesis.qe.endpoints.publicendpoint;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;

import static org.junit.Assert.fail;

@Component
@Lazy
public class EnvironmentsPublicEndpoint extends PublicEndpoint {

    public EnvironmentsPublicEndpoint() {
        super("/environments");
    }

    /**
     * Get all tags in the syndesis
     * endpoint -> GET ​/public​/environments
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#getReleaseEnvironments()}
     */
    public List<String> getAllEnvironments() {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(rootEndPoint));
        Response response = invocation.get();
        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            fail("Status of the response is " + response.getStatus() + ". The response is " + response.getStatusInfo());
        }
        return response.readEntity(new GenericType<List<String>>() {
        });
    }

    /**
     * Delete particular tag. The tag is deleted from all integration where was tagged.
     * endpoint -> DELETE /public​/environments​/{env}
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#deleteEnvironment(String)}
     */
    public void deleteEnvironment(String environment) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s", environment)));
        Response response = invocation.delete();
        if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
            fail("Status of the response is " + response.getStatus() + ". The response is " + response);
        }
    }

    /**
     * Rename particular tag.
     * endpoint -> PUT /public​/environments​/{env}
     * original method -> {@link io.syndesis.server.endpoint.v1.handler.external.PublicApiHandler#renameEnvironment(String, String)}
     */
    public void renameEnvironment(String environment, String newEnvironment) {
        Invocation.Builder invocation = this.createInvocation(getWholeUrl(String.format(rootEndPoint + "/%s", environment)));
        Response response = invocation.put(Entity.entity(newEnvironment, MediaType.APPLICATION_JSON));
        if (response.getStatus() != HttpURLConnection.HTTP_NO_CONTENT) {
            fail("Status of the response is " + response.getStatus() + ". The response is " + response);
        }
    }
}
