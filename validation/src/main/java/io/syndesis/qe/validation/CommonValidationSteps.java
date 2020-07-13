package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.qe.endpoint.IntegrationOverviewEndpoint;
import io.syndesis.qe.endpoint.IntegrationsEndpoint;
import io.syndesis.qe.endpoint.model.IntegrationOverview;
import io.syndesis.qe.test.InfraFail;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import io.cucumber.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
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

    @Then("wait for integration with name: {string} to become active")
    public void waitForIntegrationToBeActive(String integrationName) {
        waitForIntegrationToBeActive(9, integrationName);
    }

    @Then("wait max {int} minutes for integration with name: {string} to become active")
    public void waitForIntegrationToBeActive(int waitTime, String integrationName) {
        final long start = System.currentTimeMillis();
        //wait for activation
        log.info("Waiting until integration \"{}\" becomes active. This may take a while...", integrationName);

        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);

        final boolean activated = TestUtils.waitForPublishing(integrationOverviewEndpoint, integrationOverview, TimeUnit.MINUTES, waitTime);
        if (!activated) {
            log.error("Integration was not active after {} minutes", waitTime);
            final String podName = OpenShiftUtils.getPod(p -> p.getMetadata().getName().contains(integrationName.replaceAll(" ", "-").toLowerCase()))
                .getMetadata().getName();
            log.error(OpenShiftUtils.getInstance().pods().withName(podName).getLog());
            InfraFail.fail("Integration was not active after " + waitTime + " minutes");
        }
        log.info("Integration pod has been started. It took {}s to build the integration.",
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
        if (TestUtils.isJenkins()) {
            log.info("Running on Jenkins, adding 2 min sleep");
            TestUtils.sleepIgnoreInterrupt(120000L);
        }
    }

    @Then("verify that the integration with name {string} is not started")
    public void verifyIntegrationNotStarted(String integrationName) {
        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);
        Assertions.assertThat(TestUtils.waitForPublishing(integrationOverviewEndpoint, integrationOverview, TimeUnit.MINUTES, 9))
            .as("No more integrations should be started").isFalse();
    }

    @Then("verify there is s2i build running for integration: {string}")
    public void verifyIntegrationBuildRunning(String integrationName) {
        final String sanitizedName = integrationName.toLowerCase().replaceAll(" ", "-");
        final List<Build> builds = new ArrayList<>(OpenShiftUtils.getInstance().getBuilds());
        Assertions.assertThat(builds).isNotEmpty();
        Assertions.assertThat(builds).filteredOn(build -> build.getMetadata().getLabels().get("buildconfig").contentEquals(sanitizedName))
            .isNotEmpty();
        log.info("There is build with name {} running", sanitizedName);
    }

    @Then("verify there are no s2i builds running for integration: {string}")
    public void verifyNoIntegrationBuildRunning(String integrationName) {
        final String sanitizedName = integrationName.toLowerCase().replaceAll(" ", "-");
        Assertions.assertThat(new ArrayList<>(OpenShiftUtils.getInstance().getBuilds()))
            .filteredOn(build -> build.getMetadata().getLabels().get("buildconfig").contentEquals(sanitizedName)).isEmpty();
        log.info("There is no builds with name {} running", sanitizedName);
    }

    @Then("verify that integration with name {string} exists")
    public void integrationExist(String integrationName) {
        Assertions.assertThat(integrationsEndpoint.getIntegrationId(integrationName)).isPresent();
    }

    @Then("verify that integration with name {string} doesn't exist")
    public void integrationNotExist(String integrationName) {
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> integrationsEndpoint.getIntegrationId(integrationName));
    }

    @Then("verify integration {string} has current state {string}")
    public void verifyIntegrationState(String integrationName, String integrationState) {

        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        integrationOverviewEndpoint = new IntegrationOverviewEndpoint();
        final IntegrationOverview integrationOverview = integrationOverviewEndpoint.getOverview(integrationId);

        log.debug("Actual state: {} and desired state: {}", integrationOverview.getCurrentState().name(), integrationState);
        Assertions.assertThat(integrationOverview.getCurrentState().name()).isEqualTo(integrationState);
    }

    @Then("validate integration: {string} pod scaled to {int}")
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
                log.error("Interruption Error: {0}", e);
            }
        } catch (InterruptedException ex) {
            log.error("Error: {0}", ex);
        }
        final List<Pod> pods = OpenShiftUtils.getInstance().getPods().stream().filter(
            b -> b.getMetadata().getName().contains(sanitizedName)).collect(Collectors.toList());
        assertThat(pods.stream().filter(p -> p.getStatus().getPhase().contentEquals("Running")).count() == podCount);
        log.info("There are {} pods with name {} running", podCount, sanitizedName);
    }

    @Then("switch Inactive and Active state on integration {string} for {int} times and check pods")
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

    @Then("check that integration {word} contains warning {string}")
    public void verifyWarningOnIntegration(String integrationName, String warning) {
        IntegrationOverview overview = integrationOverviewEndpoint.getOverview(integrationsEndpoint.getIntegrationId(integrationName).get());
        Assertions
            .assertThat(integrationOverviewEndpoint.getOverview(integrationsEndpoint.getIntegrationId(integrationName).get()).getBoard().getWarnings()
                .getAsInt()).isGreaterThan(0);
        Assertions.assertThat(
            overview.getBoard().getMessages().stream().filter(leveledMessage -> leveledMessage.getDetail().get().contains(warning)).findFirst())
            .isNotEmpty();
    }

    @Then("check that integration {word} doesn't contains any warning")
    public void verifyNoWarningOnIntegration(String integrationName) {
        Assertions
            .assertThat(integrationOverviewEndpoint.getOverview(integrationsEndpoint.getIntegrationId(integrationName).get()).getBoard().getWarnings()
                .getAsInt()).isEqualTo(0);
    }

    @Then("^verify that (camel-k )?integration \"([^\"]*)\" build is successful$")
    public void verifyBuildIsSuccessful(String camelK, String integrationName) {
        BooleanSupplier bs = (camelK != null && !camelK.isEmpty())
            ? () -> OpenShiftUtils.podExists(p -> p.getMetadata().getName().startsWith("camel-k-ctx") && p.getMetadata().getName().endsWith("build"))
            : () -> OpenShiftUtils.integrationPodExists(integrationName, pod -> pod.getMetadata().getName().endsWith("build"));
        TestUtils.waitFor(
            bs,
            5,
            300,
            "Unable to find build pod for integration " + integrationName
        );

        bs = (camelK != null && !camelK.isEmpty())
            ? () -> "Succeeded".equals(OpenShiftUtils.getPod(p -> p.getMetadata().getName().startsWith("camel-k-ctx")
            && p.getMetadata().getName().endsWith("build")).getStatus().getPhase())
            : () -> "Succeeded".equals(OpenShiftUtils.getIntegrationPod(integrationName, pod -> pod.getMetadata().getName().endsWith("build"))
            .getStatus().getPhase());
        TestUtils.waitFor(
            bs,
            5,
            600,
            "Build pod for integration " + integrationName + " didn't finish successfully in 10 minutes"
        );
    }
}
