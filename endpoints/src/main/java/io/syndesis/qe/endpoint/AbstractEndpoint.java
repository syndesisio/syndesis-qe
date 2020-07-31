package io.syndesis.qe.endpoint;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.qe.endpoint.client.EndpointClient;
import io.syndesis.qe.endpoint.util.RetryingInvocationBuilder;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements a client endpoint for syndesis REST.
 *
 * @author jknetl
 */
@Slf4j
public abstract class AbstractEndpoint<T> {
    @Setter
    protected String endpointName;
    private Class<T> type;
    protected Client client;

    protected MultivaluedMap<String, Object> commonHeaders = new MultivaluedHashMap<>();

    public AbstractEndpoint(Class<?> type, String endpointName) {
        this.type = (Class<T>) type;
        this.endpointName = endpointName;

        client = EndpointClient.getClient();

        commonHeaders.add("X-Forwarded-User", "pista");
        commonHeaders.add("X-Forwarded-Access-Token", "kral");
        commonHeaders.add("SYNDESIS-XSRF-TOKEN", "awesome");
    }

    public T create(T obj) {
        log.debug("POST: {}", getEndpointUrl());
        final Invocation.Builder invocation = this.createInvocation();
        final JsonNode response = invocation.post(Entity.entity(obj, MediaType.APPLICATION_JSON), JsonNode.class);

        return transformJsonNode(response, type);
    }

    public void delete(String id) {
        log.debug("DELETE: {}", getEndpointUrl(Optional.ofNullable(id)));
        this.createInvocation(id).delete();
    }

    public T get(String id) {
        log.debug("GET : {}", getEndpointUrl(Optional.ofNullable(id)));
        final Invocation.Builder invocation = this.createInvocation(id);
        JsonNode response = null;
        try {
            response = invocation.get(JsonNode.class);
        } catch (NotFoundException ex) {
            log.error("Not found: " + id);
            log.error("Found:");
            list().forEach(t -> log.error("  " + t.toString()));
        }
        return transformJsonNode(response, type);
    }

    public void update(String id, T obj) {
        log.debug("PUT : {}", getEndpointUrl(Optional.ofNullable(id)));
        this.createInvocation(id).put(Entity.entity(obj, MediaType.APPLICATION_JSON), JsonNode.class);
    }

    public List<T> list() {
        return list(null);
    }

    public List<T> list(String id) {
        final ObjectMapper mapper = new ObjectMapper().registerModules(new Jdk8Module());
        mapper.configure(Feature.AUTO_CLOSE_SOURCE, true);
        final ObjectWriter ow = mapper.writer();
        final Class<ListResult<T>> listtype = (Class) ListResult.class;

        log.debug("GET : {}", getEndpointUrl(Optional.ofNullable(id)));

        JsonNode response = this.createInvocation(id).get(JsonNode.class);

        ListResult<T> result = null;
        try {
            result = JsonUtils.reader().forType(listtype).readValue(response.toString());
        } catch (IOException ex) {
            log.error("" + ex);
        }

        final List<T> ts = new ArrayList<>();

        for (int i = 0; i < result.getTotalCount(); i++) {
            T con = null;
            try {
                final String json = ow.writeValueAsString(result.getItems().get(i));
                con = JsonUtils.reader().forType(type).readValue(json);
            } catch (IOException ex) {
                log.error(ex.toString());
            }
            ts.add(con);
        }
        return ts;
    }

    public String getEndpointUrl() {
        return getEndpointUrl(Optional.empty());
    }

    public String getEndpointUrl(Optional<String> id) {
        String url = null;
        if (id.isPresent()) {
            url = String.format("%s%s%s/%s", Constants.LOCAL_REST_URL, Constants.API_PATH, endpointName, id.get());
        } else {
            url = String.format("%s%s%s", Constants.LOCAL_REST_URL, Constants.API_PATH, endpointName);
        }
        return url;
    }

    protected Invocation.Builder createInvocation() {
        return createInvocation(null);
    }

    protected Invocation.Builder createInvocation(String id) {
        return new RetryingInvocationBuilder(client
                                                 .target(getEndpointUrl(Optional.ofNullable(id)))
                                                 .request(MediaType.APPLICATION_JSON)
                                                 .headers(commonHeaders));
    }

    protected T transformJsonNode(JsonNode json, Class<T> t) {
        T ts = null;
        try {
            ts = JsonUtils.reader().forType(t).readValue(json.toString());
        } catch (IOException ex) {
            log.error("" + ex);
        }
        return ts;
    }
}
