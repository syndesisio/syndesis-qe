package io.syndesis.qe.rest.tests.integrations;

import java.security.GeneralSecurityException;

import cucumber.api.java.en.Given;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Dec 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class ConnectionsGeneralSteps {

	private final ConnectionsEndpoint connectionsEndpoint;
	private final ConnectorsEndpoint connectorsEndpoint;
	private final AccountsDirectory accountsDirectory;

	public ConnectionsGeneralSteps() throws GeneralSecurityException {
		connectionsEndpoint = new ConnectionsEndpoint(RestConstants.getInstance().getSyndesisURL());
		connectorsEndpoint = new ConnectorsEndpoint(RestConstants.getInstance().getSyndesisURL());
		accountsDirectory = new AccountsDirectory();
	}

	@Given("^creates the TW connection using \"([^\"]*)\" template")
	public void createTwitterConnection(String twitterTemplate) throws GeneralSecurityException {

		final Connector twitterConnector = connectorsEndpoint.get("twitter");
		final Account twitterAccount = accountsDirectory.getAccount(twitterTemplate).get();
		log.info("Template name:  {}", twitterTemplate);

		final Connection twitterConnection = new Connection.Builder()
				.connector(twitterConnector)
				.connectorId(twitterConnector.getId())
				.id(RestConstants.getInstance().getTWITTER_CONNECTION_ID())
				.name("New Fuse QE twitter listen")
				.configuredProperties(TestUtils.map(
						"accessToken", twitterAccount.getProperty("accessToken"),
						"accessTokenSecret", twitterAccount.getProperty("accessTokenSecret"),
						"consumerKey", twitterAccount.getProperty("consumerKey"),
						"consumerSecret", twitterAccount.getProperty("consumerSecret")
				))
				.build();
		log.info("Creating twitter connection {}", twitterConnection.getName());
		connectionsEndpoint.create(twitterConnection);
	}

	@Given("^creates SF connection")
	public void createSalesforceConnections() {

		final Account salesforceAccount = accountsDirectory.getAccount("salesforce").get();
		final Connector salesforceConnector = connectorsEndpoint.get("salesforce");

		final Connection salesforceConnection = new Connection.Builder()
				.connector(salesforceConnector)
				.connectorId(salesforceConnector.getId())
				.id(RestConstants.getInstance().getSALESFORCE_CONNECTION_ID())
				.icon("fa-puzzle-piece")
				.name("New Fuse QE salesforce")
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
}
