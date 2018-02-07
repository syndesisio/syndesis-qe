package io.syndesis.qe.rest.tests.integrations;

import java.util.Arrays;

import cucumber.api.java.en.Given;
import io.syndesis.model.connection.Connection;
import io.syndesis.model.connection.Connector;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.S3BucketNameBuilder;
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

	public ConnectionsGeneralSteps() {
		connectionsEndpoint = new ConnectionsEndpoint();
		connectorsEndpoint = new ConnectorsEndpoint();
		accountsDirectory = AccountsDirectory.getInstance();
	}

	@Given("^create the TW connection using \"([^\"]*)\" template")
	public void createTwitterConnection(String twitterTemplate) {

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
				.icon("fa-twitter")
				.tags(Arrays.asList("twitter"))
				.build();
		log.info("Creating twitter connection {}", twitterConnection.getName());
		connectionsEndpoint.create(twitterConnection);
	}

	@Given("^create SF connection")
	public void createSalesforceConnection() {

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
				.tags(Arrays.asList("salesforce"))
				.build();

		log.info("Creating salesforce connection {}", salesforceConnection.getName());
		connectionsEndpoint.create(salesforceConnection);
	}

	@Given("^create S3 connection using \"([^\"]*)\" bucket")
	public void createS3Connection(String s3Bucket) {

		final Connector s3Connector = connectorsEndpoint.get("aws-s3");
		final Account s3Account = accountsDirectory.getAccount("s3").get();
		log.info("Bucket name:  {}", S3BucketNameBuilder.getBucketName(s3Bucket));

		final Connection s3Connection = new Connection.Builder()
				.connector(s3Connector)
				.connectorId(s3Connector.getId())
				.id(S3BucketNameBuilder.getBucketName(s3Bucket))
				.name("New Fuse QE s3 " + S3BucketNameBuilder.getBucketName(s3Bucket))
				.icon("fa-puzzle-piece")
				.configuredProperties(TestUtils.map(
						"accessKey", s3Account.getProperty("accessKey"),
						"bucketNameOrArn", S3BucketNameBuilder.getBucketName(s3Bucket),
						"region", s3Account.getProperty("region"),
						"secretKey", s3Account.getProperty("secretKey")
				))
				.tags(Arrays.asList("aws-s3"))
				.build();

		log.info("Creating s3 connection {}", s3Connection.getName());
		connectionsEndpoint.create(s3Connection);
	}
}
