package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.utils.AccountUtils;
import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HTTPValidationSteps {
    // Static to have this lpf shared between tests
    private static LocalPortForward localPortForward;

    @When("^clear endpoint events$")
    public void clear() {
        if (localPortForward != null) {
            TestUtils.terminateLocalPortForward(localPortForward);
        }
        Optional<Pod> pod = OpenShiftUtils.getPodByPartialName("endpoints");
        assertThat(pod.isPresent()).isTrue();
        localPortForward = TestUtils.createLocalPortForward(pod.get(), 8080, 28080);

        // Clear all events
        HttpUtils.doDeleteRequest("http://localhost:28080/clearEvents");
    }

    @Then("^verify that endpoint \"([^\"]*)\" was executed$")
    public void verifyThatEndpointWasExecuted(String method) {
        verify(method, false);
    }

    @Then("^verify that endpoint \"([^\"]*)\" was executed once$")
    public void verifyThatEndpointWasExecutedOnce(String method) {
        verify(method, true);
    }

    private void verify(String method, boolean once) {
        // Let the integration running
        TestUtils.sleepIgnoreInterrupt(30000L);
        // Get new events
        HTTPResponse r = HttpUtils.doGetRequest("http://localhost:28080/events");
        Map<Long, String> events = new Gson().fromJson(r.getBody(), Map.class);

        if (once) {
            assertThat(events).size().isEqualTo(1);
        } else {
            assertThat(events).size().isGreaterThanOrEqualTo(5);
        }
        for (String event : events.values()) {
            assertThat(method.equals(event));
        }
    }

    @Then("^verify that after \"([^\"]*)\" seconds there were \"([^\"]*)\" calls$")
    public void verifyThatAfterSecondsWasCalls(double seconds, int calls) {
        clear();
        TestUtils.sleepIgnoreInterrupt((long) seconds * 1000);
        HTTPResponse r = HttpUtils.doGetRequest("http://localhost:28080/events");
        Map<Long, String> events = new Gson().fromJson(r.getBody(), Map.class);
        assertThat(events).size().isGreaterThanOrEqualTo(calls);
    }

    @When("send get request using {string} and {string} path")
    public void sendGetRequestUsingAndPath(String account, String path) {
        final Account a = AccountUtils.get(account);
        HttpUtils.doGetRequest(a.getProperty("baseUrl") + path);
    }
}
