package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import cucumber.api.java.en.Then;
import io.fabric8.kubernetes.api.model.Pod;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationMetricsSummary;
import io.syndesis.qe.endpoints.ActivityIntegrationsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.rest.v1.handler.activity.Activity;
import io.syndesis.qe.endpoints.IntegrationsMetricsEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * For monitoring of metrics and logs related to integrations.
 */

@Slf4j
public class MonitoringValidationSteps {

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;
    @Autowired
    private IntegrationsMetricsEndpoint integrationsMetricsEndpoint;
    @Autowired
    private ActivityIntegrationsEndpoint activityIntegrationsEndpoint;

    public MonitoringValidationSteps() {
    }

    @Then("^validate that number of all messages through integration \"([^\"]*)\" is greater than \"([^\"]*)\", period in ms: \"([^\"]*)\"$")
    public void validateThatNumberOfAllMessagesOfIntegrationIsGreaterThanPeriodInMs(String integrationName, Integer nr, Integer ms) throws InterruptedException {
        Thread.sleep(ms + 1000);
//        0.  get integration id.
        String integrationId = this.getIdByIntegrationName(integrationName);
        Assertions.assertThat(integrationId).isNotNull();

//        1. get metrics:
        IntegrationMetricsSummary summary = integrationsMetricsEndpoint.get(integrationId);
//        2. get metrics info:
        Long nrOfMessages = summary.getMessages();
        log.info("MESSAGES SUMMARY: *{}*", nrOfMessages);
        Assertions.assertThat(nrOfMessages).isEqualTo(3);

        Date lastProceded = summary.getLastProcessed().get();
        log.info("LAST MESSAGE WAS PROCEEDED ON: *{}*", lastProceded);
    }

    @Then("^validate that log of integration \"([^\"]*)\" has been created, period in ms: \"([^\"]*)\"$")
    public void validateThatLogOfIntegrationHasBeenCreatedPeriodInMs(String integrationName, Integer ms) throws InterruptedException {
        Thread.sleep(ms + 1000);
        String integrationId = this.getIdByIntegrationName(integrationName);
        Assertions.assertThat(integrationId).isNotNull();

        //1.
        List<Activity> activityIntegrationLogs = activityIntegrationsEndpoint.list(integrationId);
        activityIntegrationLogs.stream().forEach(activity -> log.info("LOGS ACTIVITY: *{}*", activity.getPod()));
        //I suppose there is the same pod for every activity
        String podName = activityIntegrationLogs.get(0).getPod();

        Optional<Pod> buildPod = OpenShiftUtils.getInstance().getPods().stream().filter(p -> p.getMetadata().getName().equals(podName)).findFirst();
        if (buildPod.isPresent()) {
            String logText = OpenShiftUtils.getInstance().getPodLog(buildPod.get());
            Assertions.assertThat(logText).isNotEmpty();
        } else {
            Assertions.fail("No pod found for pod name: " + podName);
        }
        //TODO other functionality for logs to be tested.
    }

    //    AUXILIARIES
    private String getIdByIntegrationName(String integrationName) {
        List<Integration> integrations = integrationsEndpoint.list();
        Integration integr = integrations.stream().filter(integration -> integrationName.equals(integration.getName())).findAny().orElse(null);
        return integr.getId().get();
    }
}
