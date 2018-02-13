package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import io.syndesis.model.metrics.IntegrationMetricsSummary;

@Component
public class MetricsEndpoint extends AbstractEndpoint<IntegrationMetricsSummary> {

    public MetricsEndpoint() {
        super(IntegrationMetricsSummary.class, "/metrics");
    }
}
