package io.syndesis.qe.steps.state;

import io.syndesis.qe.endpoint.IntegrationsEndpoint;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationStateHandler {
    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @When("rebuild integration with name {string}")
    public void rebuildIntegration(String name) {
        Optional<String> integrationId = integrationsEndpoint.getIntegrationId(name);
        if (!integrationId.isPresent()) {
            InfraFail.fail("Unable to find ID for flow " + name);
        }

        final int currentNo = OpenShiftUtils.extractPodSequenceNr(
            OpenShiftUtils.getIntegrationPod(name, p -> p.getMetadata().getName().endsWith("-build"))
        );
        integrationsEndpoint.activateIntegration(integrationId.get());

        try {
            OpenShiftWaitUtils.waitFor(() -> OpenShiftUtils.integrationPodExists(name, p -> p.getMetadata().getName().endsWith("build"),
                p -> OpenShiftUtils.extractPodSequenceNr(p) > currentNo), 10000L, 60000L);
        } catch (TimeoutException | InterruptedException e) {
            InfraFail.fail("Unable to find new build pod for integration " + name + "after 60 seconds");
        }
    }
}
