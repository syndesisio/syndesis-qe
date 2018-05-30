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
                .connectorId(getConnectorId(salesforceConnector))
                .id(RestConstants.SALESFORCE_CONNECTION_ID)
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


    @Given("^create Dropbox connection")
    public void createDropboxConnection() {

        final Account dropboxAccount = accountsDirectory.getAccount("QE Dropbox").get();
        final Connector dropboxConnector = connectorsEndpoint.get("dropbox");

        final Connection dropboxConnection = new Connection.Builder()
                .connector(dropboxConnector)
                .connectorId(getConnectorId(dropboxConnector))
                .id(RestConstants.DROPBOX_CONNECTION_ID)
                .icon("fa-dropbox")
                .name("New Fuse QE Dropbox")
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

    @Given("^create the FTP connection using \"([^\"]*)\" template")
    public void createFtpConnection(String ftpTemplate) {

        connectionsEndpoint.list().forEach(c -> log.info(c.getName()));
        final Connector ftpConnector = connectorsEndpoint.get("ftp");
        final Account ftpAccount = accountsDirectory.getAccount(ftpTemplate).get();
        log.info("Template name:  {}", ftpTemplate);

        final Connection ftpConnection = new Connection.Builder()
                .connector(ftpConnector)
                .connectorId(getConnectorId(ftpConnector))
                .id(RestConstants.FTP_CONNECTION_ID)
                .name("New Fuse QE FTP")
                .configuredProperties(TestUtils.map(
//                        "binary", ftpAccount.getProperty("binary"),
//                        "connectTimeout", ftpAccount.getProperty("connectTimeout"),
//                        "disconnect", ftpAccount.getProperty("disconnect"),
                        "host", ftpAccount.getProperty("host"),
//                        "maximumReconnectAttempts", ftpAccount.getProperty("maximumReconnectAttempts"),
//                        "passiveMode", ftpAccount.getProperty("passiveMode"),
//                        "password", ftpAccount.getProperty("password"),
                        "port", ftpAccount.getProperty("port")
//                        "reconnectDelay", ftpAccount.getProperty("reconnectDelay"),
//                        "timeout", ftpAccount.getProperty("timeout"),
//                        "username", ftpAccount.getProperty("username")
                ))
                .icon("fa-ftp")
                .tags(Arrays.asList("ftp"))
                .build();
        log.info("Creating ftp connection {}", ftpConnection.getName());
        connectionsEndpoint.create(ftpConnection);
    }

    private String getConnectorId(Connector connector) {
        return connector.getId().orElseThrow(() -> new IllegalArgumentException("Connector ID is null"));
    }
}
