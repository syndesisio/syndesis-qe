package io.syndesis.qe.endpoints;

import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.TestUtils;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * TestSupport class contains utility methods for usage of the test-support endpoint.
 *
 * @author jknetl
 */
@Slf4j
public final class TestSupport {

    private static final String ENDPOINT_NAME = "/test-support";
    private static final String API_PATH = TestConfiguration.syndesisRestApiPath();
    private static Client client;
    private static TestSupport instance = null;

    private TestSupport() {
        client = RestUtils.getClient();
    }

    public static TestSupport getInstance() {
        if (instance == null) {
            instance = new TestSupport();
        }
        return instance;
    }

    /**
     * Resets Syndesis database.
     */
    public void resetDB() {
        int tries = 0;
        TestUtils.withRetry(() -> {
            if (resetDbWithResponse() == 204) {
                log.info("Cleaning integration pods");
                // wait till the integration pods are deleted
                TestUtils
                    .waitFor(() -> OpenShiftUtils.getInstance().pods().withLabel("syndesis.io/component", "integration").list().getItems().isEmpty(),
                        1, 5 * 60,
                        "Some integration was not deleted successfully in time. Integration name: " +
                            OpenShiftUtils.getPodByPartialName("i-").get().getMetadata().getName());

                // In OCP 4.x the build and deploy pods stays there, so delete them
                OpenShiftUtils.getInstance().pods().delete(
                    OpenShiftUtils.getInstance().pods().list().getItems().stream()
                        .filter(pod -> pod.getMetadata().getName().startsWith("i-"))
                        .collect(Collectors.toList())
                );
                return true;
            } else {
                return false;
            }
        }, 10, 5000L, "Unable to reset DB after 10 tries");
    }

    /**
     * Resets Syndesis database.
     *
     * @return HTTP response code
     */
    private int resetDbWithResponse() {
        String url = getEndpointUrl();
        final Invocation.Builder invocation = client
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .header("X-Forwarded-User", "pista")
            .header("X-Forwarded-Access-Token", "kral");
        int responseCode = -1;
        try {
            responseCode = invocation.get().getStatus();
        } catch (ProcessingException e) {
            log.error("Error while invoking reset-db: ", e);
            TestUtils.saveDebugInfo();
        }
        log.info("syndesis-db has been reset, via url: *{}*, responseCode:*{}*", url, responseCode);
        log.debug("Reset endpoint reponse: {}", responseCode);
        return responseCode;
    }

    public String getEndpointUrl() {
        String restEndpoint = String.format("%s%s%s%s", RestUtils.getRestUrl(), API_PATH, ENDPOINT_NAME, "/reset-db");
        log.debug("Reset endpoint URL: *{}*", restEndpoint);
        return restEndpoint;
    }
}
