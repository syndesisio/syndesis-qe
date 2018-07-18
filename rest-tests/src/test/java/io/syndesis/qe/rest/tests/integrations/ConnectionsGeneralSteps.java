package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
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

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;
    private final AccountsDirectory accountsDirectory;

    public ConnectionsGeneralSteps() {
        accountsDirectory = AccountsDirectory.getInstance();
    }

    @Given("^create the TW connection using \"([^\"]*)\" template")
    public void createTwitterConnection(String twitterTemplate) {

        final Connector twitterConnector = connectorsEndpoint.get("twitter");
        final Account twitterAccount = accountsDirectory.getAccount(twitterTemplate).get();
        log.info("Template name:  {}", twitterTemplate);

        final Connection twitterConnection = new Connection.Builder()
                .connector(twitterConnector)
                .connectorId(getConnectorId(twitterConnector))
                .id(RestConstants.TWITTER_CONNECTION_ID)
                .name("Fuse QE twitter listen")
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
        final Account salesforceAccount = accountsDirectory.getAccount("QE Salesforce").get();
        final Connector salesforceConnector = connectorsEndpoint.get("salesforce");

        final Connection salesforceConnection = new Connection.Builder()
                .connector(salesforceConnector)
                .connectorId(getConnectorId(salesforceConnector))
                .id(RestConstants.SALESFORCE_CONNECTION_ID)
                .icon("fa-puzzle-piece")
                .name("Fuse QE salesforce")
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


    @Given("^create Dropbox connection")
    public void createDropboxConnection() {

        final Account dropboxAccount = accountsDirectory.getAccount("QE Dropbox").get();
        final Connector dropboxConnector = connectorsEndpoint.get("dropbox");

        final Connection dropboxConnection = new Connection.Builder()
                .connector(dropboxConnector)
                .connectorId(getConnectorId(dropboxConnector))
                .id(RestConstants.DROPBOX_CONNECTION_ID)
                .icon("fa-dropbox")
                .name("Fuse QE Dropbox")
                .configuredProperties(TestUtils.map(
                        "accessToken", dropboxAccount.getProperty("accessToken"),
                        "clientIdentifier", dropboxAccount.getProperty("clientIdentifier")))
                .tags(Arrays.asList("dropbox"))
                .build();

        log.info("Creating Dropbox connection *{}*", dropboxConnection.getName());
        connectionsEndpoint.create(dropboxConnection);
    }

    @Given("^create S3 connection using \"([^\"]*)\" bucket")
    public void createS3Connection(String s3Bucket) {

        final Connector s3Connector = connectorsEndpoint.get("aws-s3");
        final Account s3Account = accountsDirectory.getAccount("s3").get();
        log.info("Bucket name:  {}", S3BucketNameBuilder.getBucketName(s3Bucket));

        final Connection s3Connection = new Connection.Builder()
                .connector(s3Connector)
                .connectorId(getConnectorId(s3Connector))
                .id(S3BucketNameBuilder.getBucketName(s3Bucket))
                .name("Fuse QE s3 " + S3BucketNameBuilder.getBucketName(s3Bucket))
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

    @Given("^create the FTP connection using \"([^\"]*)\" template")
    public void createFtpConnection(String ftpTemplate) {
        final Connector ftpConnector = connectorsEndpoint.get("ftp");
        final Account ftpAccount = accountsDirectory.getAccount(ftpTemplate).get();
        log.info("Template name: {}", ftpTemplate);

        final Connection ftpConnection = new Connection.Builder()
                .connector(ftpConnector)
                .connectorId(getConnectorId(ftpConnector))
                .id(RestConstants.FTP_CONNECTION_ID)
                .name("Fuse QE FTP")
                .configuredProperties(TestUtils.map(
                        "host", ftpAccount.getProperty("host"),
                        "port", ftpAccount.getProperty("port")
                ))
                .icon("fa-ftp")
                .tags(Arrays.asList("ftp"))
                .build();
        log.info("Creating ftp connection {}", ftpConnection.getName());
        connectionsEndpoint.create(ftpConnection);
    }

    @Given("^create AMQ connection")
    public void createAmqConnection() {
        final Connector amqConnector = connectorsEndpoint.get("activemq");
        final Account amqAccount = accountsDirectory.getAccount("AMQ").get();
        final Connection amqConnection = new Connection.Builder()
                .name("Fuse QE ActiveMQ")
                .connector(amqConnector)
                .connectorId(getConnectorId(amqConnector))
                .id(RestConstants.AMQ_CONNECTION_ID)
                .configuredProperties(TestUtils.map(
                        "brokerUrl", amqAccount.getProperty("brokerUrl"),
                        "username", amqAccount.getProperty("username"),
                        "password", amqAccount.getProperty("password")
                ))
                .icon("fa-puzzle-piece")
                .tags(Arrays.asList("amq", "activemq"))
                .build();
        log.info("Creating ActiveMQ connection {}", amqConnection.getName());
        connectionsEndpoint.create(amqConnection);
    }

    @Given("^create Kafka connection$")
    public void createKafkaConnection() {
        final Connector kafkaConnector = connectorsEndpoint.get("kafka");
        final Account kafkaAccount = accountsDirectory.getAccount("kafka").get();
        final Connection kafkaConnection = new Connection.Builder()
                .name("Fuse QE Kafka")
                .connector(kafkaConnector)
                .connectorId(getConnectorId(kafkaConnector))
                .id(RestConstants.KAFKA_CONNECTION_ID)
                .configuredProperties(TestUtils.map(
                        "brokers", kafkaAccount.getProperty("brokerUrl")
                ))
                .icon("fa-puzzle-piece")
                .tags(Arrays.asList("kafka"))
                .build();
        log.info("Creating Kafka connection {}", kafkaConnection.getName());
        connectionsEndpoint.create(kafkaConnection);
    }

    private String getConnectorId(Connector connector) {
        return connector.getId().orElseThrow(() -> new IllegalArgumentException("Connector ID is null"));
    }

    @Given("^create HTTP connection$")
    public void createHTTPConnection() {
        final Connector httpConnector = connectorsEndpoint.get("http4");
        final Account httpAccount = accountsDirectory.getAccount("http").get();
        final Connection httpConnection = new Connection.Builder()
                .name("Fuse QE HTTP")
                .connector(httpConnector)
                .connectorId(getConnectorId(httpConnector))
                .id(RestConstants.HTTP_CONNECTION_ID)
                .configuredProperties(TestUtils.map(
                        "baseUrl", httpAccount.getProperty("baseUrlHttp")
                ))
                .icon("fa-puzzle-piece")
                .tags(Arrays.asList("http"))
                .build();
        log.info("Creating HTTP connection {}", httpConnection.getName());
        connectionsEndpoint.create(httpConnection);
    }
}
