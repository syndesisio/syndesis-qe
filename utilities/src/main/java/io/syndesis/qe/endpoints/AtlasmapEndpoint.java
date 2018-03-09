package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.Optional;

import io.atlasmap.json.v2.JsonInspectionRequest;
import io.atlasmap.json.v2.JsonInspectionResponse;
import io.syndesis.qe.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Feb 27, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
@Component
public class AtlasmapEndpoint extends AbstractEndpoint<JsonInspectionResponse> {

    public AtlasmapEndpoint() {
        super(JsonInspectionRequest.class, "/atlas");
        client = RestUtils.getWrappedClient();
    }

    public JsonInspectionResponse inspectJson(JsonInspectionRequest body) {
        log.debug("POST: {}", getEndpointUrl(Optional.of("json/inspect")));
        final Invocation.Builder invocation = this.createInvocation("json/inspect");
        JsonInspectionResponse response = invocation.post(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE), JsonInspectionResponse.class);

        return response;
    }
}
