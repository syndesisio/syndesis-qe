package io.syndesis.qe.rest.tests.steps.state;

import static org.assertj.core.api.Fail.fail;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import cucumber.api.java.en.When;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

public class IntegrationStateHandler {
    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    @When("^rebuild integration with name \"([^\"]*)\"$")
    public void rebuildIntegration(String name) {
        Optional<String> integrationId = integrationsEndpoint.getIntegrationId(name);
        if (!integrationId.isPresent()) {
            fail("Unable to find ID for flow " + name);
        }

        integrationsEndpoint.activateIntegration(integrationId.get());
        final int maxRetries = 10;
        int retries = 0;
        boolean buildPodPresent = false;
        while (!buildPodPresent && retries < maxRetries) {
            buildPodPresent = OpenShiftUtils.client().pods().list().getItems().stream().anyMatch(
                    p -> p.getMetadata().getName().contains(name.toLowerCase().replaceAll(" ", "-"))
                            && p.getMetadata().getName().endsWith("-build"));
            TestUtils.sleepIgnoreInterrupt(10000L);
            retries++;
        }
    }
}
