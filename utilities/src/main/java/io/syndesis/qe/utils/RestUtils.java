package io.syndesis.qe.utils;

import io.syndesis.qe.component.Component;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;

import java.io.IOException;

import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for Rest client (RestEasy).
 *
 * @author jknetl
 */
@Slf4j
public final class RestUtils {
    private static LocalPortForward localPortForward = null;
    private static String restUrl;

    private static final String CHECK_URL = "http://localhost:8080/api/v1/version";

    private RestUtils() {
    }

    public static String getRestUrl() {
        if (restUrl == null) {
            setupLocalPortForward();
            waitForPortForward();
        } else {
            // Check if the port forward is working
            HTTPResponse httpResponse = null;
            try {
                httpResponse = HTTPUtils.doGetRequest(CHECK_URL);
            } catch (Exception ignore) {
                // ignore
            }
            if (httpResponse == null || httpResponse.getCode() != 200) {
                log.error("Port-forward was created, but seems it isn't working, recreating it");
                setupLocalPortForward();
                waitForPortForward();
            }
        }
        return restUrl;
    }

    private static void waitForPortForward() {
        TestUtils.waitFor(() -> {
            HTTPResponse response = null;
            try {
                response = HTTPUtils.doGetRequest(CHECK_URL);
            } catch (Exception ignore) {
                // ignore
            }
            return response != null && response.getCode() == 200;
        }, 5, 90, "Port-forward not working after 90 seconds");
        TestUtils.sleepIgnoreInterrupt(15000L);
    }

    public static void setupLocalPortForward() {
        if (localPortForward != null) {
            try {
                localPortForward.close();
            } catch (IOException e) {
                log.error("Unable to terminate local port forward: ", e);
            }
        }
        log.debug("creating local port forward for pod syndesis-server");
        localPortForward = OpenShiftUtils.createLocalPortForward(Component.SERVER.getName(), 8080, 8080);
        // If there was no pod, do nothing
        if (localPortForward == null) {
            return;
        }
        try {
            restUrl = String.format("http://%s:%s", localPortForward.getLocalAddress().getLoopbackAddress().getHostName(),
                localPortForward.getLocalPort());
        } catch (IllegalStateException ex) {
            restUrl = String.format("http://%s:%s", "127.0.0.1", 8080);
        }
        log.debug("rest endpoint URL: " + restUrl);
    }

    /**
     * Resets the URL and port-forward.
     */
    public static void reset() {
        restUrl = null;
        OpenShiftUtils.terminateLocalPortForward(localPortForward);
        localPortForward = null;
    }
}
