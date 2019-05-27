package io.syndesis.qe.rest.tests.steps.connection;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;

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
    private Account account;

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
        account = accountsDirectory.getAccount(Account.Name.ACTIVEMQ).get();
        createConnection(
            fromData(
                keyValue("connector", "activemq"),
                accountProperty("brokerUrl"),
                accountProperty("username"),
                accountProperty("password")
            )
        );
    }

    @Given("^create Box connection$")
    public void createBoxConnection() {
        account = accountsDirectory.getAccount(Account.Name.BOX).get();
        createConnection(
            fromData(
                keyValue("connector", "box"),
                accountProperty("userName"),
                accountProperty("userPassword"),
                accountProperty("clientId"),
                accountProperty("clientSecret")
            )
        );
    }

    @Given("^create Dropbox connection$")
    public void createDropboxConnection() {
        account = accountsDirectory.getAccount(Account.Name.DROPBOX).get();
        createConnection(
            fromData(
                keyValue("connector", "dropbox"),
                accountProperty("accessToken"),
                accountProperty("clientIdentifier")
            )
        );
    }

    @Given("^create FTP connection$")
    public void createFTPConnection() {
        account = accountsDirectory.getAccount(Account.Name.FTP).get();
        createConnection(
            fromData(
                keyValue("connector", "ftp"),
                accountProperty("host"),
                accountProperty("port")
            )
        );
    }

    @Given("^create HTTP connection$")
    public void createHTTPConnection() {
        account = accountsDirectory.getAccount(Account.Name.HTTP).get();
        createConnection(
            fromData(
                keyValue("connector", "http"),
                accountProperty("baseUrl")
            )
        );
    }

    @Given("^create IRC connection$")
    public void createIRCConnection() {
        account = accountsDirectory.getAccount(Account.Name.IRC).get();
        createConnection(
            fromData(
                keyValue("connector", "irc"),
                accountProperty("hostname"),
                accountProperty("port")
            )
        );
    }

    @Given("^create Kafka connection$")
    public void createKafkaConnection() {
        account = accountsDirectory.getAccount(Account.Name.KAFKA).get();
        createConnection(
            fromData(
                keyValue("connector", "kafka"),
                accountProperty("brokers")
            )
        );
    }

    @Given("^create SalesForce connection$")
    public void createSalesForceConnection() {
        account = accountsDirectory.getAccount(Account.Name.SALESFORCE).get();
        createConnection(
            fromData(
                keyValue("connector", "salesforce"),
                accountProperty("clientId"),
                accountProperty("clientSecret"),
                accountProperty("loginUrl"),
                accountProperty("userName"),
                accountProperty("password")
            )

        );
    }

    @Given("^create SNS connection$")
    public void createSNSConnection() {
        account = accountsDirectory.getAccount(Account.Name.AWS).get();
        createConnection(
            fromData(
                keyValue("connector", "sns"),
                accountProperty("accessKey"),
                accountProperty("secretKey"),
                accountProperty("region")
            )
        );
    }

    @Given("^create SQS connection$")
    public void createSQSConnection() {
        account = accountsDirectory.getAccount(Account.Name.AWS).get();
        createConnection(
            fromData(
                keyValue("connector", "sqs"),
                accountProperty("accessKey"),
                accountProperty("secretKey"),
                accountProperty("region")
            )
        );
    }

    @Given("^create S3 connection using \"([^\"]*)\" bucket$")
    public void createS3Connection(String s3Bucket) {
        account = accountsDirectory.getAccount(Account.Name.AWS).get();
        log.info("Bucket name: {}", S3BucketNameBuilder.getBucketName(s3Bucket));
        createConnection(
            fromData(
                keyValue("connector", "s3"),
                accountProperty("accessKey"),
                keyValue("bucketNameOrArn", S3BucketNameBuilder.getBucketName(s3Bucket)),
                accountProperty("region"),
                accountProperty("secretKey"),
                keyValue("connectionId", S3BucketNameBuilder.getBucketName(s3Bucket)),
                keyValue("name", "Fuse QE S3 " + S3BucketNameBuilder.getBucketName(s3Bucket))
            )
        );
    }

    @Given("^create Twitter connection using \"([^\"]*)\" account$")
    public void createTwitterConnection(String twitterAccount) {
        account = accountsDirectory.getAccount(twitterAccount).get();
        createConnection(
            fromData(
                keyValue("connector", "twitter"),
                accountProperty("accessToken"),
                accountProperty("accessTokenSecret"),
                accountProperty("consumerKey"),
                accountProperty("consumerSecret")
            )
        );
    }

    @SafeVarargs
    private final DataTable fromData(List<String>... elements) {
        return DataTable.create(new ArrayList<>(Arrays.asList(elements)));
    }

    private List<String> keyValue(String key, String value) {
        return Arrays.asList(key, value);
    }

    private List<String> accountProperty(String prop) {
        return Arrays.asList(prop, account.getProperty(prop));
    }
}

