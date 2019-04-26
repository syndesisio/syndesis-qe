package io.syndesis.qe.rest.tests.steps.connection;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.java.en.Given;
import io.cucumber.datatable.DataTable;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Dec 7, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class Connections {
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;
    private final AccountsDirectory accountsDirectory;

    public Connections() {
        accountsDirectory = AccountsDirectory.getInstance();
    }

    private String getConnectorId(Connector connector) {
        return connector.getId().orElseThrow(() -> new IllegalArgumentException("Connector ID is null"));
    }

    @Given("^create connection$")
    public void createConnection(DataTable connectionProperties) {
        List<List<String>> cells = connectionProperties.cells();
        Map<String, String> connectionPropertiesMap = new HashMap<>();
        for (List<String> cell : cells) {
            connectionPropertiesMap.put(cell.get(0), cell.get(1));
        }

        final String connectorName = connectionPropertiesMap.get("connector").toUpperCase();
        final String connectorId = RestTestsUtils.Connector.valueOf(connectorName).getId();
        final String connectionId = connectionPropertiesMap.get("connectionId");
        final String connectionName = connectionPropertiesMap.get("name");

        final Connector connector = connectorsEndpoint.get(connectorId);
        final Optional<Account> account = accountsDirectory.getAccount(connectionPropertiesMap.get("account"));

        connectionPropertiesMap.remove("connector");
        connectionPropertiesMap.remove("connectionId");
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
                .id(connectionId != null ? connectionId : RestTestsUtils.Connection.valueOf(connectorName).getId())
                .name(connectionName != null ? connectionName : "Fuse QE " + connectorName)
                .configuredProperties(connectionPropertiesMap)
                .icon(connector.getIcon())
                .tags(Collections.singletonList(connectorId))
                .build();
        log.info("Creating {} connection with properties {}", connectorId, connectionPropertiesMap);
        connectionsEndpoint.create(connection);
    }

    @Given("^create ActiveMQ connection$")
    public void createActiveMQConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.ACTIVEMQ).get();
        table.add(Arrays.asList("connector", "activemq"));
        table.add(Arrays.asList("brokerUrl", account.getProperty("brokerUrl")));
        table.add(Arrays.asList("username", account.getProperty("username")));
        table.add(Arrays.asList("password", account.getProperty("password")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Box connection$")
    public void createBoxConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.BOX).get();
        table.add(Arrays.asList("connector", "box"));
        table.add(Arrays.asList("userName", account.getProperty("userName")));
        table.add(Arrays.asList("userPassword", account.getProperty("userPassword")));
        table.add(Arrays.asList("clientId", account.getProperty("clientId")));
        table.add(Arrays.asList("clientSecret", account.getProperty("clientSecret")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Dropbox connection$")
    public void createDropboxConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.DROPBOX).get();
        table.add(Arrays.asList("connector", "dropbox"));
        table.add(Arrays.asList("accessToken", account.getProperty("accessToken")));
        table.add(Arrays.asList("clientIdentifier", account.getProperty("clientIdentifier")));
        createConnection(DataTable.create(table));
    }

    @Given("^create FTP connection$")
    public void createFTPConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.FTP).get();
        table.add(Arrays.asList("connector", "ftp"));
        table.add(Arrays.asList("host", account.getProperty("host")));
        table.add(Arrays.asList("port", account.getProperty("port")));
        createConnection(DataTable.create(table));
    }

    @Given("^create HTTP connection$")
    public void createHTTPConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.HTTP).get();
        table.add(Arrays.asList("connector", "http"));
        table.add(Arrays.asList("baseUrl", account.getProperty("baseUrl")));
        createConnection(DataTable.create(table));
    }

    @Given("^create IRC connection$")
    public void createIRCConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.IRC).get();
        table.add(Arrays.asList("connector", "irc"));
        table.add(Arrays.asList("hostname", account.getProperty("hostname")));
        table.add(Arrays.asList("port", account.getProperty("port")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Kafka connection$")
    public void createKafkaConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.KAFKA).get();
        table.add(Arrays.asList("connector", "kafka"));
        table.add(Arrays.asList("brokers", account.getProperty("brokers")));
        createConnection(DataTable.create(table));
    }

    @Given("^create SalesForce connection$")
    public void createSalesForceConnection() {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.SALESFORCE).get();
        table.add(Arrays.asList("connector", "salesforce"));
        table.add(Arrays.asList("clientId", account.getProperty("clientId")));
        table.add(Arrays.asList("clientSecret", account.getProperty("clientSecret")));
        table.add(Arrays.asList("loginUrl", account.getProperty("loginUrl")));
        table.add(Arrays.asList("userName", account.getProperty("userName")));
        table.add(Arrays.asList("password", account.getProperty("password")));
        createConnection(DataTable.create(table));
    }

    @Given("^create Twitter connection using \"([^\"]*)\" account$")
    public void createTwitterConnection(String twitterAccount) {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(twitterAccount).get();
        table.add(Arrays.asList("connector", "twitter"));
        table.add(Arrays.asList("accessToken", account.getProperty("accessToken")));
        table.add(Arrays.asList("accessTokenSecret", account.getProperty("accessTokenSecret")));
        table.add(Arrays.asList("consumerKey", account.getProperty("consumerKey")));
        table.add(Arrays.asList("consumerSecret", account.getProperty("consumerSecret")));
        createConnection(DataTable.create(table));
    }

    @Given("^create S3 connection using \"([^\"]*)\" bucket$")
    public void createS3Connection(String s3Bucket) {
        final List<List<String>> table = new ArrayList<>();
        final Account account = accountsDirectory.getAccount(Account.Name.AWS).get();
        log.info("Bucket name: {}", S3BucketNameBuilder.getBucketName(s3Bucket));

        table.add(Arrays.asList("connector", "s3"));
        table.add(Arrays.asList("accessKey", account.getProperty("accessKey")));
        table.add(Arrays.asList("bucketNameOrArn", S3BucketNameBuilder.getBucketName(s3Bucket)));
        table.add(Arrays.asList("region", account.getProperty("region")));
        table.add(Arrays.asList("secretKey", account.getProperty("secretKey")));
        table.add(Arrays.asList("connectionId", S3BucketNameBuilder.getBucketName(s3Bucket)));
        table.add(Arrays.asList("name", "Fuse QE S3 " + S3BucketNameBuilder.getBucketName(s3Bucket)));
        createConnection(DataTable.create(table));
    }
}

