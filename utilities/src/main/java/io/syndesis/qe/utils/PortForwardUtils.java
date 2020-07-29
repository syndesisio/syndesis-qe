package io.syndesis.qe.utils;

import io.syndesis.qe.component.Component;
import io.syndesis.qe.endpoint.Constants;
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
public final class PortForwardUtils {
    private static LocalPortForward localPortForward = null;

    private static final String CHECK_URL = Constants.LOCAL_REST_URL + Constants.API_PATH + "/version";

    private PortForwardUtils() {
    }

    public static void createOrCheckPortForward() {
        if (localPortForward == null) {
            setupLocalPortForward();
            waitForPortForward();
        } else {
            // Check if the port forward is working
            HTTPResponse httpResponse = null;
            try {
                httpResponse = HTTPUtils.doGetRequest(CHECK_URL, null, false);
            } catch (Exception ignore) {
                // ignore
            }
            if (httpResponse == null || httpResponse.getCode() != 200) {
                log.error("Port-forward was created, but seems it isn't working, recreating it");
                setupLocalPortForward();
                waitForPortForward();
            }
        }
    }

    private static void waitForPortForward() {
        if (localPortForward == null) {
            return;
        }
        TestUtils.waitFor(() -> {
            HTTPResponse response = null;
            try {
                response = HTTPUtils.doGetRequest(CHECK_URL, null, false);
            } catch (Exception ex) {
                log.debug("Exception while waiting for port forward: " + ex);
            }
            log.debug("Wait for port-forward response code: " + (response == null ? -1 : response.getCode()));
            return response != null && response.getCode() == 200;
        }, 5, 90, "Port-forward not working after 90 seconds");
    }

    private static void setupLocalPortForward() {
        if (localPortForward != null) {
            try {
                localPortForward.close();
                localPortForward = null;
            } catch (IOException e) {
                log.error("Unable to terminate local port forward: ", e);
            }
        }
        if (!OpenShiftUtils.podExists(p -> p.getMetadata().getName().contains(Component.SERVER.getName()))) {
            return;
        }
        log.debug("creating local port forward for pod syndesis-server");
        localPortForward = OpenShiftUtils.createLocalPortForward(Component.SERVER.getName(), 8080, 8080);
        TestUtils.sleepIgnoreInterrupt(15000L);
    }

    /**
     * Resets the URL and port-forward.
     */
    public static void reset() {
        OpenShiftUtils.terminateLocalPortForward(localPortForward);
        localPortForward = null;
    }
}
