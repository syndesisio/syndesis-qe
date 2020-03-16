package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.qe.endpoints.ActivityIntegrationsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsMetricsEndpoint;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.server.endpoint.v1.handler.activity.Activity;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import cucumber.api.java.en.Then;

/**
 * For monitoring of metrics and logs related to integrations.
 */
public class MonitoringValidationSteps {
    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;
    @Autowired
    private IntegrationsMetricsEndpoint integrationsMetricsEndpoint;
    @Autowired
    private ActivityIntegrationsEndpoint activityIntegrationsEndpoint;

    @Then("validate that number of all messages through integration {string} is greater than {int}, period in ms: {int}")
    public void validateThatNumberOfAllMessagesOfIntegrationIsGreaterThanPeriodInMs(String integrationName, int nr, int ms) {
        TestUtils.sleepIgnoreInterrupt(ms);
        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        assertThat(integrationId).isNotNull();

        IntegrationMetricsSummary summary = integrationsMetricsEndpoint.get(integrationId);
        assertThat(summary.getMessages()).isGreaterThan(nr);
    }

    @Then("validate that log of integration {string} has been created and contains {string}")
    public void validateThatLogOfIntegrationHasBeenCreatedPeriodInMs(String integrationName, String contains) {
        String integrationId = integrationsEndpoint.getIntegrationId(integrationName).get();
        assertThat(integrationId).isNotNull();

        List<Activity> activityIntegrationLogs = activityIntegrationsEndpoint.list(integrationId);
        String podName = activityIntegrationLogs.get(0).getPod();
        assertThat(OpenShiftUtils.getPodLogs(podName)).isNotEmpty().contains(contains);
    }
}
