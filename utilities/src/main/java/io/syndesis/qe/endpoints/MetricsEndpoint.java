package io.syndesis.qe.endpoints;

import java.security.GeneralSecurityException;

import io.syndesis.model.integration.Integration;
import io.syndesis.model.metrics.IntegrationMetricsSummary;

public class MetricsEndpoint extends AbstractEndpoint<IntegrationMetricsSummary> {

	public MetricsEndpoint() throws GeneralSecurityException {
		super(IntegrationMetricsSummary.class, "/metrics");
	}
}

