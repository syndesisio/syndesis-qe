package io.syndesis.qe.rest.tests.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.OpenShiftUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.cucumber.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;

public class OperatorMeteringLabels {
    /**
     * Checks whether pods contains com.redhat metering labels.
     * It is a feature for Fuse Online product, therefore check runs only in case of the productized build.
     */
    @Then("verify new RedHat metering labels")
    public void checkRedhatLabels() {
        List<Pod> pods = OpenShiftUtils.getInstance().pods().withLabel("syndesis.io/component").list().getItems().stream()
            .filter(p -> !"integration".equals(p.getMetadata().getLabels().get("syndesis.io/component")))
            .collect(Collectors.toList());
        assertThat(OpenShiftUtils.getAnyPod("name", "jaeger-operator")).isPresent();
        pods.add(OpenShiftUtils.getAnyPod("name", "jaeger-operator").get());
        for (Pod p : pods) {
            if (p.getStatus().getPhase().contains("Running")) {
                Map<String, String> labels = p.getMetadata().getLabels();
                assertThat(labels).containsKey("com.redhat.product-name");
                assertThat(labels).containsKey("com.redhat.product-version");
                assertThat(labels).containsKey("com.redhat.component-name");
                assertThat(labels).containsKey("com.redhat.component-version");
            }
        }
    }
}
