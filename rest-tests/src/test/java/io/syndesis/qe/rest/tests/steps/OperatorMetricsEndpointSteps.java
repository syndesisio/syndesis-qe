package io.syndesis.qe.rest.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.HttpUtils;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import java.io.IOException;

import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.LocalPortForward;

public class OperatorMetricsEndpointSteps {

    @Then("verify whether operator metrics endpoint is active")
    public void checkEndpoint() {
        Endpoints operatorEndpoint = OpenShiftUtils.getInstance().getEndpoint("syndesis-operator-metrics");
        assertThat(operatorEndpoint.getSubsets()).isNotEmpty();
    }

    @Then("verify whether operator metrics endpoint includes version information")
    public void checkVersion() throws IOException {
        try (LocalPortForward ignored = TestUtils.createLocalPortForward(
            //skip syndesis-operator-{d}-deploy pods
            OpenShiftUtils.getPod(p -> p.getMetadata().getName().matches("syndesis-operator-\\d-(?!deploy).*")), 8383, 8383)) {
            assertThat(HttpUtils.doGetRequest("http://localhost:8383/metrics").getBody()).contains("syndesis_version_info{operator_version");
        }
    }
}
