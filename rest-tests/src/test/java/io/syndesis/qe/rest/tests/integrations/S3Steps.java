package io.syndesis.qe.rest.tests.integrations;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 2, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class S3Steps {

	private final ConnectionsEndpoint connectionsEndpoint;
	private final ConnectorsEndpoint connectorsEndpoint;
	private final IntegrationsEndpoint integrationsEndpoint;
	private final List<Step> steps = new ArrayList<>();

	public S3Steps() throws GeneralSecurityException {
		connectorsEndpoint = new ConnectorsEndpoint();
		connectionsEndpoint = new ConnectionsEndpoint();
		integrationsEndpoint = new IntegrationsEndpoint();
	}

	@Given("^create S3 polling step with bucket: \"([^\"]*)\"")
	public void createS3PollingStep(String bucketName) {
		final Connector s3Connector = connectorsEndpoint.get("aws-s3");
		final Connection s3Connection = connectionsEndpoint.get(bucketName);
		final Step s3Step = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(s3Connection)
				.action(TestUtils.findConnectorAction(s3Connector, "aws-s3-polling-bucket-connector"))
				.configuredProperties(TestUtils.map("deleteAfterRead", "false",
						"maxMessagesPerPoll", "10",
						"delay", "1000"))
				.build();

		steps.add(s3Step);
	}

	@Given("^create S3 copy step with bucket: \"([^\"]*)\"")
	public void createS3CopyStep(String bucketName) {
		final Connector s3Connector = connectorsEndpoint.get("aws-s3");
		final Connection s3Connection = connectionsEndpoint.get(bucketName);
		final Step s3Step = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(s3Connection)
				.action(TestUtils.findConnectorAction(s3Connector, "aws-s3-copy-object-connector"))
				.build();

		steps.add(s3Step);
	}

	@When("^create S3 to S3 integration with name: \"([^\"]*)\"")
	public void createIntegrationFromGivenSteps(String integrationName) throws GeneralSecurityException {

		Integration integration = new Integration.Builder()
				.steps(steps)
				.name(integrationName)
				.desiredStatus(Integration.Status.Activated)
				.build();

		log.info("Creating integration {}", integration.getName());
		integration = integrationsEndpoint.create(integration);
	}
}
