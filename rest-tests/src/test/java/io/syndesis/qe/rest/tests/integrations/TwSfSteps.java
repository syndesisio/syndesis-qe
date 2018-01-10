package io.syndesis.qe.rest.tests.integrations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.filter.FilterPredicate;
import io.syndesis.model.filter.RuleFilterStep;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.FilterRulesBuilder;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Steps for Twitter mention to Salesforce upsert contact integration.
 *
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class TwSfSteps {

	public static final String SYNDESIS_TALKY_ACCOUNT = "twitter_talky";

	private final ConnectionsEndpoint connectionsEndpoint;
	private final ConnectorsEndpoint connectorsEndpoint;
	private final IntegrationsEndpoint integrationsEndpoint;
	private final List<Step> steps = new ArrayList<>();

	public TwSfSteps() throws GeneralSecurityException {
		connectorsEndpoint = new ConnectorsEndpoint(RestConstants.getInstance().getSyndesisURL());
		connectionsEndpoint = new ConnectionsEndpoint(RestConstants.getInstance().getSyndesisURL());
		integrationsEndpoint = new IntegrationsEndpoint(RestConstants.getInstance().getSyndesisURL());
	}

	@Given("^create TW mention step with \"([^\"]*)\" action")
	public void createTwitterStep(String twitterAction) {

		final Connector twitterConnector = connectorsEndpoint.get("twitter");
		final Connection twitterConnection = connectionsEndpoint.get(RestConstants.getInstance().getTWITTER_CONNECTION_ID());
		final Step twitterStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(twitterConnection)
				.action(TestUtils.findConnectorAction(twitterConnector, twitterAction))
				.build();
		steps.add(twitterStep);
	}

	@Given("^create TW to SF mapper step")
	public void createMapperStep() throws IOException {
		final String mapping = new String(Files.readAllBytes(Paths.get("./target/test-classes/mappings/twitter-salesforce.json")));

		final Step mapperStep = new SimpleStep.Builder()
				.stepKind("mapper")
				.configuredProperties(TestUtils.map("atlasmapping", mapping))
				.build();
		steps.add(mapperStep);
	}

	@Given("^create basic TW to SF filter step")
	public void createBasicFilterStep() {
		final Step basicFilter = new RuleFilterStep.Builder()
				.configuredProperties(TestUtils.map(
						"type", "rule",
						"predicate", FilterPredicate.AND.toString(),
						"rules", new FilterRulesBuilder().addPath("text").addValue("#backendTest").addOps("contains").build()
				))
				.build();
		steps.add(basicFilter);
	}

	@Given("^create SF step for TW SF test")
	public void createSalesforceStep() {
		final Connector salesforceConnector = connectorsEndpoint.get("salesforce");

		final Connection salesforceConnection = connectionsEndpoint.get(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
		final Step salesforceStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(salesforceConnection)
				.action(TestUtils.findConnectorAction(salesforceConnector, "salesforce-create-sobject"))
				.build();
		steps.add(salesforceStep);
	}

	@When("^create TW to SF integration with name: \"([^\"]*)\"$")
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
