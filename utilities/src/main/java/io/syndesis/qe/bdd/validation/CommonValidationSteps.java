package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.qe.endpoints.IntegrationOverviewEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.model.IntegrationOverview;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Dec 12, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class CommonValidationSteps {

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;
    @Autowired
    private IntegrationOverviewEndpoint integrationOverviewEndpoint;

    public CommonValidationSteps() {
    }

    @Then("^wait for integration with name: \"([^\"]*)\" to become active")
    public void waitForIntegrationToBeActive(String integrationName) {
        final long start = System.currentTimeMillis();
        //wait for activation
        log.info("Waiting until integration \"{}\" becomes active. This may take a while...", integrationName);

        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);

        final boolean activated = TestUtils.waitForPublishing(integrationOverviewEndpoint, integrationOverview, TimeUnit.MINUTES, 10);
        Assertions.assertThat(activated).isEqualTo(true);
        log.info("Integration pod has been started. It took {}s to build the integration.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
    }

    @Then(value = "^verify there is s2i build running for integration: \"([^\"]*)\"$")
    public void verifyIntegrationBuildRunning(int numberOfBuilds, String integrationName) {
        final String sanitizedName = integrationName.toLowerCase().replaceAll(" ", "-");
        final List<Build> builds = new ArrayList<>(OpenShiftUtils.getInstance().getBuilds());
        Assertions.assertThat(builds).isNotEmpty();
        Assertions.assertThat(builds).filteredOn(build -> build.getMetadata().getLabels().get("buildconfig").contentEquals(sanitizedName)).isNotEmpty();
        log.info("There is build with name {} running", sanitizedName);
    }

    @Then(value = "^verify there are no s2i builds running for integration: \"([^\"]*)\"$")
    public void verifyNoIntegrationBuildRunning(String integrationName) {
        final String sanitizedName = integrationName.toLowerCase().replaceAll(" ", "-");
        Assertions.assertThat(new ArrayList<>(OpenShiftUtils.getInstance().getBuilds())).filteredOn(build -> build.getMetadata().getLabels().get("buildconfig").contentEquals(sanitizedName)).isEmpty();
        log.info("There is no builds with name {} running", sanitizedName);
    }

    @Then("^verify integration \"([^\"]*)\" has current state \"([^\"]*)\"")
    public void verifyIntegrationState(String integrationName, String integrationState) {

        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        integrationOverviewEndpoint = new IntegrationOverviewEndpoint();
        final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);

        log.debug("Actual state: {} and desired state: {}", integrationOverview.getCurrentState().name(), integrationState);
        Assertions.assertThat(integrationOverview.getCurrentState().name()).isEqualTo(integrationState);
    }

    @Then("^validate integration: \"([^\"]*)\" pod scaled to (\\d+)$")
    public void verifyPodCount(String integrationName, int podCount) {
        log.info("Then validate the pod scaled to: {}", podCount);

        final String sanitizedName = "i-" + integrationName.toLowerCase().replaceAll(" ", "-");
        log.info("Pod name: {}", sanitizedName);

        try {
            final String errorMessage = "Wrong number of pods " + sanitizedName;
            OpenShiftWaitUtils.assertEventually(errorMessage, OpenShiftWaitUtils.areExactlyNPodsRunning("deploymentconfig", sanitizedName, podCount));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Interruption Error: {}", e);
            }
        } catch (InterruptedException ex) {
            log.error("Error: {}", ex);
        }
        final List<Pod> pods = OpenShiftUtils.getInstance().getPods().stream().filter(
                b -> b.getMetadata().getName().contains(sanitizedName)).collect(Collectors.toList());
        Assertions.assertThat(pods.stream().filter(p -> p.getStatus().getPhase().contentEquals("Running")).count() == podCount);
        log.info("There are {} pods with name {} running", podCount, sanitizedName);
    }

    @Then("^switch Inactive and Active state on integration \"([^\"]*)\" for (\\d+) times and check pods up/down")
    public void verifyIntegrationOnOffNTimes(String integrationName, int switchNTimes) {
        final String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();

        for (int i = 0; i <= switchNTimes; i++) {

            final IntegrationDeploymentState newDepState;
            final Integration integration = integrationsEndpoint.get(integrationId);
            int integrationVersion = integration.getVersion();

            log.info("Getting integrationDeployment with deployment number: {}", integrationVersion);

            IntegrationDeployment currentDeployment = integrationsEndpoint.getCurrentIntegrationDeployment(integrationId, integrationVersion);
            if (currentDeployment.getCurrentState().equals(IntegrationDeploymentState.Published)) {
                newDepState = IntegrationDeploymentState.Unpublished;
                log.info("Unpublishing integration with integration version: {}", integrationVersion);
                integrationsEndpoint.deactivateIntegration(integrationId, integrationVersion);
            } else {
                newDepState = IntegrationDeploymentState.Published;
                log.info("Publishing integration: {}", integrationId);
                integrationsEndpoint.activateIntegration(integrationId);
            }

            if (newDepState.equals(IntegrationDeploymentState.Published)) {
                verifyPodCount(integrationName, 1);
            } else {
                verifyPodCount(integrationName, 0);
            }
        }
    }
}
