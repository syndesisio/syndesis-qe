package io.syndesis.qe.endpoints;

import static io.syndesis.qe.endpoint.client.EndpointClient.getClient;

import io.syndesis.qe.resource.ResourceFactory;
import io.syndesis.qe.resource.impl.Ops;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.uri.UriComponent;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrometheusQueryEndpoint {

    private static String prometheusEndpoint() {
        return ResourceFactory.get(Ops.class).getPrometheusRoute();
    }

    public JsonNode executeQuery(String query) {
        Invocation.Builder invocation = getClient().target(prometheusEndpoint()).path("api").path("v1").path("query")
            .queryParam("query", UriComponent.encode(query, UriComponent.Type.QUERY_PARAM))
            .request(MediaType.APPLICATION_JSON);
        JsonNode ret = invocation.get(JsonNode.class);
        return ret;
    }

    public JsonNode executeQueryOnRange(String query, int offsetSecs, Integer stepSize) {
        DateTime end = DateTime.now();
        DateTime start = end.minusSeconds(offsetSecs);
        Invocation.Builder invocation = getClient().target(prometheusEndpoint()).path("api").path("v1").path("query_range")
            .queryParam("query", UriComponent.encode(query, UriComponent.Type.QUERY_PARAM))
            .queryParam("start", UriComponent.encode(start.toString(ISODateTimeFormat.dateTime()), UriComponent.Type.QUERY_PARAM))
            .queryParam("end", UriComponent.encode(end.toString(ISODateTimeFormat.dateTime()), UriComponent.Type.QUERY_PARAM))
            .queryParam("step", UriComponent.encode(stepSize.toString(), UriComponent.Type.QUERY_PARAM))
            .request(MediaType.APPLICATION_JSON);
        JsonNode ret = invocation.get(JsonNode.class);
        return ret;
    }

    public JsonNode getAlertRules() {
        Invocation.Builder invocation = getClient().target(prometheusEndpoint()).path("api").path("v1").path("rules")
            .queryParam("type", UriComponent.encode("alert", UriComponent.Type.QUERY_PARAM))
            .request(MediaType.APPLICATION_JSON);
        JsonNode ret = invocation.get(JsonNode.class);
        return ret;
    }

    public JsonNode getAlerts() {
        Invocation.Builder invocation = getClient().target(prometheusEndpoint()).path("api").path("v1").path("alerts")
            .request(MediaType.APPLICATION_JSON);
        JsonNode ret = invocation.get(JsonNode.class);
        return ret;
    }
}
