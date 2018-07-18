package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

@Slf4j
public class HTTPValidationSteps {
    // Static to have this lpf shared between tests
    private static LocalPortForward localPortForward;

    @Then("^verify that endpoint \"([^\"]*)\" was executed$")
    public void verifyThatEndpointWasExecuted(String method) {
        if (localPortForward == null || !localPortForward.isAlive()) {
            log.info("lpf null or is not alive");
            localPortForward = TestUtils.createLocalPortForward(OpenShiftUtils.getPodByPartialName("endpoints"), 8080, 28080);
        }
        // Clear all events
        HttpUtils.doDeleteRequest("http://localhost:28080/clearEvents");
        // Let the integration running
        TestUtils.sleepIgnoreInterrupt(30000L);
        // Get new events
        Response r = HttpUtils.doGetRequest("http://localhost:28080/events");
        Map<Long, String> events = null;
        try {
            events = new Gson().fromJson(r.body().string(), Map.class);
        } catch (IOException ex) {
            log.error("Unable to convert from json to map", ex);
            ex.printStackTrace();
        }

        assertThat(events).size().isGreaterThanOrEqualTo(5);
        for (String event : events.values()) {
            assertThat(method.equals(event));
        }
    }
}
