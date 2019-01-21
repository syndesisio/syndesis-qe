package io.syndesis.qe.rest.tests.integrations;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Dec 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class ConnectionSteps {

    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;
    private final AccountsDirectory accountsDirectory;

    public ConnectionSteps() {
        accountsDirectory = AccountsDirectory.getInstance();
    }

    private String getConnectorId(Connector connector) {
        return connector.getId().orElseThrow(() -> new IllegalArgumentException("Connector ID is null"));
    }

    @Given("^create connection")
    public void createConnection(DataTable connectionProperties) {
        Map<String, String> connectionPropertiesMap = new HashMap<>(connectionProperties.asMap(String.class, String.class));

        final String connectorName = connectionPropertiesMap.get("connector").toUpperCase();
        final String connectorId = RestTestsUtils.Connector.valueOf(connectorName).getId();
        final String connectionName = connectionPropertiesMap.get("name");

        final Connector connector = connectorsEndpoint.get(connectorId);
        final Optional<Account> account = accountsDirectory.getAccount(connectionPropertiesMap.get("account"));

        connectionPropertiesMap.remove("connectorId");
        connectionPropertiesMap.remove("account");
        connectionPropertiesMap.remove("name");

        for (Map.Entry<String, String> keyValue : connectionPropertiesMap.entrySet()) {
            if ("$ACCOUNT$".equals(keyValue.getValue())) {
                if (!account.isPresent()) {
                    throw new RuntimeException("Account " + connectionPropertiesMap.get("account") + " was not found");
                }
                keyValue.setValue(account.get().getProperty(keyValue.getKey()));
            }
        }

        final Connection connection = new Connection.Builder()
                .connector(connector)
                .connectorId(getConnectorId(connector))
                .id(RestTestsUtils.Connection.valueOf(connectorName).getId())
                .name(connectionName != null ? connectionName : "Fuse QE " + connectorName)
                .configuredProperties(connectionPropertiesMap)
                .icon(connector.getIcon())
                .tags(Collections.singletonList(connectorId))
                .build();
        log.info("Creating {} connection with properties {}", connectorId, connectionPropertiesMap);
        connectionsEndpoint.create(connection);
    }

    @Given("^create ActiveMQ connection")
    public void createActiveMQConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("AMQ").get();
        table.add(Arrays.asList("connector", "activemq"));
        table.add(Arrays.asList("brokerUrl", account.getProperty("brokerUrl")));
        table.add(Arrays.asList("username", account.getProperty("username")));
        table.add(Arrays.asList("password", account.getProperty("password")));
        createConnection(DataTable.create(table));
    }

    @Given("^create FTP connection")
    public void createFTPConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("ftp").get();
        table.add(Arrays.asList("connector", "ftp"));
        table.add(Arrays.asList("host", account.getProperty("host")));
        table.add(Arrays.asList("port", account.getProperty("port")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Dropbox connection")
    public void createDropboxConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("QE Dropbox").get();
        table.add(Arrays.asList("connector", "dropbox"));
        table.add(Arrays.asList("accessToken", account.getProperty("accessToken")));
        table.add(Arrays.asList("clientIdentifier", account.getProperty("clientIdentifier")));
        createConnection(DataTable.create(table));
    }

    @Given("^create HTTP connection")
    public void createHTTPConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("http").get();
        table.add(Arrays.asList("connector", "http"));
        table.add(Arrays.asList("baseUrl", account.getProperty("baseUrl")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Kafka connection")
    public void createKafkaConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("kafka").get();
        table.add(Arrays.asList("connector", "kafka"));
        table.add(Arrays.asList("brokers", account.getProperty("brokers")));
        createConnection(DataTable.create(table));
    }

    @Given("^create SalesForce connection")
    public void createSalesForceConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("QE Salesforce").get();
        table.add(Arrays.asList("connector", "salesforce"));
        table.add(Arrays.asList("clientId", account.getProperty("clientId")));
        table.add(Arrays.asList("clientSecret", account.getProperty("clientSecret")));
        table.add(Arrays.asList("loginUrl", account.getProperty("loginUrl")));
        table.add(Arrays.asList("userName", account.getProperty("userName")));
        table.add(Arrays.asList("password", account.getProperty("password")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Twitter connection")
    public void createTwitterConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount("twitter_talky").get();
        table.add(Arrays.asList("connector", "twitter"));
        table.add(Arrays.asList("accessToken", account.getProperty("accessToken")));
        table.add(Arrays.asList("accessTokenSecret", account.getProperty("accessTokenSecret")));
        table.add(Arrays.asList("consumerKey", account.getProperty("consumerKey")));
        table.add(Arrays.asList("consumerSecret", account.getProperty("consumerSecret")));
        createConnection(DataTable.create(table));
    }

    @Given("^create S3 connection using \"([^\"]*)\" bucket")
    public void createS3Connection(String s3Bucket) {
        final Connector s3Connector = connectorsEndpoint.get(RestTestsUtils.Connector.S3.getId());
        final Account s3Account = accountsDirectory.getAccount("s3").get();
        log.info("Bucket name: {}", S3BucketNameBuilder.getBucketName(s3Bucket));

        final Connection s3Connection = new Connection.Builder()
                .connector(s3Connector)
                .connectorId(getConnectorId(s3Connector))
                .id(S3BucketNameBuilder.getBucketName(s3Bucket))
                .name("Fuse QE S3 " + S3BucketNameBuilder.getBucketName(s3Bucket))
                .configuredProperties(TestUtils.map(
                        "accessKey", s3Account.getProperty("accessKey"),
                        "bucketNameOrArn", S3BucketNameBuilder.getBucketName(s3Bucket),
                        "region", s3Account.getProperty("region"),
                        "secretKey", s3Account.getProperty("secretKey")
                ))
                .tags(Collections.singletonList("aws-s3"))
                .build();

        log.info("Creating s3 connection {}", s3Connection.getName());
        connectionsEndpoint.create(s3Connection);
    }
}

