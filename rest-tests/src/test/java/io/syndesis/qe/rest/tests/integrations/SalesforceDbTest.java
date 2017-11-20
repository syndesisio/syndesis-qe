package io.syndesis.qe.rest.tests.integrations;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hamcrest.CoreMatchers;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.force.api.QueryResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.SimpleStep;
import io.syndesis.model.integration.Step;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.endpoints.IntegrationsEndpoint;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.rest.tests.AbstractSyndesisRestTest;
import io.syndesis.qe.salesforce.Lead;
import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.SampleDbConnectionManager;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

/**
 * Test for Salesforce to DB contact integration.
 *
 * Oct 26, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SalesforceDbTest extends AbstractSyndesisRestTest {

	private static final String SALESFORCE_CONNECTION_ID = "fuseqe-salesforce";
	private static final String INTEGRATION_NAME = "Salesforce to DB rest test";
	private static final String TEST_LEAD_FIRST_NAME = "John";
	private static final String TEST_LEAD_LAST_NAME = "Doe";
	private static final String TEST_LEAD_COMPANY_NAME = "ACME";
	private static final String TEST_LEAD_EMAIL = "johndoe@example.com";
	private static final String TODO_APP_NAME = "todo";

	private String mapping;
	private AccountsDirectory accountsDirectory;
	private Connector salesforceConnector;
	private Connector dbConnector;
	private ConnectionsEndpoint connectionsEndpoint;
	private ConnectorsEndpoint connectorsEndpoint;
	private IntegrationsEndpoint integrationsEndpoint;
	private ForceApi salesforce;
	private DbUtils dbUtils;

	@Before
	public void before() throws IOException, TwitterException {
		accountsDirectory = new AccountsDirectory();
		final Account salesforceAccount = accountsDirectory.getAccount("salesforce").get();
		dbUtils = new DbUtils(SampleDbConnectionManager.getInstance().getConnection());
		salesforce = new ForceApi(new ApiConfig()
				.setClientId(salesforceAccount.getProperty("clientId"))
				.setClientSecret(salesforceAccount.getProperty("clientSecret"))
				.setUsername(salesforceAccount.getProperty("userName"))
				.setPassword(salesforceAccount.getProperty("password"))
				.setForceURL(salesforceAccount.getProperty("loginUrl")));

		cleanup();
	}

	private void cleanup() throws TwitterException {
		TestSupport.resetDB();
		deleteSalesforceLead(salesforce);
		dbUtils.deleteRecordsInTable(TODO_APP_NAME);
	}

	@After
	public void tearDown() throws TwitterException {
		cleanup();
		SampleDbConnectionManager.getInstance().closeConnection();
	}

	@Test
	public void testIntegration() throws GeneralSecurityException, IOException, TwitterException, InterruptedException {

		mapping = new String(Files.readAllBytes(Paths.get("./target/test-classes/mappings/salesforce-db.json")));

		connectorsEndpoint = new ConnectorsEndpoint(syndesisURL);
		connectionsEndpoint = new ConnectionsEndpoint(syndesisURL);
		integrationsEndpoint = new IntegrationsEndpoint(syndesisURL);

		dbConnector = connectorsEndpoint.get("sql-stored-connector");
		salesforceConnector = connectorsEndpoint.get("salesforce");
		createSfConnection();
		createIntegration();
		validateIntegration();
		log.info("Salesforce to DB integration test finished.");
	}

	private void createSfConnection() {
		final Account salesforceAccount = accountsDirectory.getAccount("salesforce").get();

		final Connection salesforceConnection = new Connection.Builder()
				.connector(salesforceConnector)
				.connectorId(salesforceConnector.getId())
				.id(SALESFORCE_CONNECTION_ID)
				.name("Fuse QE salesforce-db")
				.configuredProperties(TestUtils.map(
						"clientId", salesforceAccount.getProperty("clientId"),
						"clientSecret", salesforceAccount.getProperty("clientSecret"),
						"loginUrl", salesforceAccount.getProperty("loginUrl"),
						"userName", salesforceAccount.getProperty("userName"),
						"password", salesforceAccount.getProperty("password")))
				.build();

		log.info("Creating salesforce connection {}", salesforceConnection.getName());
		connectionsEndpoint.create(salesforceConnection);
	}

	private void createIntegration() {

		final Connection salesforceConnection = connectionsEndpoint.get(SALESFORCE_CONNECTION_ID);
		final Connection dbConnection = connectionsEndpoint.get(getDbConnectionId());

		final Step salesforceStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(salesforceConnection)
				.action(TestUtils.findAction(salesforceConnector, "salesforce-on-create"))
				.configuredProperties(TestUtils.map("sObjectName", "Lead"))
				.build();

		final Step mapperStep = new SimpleStep.Builder()
				.stepKind("mapper")
				.configuredProperties(TestUtils.map("atlasmapping", mapping))
				.build();

		final Step dbStep = new SimpleStep.Builder()
				.stepKind("endpoint")
				.connection(dbConnection)
				.action(TestUtils.findAction(dbConnector, "sql-stored-connector"))
				.configuredProperties(TestUtils.map("procedureName", "add_lead",
						"template", "add_lead(VARCHAR ${body[first_and_last_name]}, VARCHAR ${body[company]}, VARCHAR ${body[phone]}, VARCHAR ${body[email]},"
						+ " VARCHAR ${body[lead_source]}, VARCHAR ${body[lead_status]}, VARCHAR ${body[rating]})"))
				.build();

		Integration integration = new Integration.Builder()
				.steps(Arrays.asList(salesforceStep, mapperStep, dbStep))
				.name(INTEGRATION_NAME)
				.desiredStatus(Integration.Status.Activated)
				.build();

		log.info("Creating integration {}", integration.getName());
		integration = integrationsEndpoint.create(integration);

		final long start = System.currentTimeMillis();
		//wait for activation
		log.info("Waiting until integration becomes active. This may take a while...");
		final boolean activated = TestUtils.waitForActivation(integrationsEndpoint, integration, TimeUnit.MINUTES, 10);
		assertThat(activated, is(true));
		log.info("Integration pod has been started. It took {}s to build the integration.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
	}

	private void validateIntegration() {
		// The table has to be empty prior to testing.
		assertThat(dbUtils.getNumberOfRecordsInTable(TODO_APP_NAME), equalTo(0));
		createNewSalesforceLead();
		final long start = System.currentTimeMillis();
		// We wait for exactly 1 record to appead in DB.
		final boolean contactCreated = TestUtils.waitForEvent(lead -> lead == 1, () -> dbUtils.getNumberOfRecordsInTable(TODO_APP_NAME),
				TimeUnit.MINUTES,
				2,
				TimeUnit.SECONDS,
				5);
		assertThat("Lead record has appeard in db", contactCreated, is(true));
		log.info("Lead record appeared in DB. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
		// Now we verify, the created contact contains the lead personal information.
		assertThat(getLeadTaskFromDb().toLowerCase(), CoreMatchers.containsString(TEST_LEAD_EMAIL.toLowerCase()));
	}

	/**
	 * Used for verification of successful creation of a new task in the todo app.
	 *
	 * @return
	 */
	private String getLeadTaskFromDb() {

		String leadTask = null;
		try (ResultSet rs = dbUtils.executeSqlOnSampleDb("SELECT ID, TASK, COMPLETED FROM todo where task like '%"
				+ TEST_LEAD_FIRST_NAME + " " + TEST_LEAD_LAST_NAME + "%'");) {
			if (rs.next()) {
				leadTask = rs.getString("TASK");
				log.debug("TASK = " + leadTask);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		return leadTask;
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
		return dbConnectionId;
	}

	private String createNewSalesforceLead() {
		final Lead lead = new Lead();
		lead.setFirstName(TEST_LEAD_FIRST_NAME);
		lead.setLastName(TEST_LEAD_LAST_NAME);
		lead.setCompany(TEST_LEAD_COMPANY_NAME);
		lead.setEmail(TEST_LEAD_EMAIL);

		final String id = salesforce.createSObject("lead", lead);
		return id;
	}

	/**
	 * Looks for leads with specified first and last name and deletes them if it finds any.
	 *
	 * @param salesforce
	 */
	private void deleteSalesforceLead(ForceApi salesforce) {
		final Optional<Lead> lead = getSalesforceLead(salesforce);
		if (lead.isPresent()) {
			final String id = String.valueOf(lead.get().getId());
			salesforce.deleteSObject("lead", id);
			log.info("Deleting salesforce lead: {}", lead.get());
			deleteSalesforceLead(salesforce);
		}
	}

	private Optional<Lead> getSalesforceLead(ForceApi salesforce) {
		final QueryResult<Lead> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Email,Company FROM lead where FirstName = '"
				+ TEST_LEAD_FIRST_NAME + "' and LastName='" + TEST_LEAD_LAST_NAME + "'", Lead.class);
		final Optional<Lead> lead = queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
		return lead;
	}
}
