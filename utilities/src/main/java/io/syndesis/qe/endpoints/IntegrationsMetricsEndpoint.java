package io.syndesis.qe.endpoints;

import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;

import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

@Component
public class IntegrationsMetricsEndpoint extends AbstractEndpoint<IntegrationMetricsSummary> {

    public IntegrationsMetricsEndpoint() throws GeneralSecurityException {
        super(IntegrationMetricsSummary.class, "/metrics/integrations");
    }
}
