package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import cucumber.api.java.en.Given;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * DB steps for integrations.
 *
 * Oct 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class DbSteps {

	@Autowired
	private StepsStorage steps;

	private final ConnectionsEndpoint connectionsEndpoint;
	private final ConnectorsEndpoint connectorsEndpoint;

	public DbSteps() {
		connectorsEndpoint = new ConnectorsEndpoint();
		connectionsEndpoint = new ConnectionsEndpoint();
	}

	@Given("^create DB step from template add_lead")
	public void createDbTemplateStep() {
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
		steps.getSteps().add(dbStep);
	}

	@Given("^create DB insert taks step")
	public void createDbInsertStep() {
		final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
		final Connector dbConnector = connectorsEndpoint.get("sql");

		final Step dbStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(dbConnection)
				.action(TestUtils.findConnectorAction(dbConnector, "sql-connector"))
				.configuredProperties(TestUtils.map("query", "INSERT INTO TODO (task) VALUES (:#task)"))
				.build();
		steps.getSteps().add(dbStep);
	}

	@Given("^create DB step with query: \"([^\"]*)\" and interval: (\\d+) miliseconds")
	public void createDbStepWithInterval(String query, int interval) {
		final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());
		final Connector dbConnector = connectorsEndpoint.get("sql");
		final Step dbStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(dbConnection)
				.action(TestUtils.findConnectorAction(dbConnector, "sql-connector"))
				.configuredProperties(TestUtils.map("query", query, "schedulerPeriod", interval))
				.build();
		steps.getSteps().add(dbStep);
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
