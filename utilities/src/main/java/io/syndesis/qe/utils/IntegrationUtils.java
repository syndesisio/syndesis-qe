package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.metrics.IntegrationMetricsSummary;
import io.syndesis.qe.TestConfiguration;
import io.syndesis.qe.endpoints.ActivityIntegrationsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsMetricsEndpoint;
import io.syndesis.server.endpoint.v1.handler.activity.Activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntegrationUtils {

    @Autowired
    private IntegrationsMetricsEndpoint integrationsMetricsEndpoint;

    @Autowired
    private ActivityIntegrationsEndpoint activityIntegrationsEndpoint;

    @Autowired
    private IntegrationsEndpoint integrationsEndpoint;

    public String getIdByIntegrationName(String integrationName) {
        List<Integration> integrations = integrationsEndpoint.list();
        Integration integr =
            integrations.stream().filter(integration -> integrationName.equalsIgnoreCase(integration.getName())).findAny().orElse(null);
        if (integr == null) {
            integr =
                integrations.stream().filter(integration -> integrationName.replaceAll("-", " ").equalsIgnoreCase(integration.getName())).findAny()
                    .orElse(null);
        }
        assertThat(integr).as(String
            .format("Integration %s not found. Be sure you provide the correct name of the integration. (same name as in the UI)", integrationName))
            .isNotNull();
        return integr.getId().get();
    }

    public int numberOfMessages(String integrationName) {
        String integrationId = this.getIdByIntegrationName(integrationName);
        IntegrationMetricsSummary summary = integrationsMetricsEndpoint.get(integrationId);
        return summary.getMessages().intValue();
    }

    public List<Activity> getAllIntegrationActivities(String integrationName) {
        return activityIntegrationsEndpoint.list(this.getIdByIntegrationName(integrationName));
    }

    public IntegrationMetricsSummary getIntegrationMetrics(String integrationName) {
        return integrationsMetricsEndpoint.get(this.getIdByIntegrationName(integrationName));
    }

    public void waitForMessage(String integrationName, int numberOfMessages) {
        waitForMessage(integrationName, numberOfMessages, TestConfiguration.getConfigTimeout());
    }

    public void waitForMessage(String integrationName, int numberOfMessages, int timeoutInSeconds) {
        TestUtils.waitFor(() ->
                this.numberOfMessages(integrationName) >= numberOfMessages, 3, timeoutInSeconds,
            String.format("The message doesn't go through integration in time. Expected messages: %s, actual: %s", numberOfMessages,
                this.numberOfMessages(integrationName)));
    }
}
