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
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Steps for Salesforce to DB contact integration.
 *
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SfDbSteps {

	private final ConnectionsEndpoint connectionsEndpoint;
	private final ConnectorsEndpoint connectorsEndpoint;
	private final IntegrationsEndpoint integrationsEndpoint;
	private final List<Step> steps = new ArrayList<>();

	public SfDbSteps() throws GeneralSecurityException {
		connectorsEndpoint = new ConnectorsEndpoint(RestConstants.getInstance().getSyndesisURL());
		connectionsEndpoint = new ConnectionsEndpoint(RestConstants.getInstance().getSyndesisURL());
		integrationsEndpoint = new IntegrationsEndpoint(RestConstants.getInstance().getSyndesisURL());
	}

	@Given("^create SF step for SF DB test")
	public void createSalesforceStep() {
		final Connector salesforceConnector = connectorsEndpoint.get("salesforce");
		final Connection salesforceConnection = connectionsEndpoint.get(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID());
		final Step salesforceStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(salesforceConnection)
				.action(TestUtils.findConnectorAction(salesforceConnector, "salesforce-on-create"))
				.configuredProperties(TestUtils.map("sObjectName", "Lead"))
				.build();

		steps.add(salesforceStep);
	}

	@Given("^create SF DB mapper step")
	public void createMapperStep() throws IOException {
		final String mapping = new String(Files.readAllBytes(Paths.get("./target/test-classes/mappings/salesforce-db.json")));
		final Step mapperStep = new SimpleStep.Builder()
				.stepKind("mapper")
				.configuredProperties(TestUtils.map("atlasmapping", mapping))
				.build();
		steps.add(mapperStep);
	}

	@Given("^create DB step")
	public void createDbStep() {
		final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
		final Connector dbConnector = connectorsEndpoint.get("sql");

		final Step dbStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(dbConnection)
				.action(TestUtils.findConnectorAction(dbConnector, "sql-stored-connector"))
				.configuredProperties(TestUtils.map("procedureName", "add_lead",
						"template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]}, "
						+ "VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})"))
				.build();
		steps.add(dbStep);
	}

	@When("^create SF to DB integration with name: \"([^\"]*)\"$")
	public void createIntegrationFromGivenSteps(String integrationName) throws GeneralSecurityException {

		Integration integration = new Integration.Builder()
				.steps(steps)
				.name(integrationName)
				.desiredStatus(Integration.Status.Activated)
				.build();

		log.info("Creating integration {}", integration.getName());
		integration = integrationsEndpoint.create(integration);
	}

	private String getDbConnectionId() {

		final String postgresDbName = "PostgresDB";
		List<Connection> connects = null;

		connects = connectionsEndpoint.list();
		String dbConnectionId = null;
		for (Connection s : connects) {
			if (s.getName().equals(postgresDbName)) {
				dbConnectionId = (String) s.getId().get();
			}
		}
		log.debug("db connection id: " + dbConnectionId);
		return dbConnectionId;
	}
}
