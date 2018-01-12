package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.security.GeneralSecurityException;

import cucumber.api.java.en.Given;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 12, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SalesforceSteps {

	@Autowired
	private StepsStorage steps;

	private final ConnectionsEndpoint connectionsEndpoint;
	private final ConnectorsEndpoint connectorsEndpoint;

	public SalesforceSteps() throws GeneralSecurityException {
		connectorsEndpoint = new ConnectorsEndpoint();
		connectionsEndpoint = new ConnectionsEndpoint();
	}

	@Given("^create SF step for SF DB test")
	public void createSfDbStep() {
		final Connector salesforceConnector = connectorsEndpoint.get("salesforce");
		final Connection salesforceConnection = connectionsEndpoint.get(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
		final Step salesforceStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(salesforceConnection)
				.action(TestUtils.findConnectorAction(salesforceConnector, "salesforce-on-create"))
				.configuredProperties(TestUtils.map("sObjectName", "Lead"))
				.build();

		steps.getSteps().add(salesforceStep);
	}

	@Given("^create SF step for TW SF test")
	public void createSfTwStep() {
		final Connector salesforceConnector = connectorsEndpoint.get("salesforce");

		final Connection salesforceConnection = connectionsEndpoint.get(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
		final Step salesforceStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(salesforceConnection)
				.action(TestUtils.findConnectorAction(salesforceConnector, "salesforce-create-sobject"))
				.build();
		steps.getSteps().add(salesforceStep);
	}
}
