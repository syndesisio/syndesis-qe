package io.syndesis.qe.endpoints.publicendpoint;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.resource.impl.PublicOauthProxy;
import io.syndesis.qe.utils.PublicApiUtils;
import io.syndesis.qe.utils.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract class for public endpoints
 */
@Slf4j
public abstract class PublicEndpoint {

    protected String rootEndPoint = "/public";
    private static final String API_PATH = TestConfiguration.syndesisRestApiPath();
    private static Client client;
    private MultivaluedMap<String, Object> COMMON_HEADERS = new MultivaluedHashMap<>();

    public PublicEndpoint(String endpoint) {
        client = RestUtils.getClient();
        COMMON_HEADERS.add("X-Forwarded-User", "pista");
        COMMON_HEADERS.add("X-Forwarded-Access-Token", "kral");
        COMMON_HEADERS.add("SYNDESIS-XSRF-TOKEN", "awesome");
        COMMON_HEADERS.add("Authorization", "Bearer " + PublicApiUtils.getPublicToken());
        rootEndPoint += endpoint;
    }

    String getWholeUrl(String publicEndpointUrl) {
        return String.format("https://%s%s%s", PublicOauthProxy.PUBLIC_API_PROXY_ROUTE, API_PATH, publicEndpointUrl);
    }

    Invocation.Builder createInvocation(String url) {
        log.info(String.format("Creating invocation for url %s", url));
        return client.target(url)
                .request(MediaType.APPLICATION_JSON)
                .headers(COMMON_HEADERS);
    }
}
