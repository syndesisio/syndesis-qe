package io.syndesis.qe.endpoint;

import io.syndesis.common.model.metrics.IntegrationMetricsSummary;

import org.springframework.stereotype.Component;

@Component
public class IntegrationsMetricsEndpoint extends AbstractEndpoint<IntegrationMetricsSummary> {

    public IntegrationsMetricsEndpoint() {
        super(IntegrationMetricsSummary.class, "/metrics/integrations");
    }
}
