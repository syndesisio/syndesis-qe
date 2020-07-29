package io.syndesis.qe.steps.connection;

import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.endpoint.ConnectionsEndpoint;
import io.syndesis.qe.endpoint.ConnectorsEndpoint;
import io.syndesis.qe.util.RestTestsUtils;
import io.syndesis.qe.utils.aws.S3BucketNameBuilder;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
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

    @Given("create connection")
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

        connectionPropertiesMap.remove("connector");
        connectionPropertiesMap.remove("connectionId");
        connectionPropertiesMap.remove("account");
        connectionPropertiesMap.remove("name");

        for (Map.Entry<String, String> keyValue : connectionPropertiesMap.entrySet()) {
            if ("$ACCOUNT$".equals(keyValue.getValue())) {
                keyValue.setValue(accountsDirectory.get(connectionPropertiesMap.get("account")).getProperty(keyValue.getKey()));
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

    @Given("create ActiveMQ connection")
    public void createActiveMQConnection() {
        account = accountsDirectory.get(Account.Name.ACTIVEMQ);
        createConnection(
            fromData(
                keyValue("connector", "activemq"),
                accountProperty("brokerUrl"),
                accountProperty("username"),
                accountProperty("password")
            )
        );
    }

    @Given("create AMQP connection")
    public void createAMQPConnection() {
        account = accountsDirectory.get(Account.Name.AMQP);
        createConnection(
            fromData(
                keyValue("connector", "amqp"),
                accountProperty("connectionUri"),
                accountProperty("username"),
                accountProperty("password"),
                accountProperty("clientID")
            )
        );
    }

    @Given("create Box connection")
    public void createBoxConnection() {
        account = accountsDirectory.get(Account.Name.BOX);
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

    @Given("create Dropbox connection")
    public void createDropboxConnection() {
        account = accountsDirectory.get(Account.Name.DROPBOX);
        createConnection(
            fromData(
                keyValue("connector", "dropbox"),
                accountProperty("accessToken"),
                accountProperty("clientIdentifier")
            )
        );
    }

    @Given("create DynamoDB connection")
    public void createDynamoDBConnection() {
        account = accountsDirectory.get(Account.Name.AWS);
        createConnection(
            fromData(
                keyValue("connector", "dynamo_db"),
                accountProperty("accessKey"),
                accountProperty("secretKey"),
                regionAccountProperty(),
                accountProperty("tableName")
            )
        );
    }

    @Given("^create Email (SMTP|IMAP|POP3) (SSL|STARTTLS) connection$")
    public void createEmailConnection(String type, String security) {
        account = accountsDirectory.get("Email " + type + " With " + security);
        createConnection(
            fromData(
                keyValue("connector", "smtp".equals(type.toLowerCase()) ? "email_send" : "email_receive"),
                accountProperty("username"),
                accountProperty("password"),
                accountProperty("host"),
                accountProperty("port")
            )
        );
    }

    @Given("create FHIR connection")
    public void createFhirConnection() {
        createConnection(
            fromData(
                keyValue("connector", "fhir")
            )
        );
    }

    @Given("create FTP connection")
    public void createFTPConnection() {
        account = accountsDirectory.get(Account.Name.FTP);
        createConnection(
            fromData(
                keyValue("connector", "ftp"),
                accountProperty("host"),
                accountProperty("port")
            )
        );
    }

    @Given("create HTTP connection")
    public void createHTTPConnection() {
        account = accountsDirectory.get(Account.Name.HTTP);
        createConnection(
            fromData(
                keyValue("connector", "http"),
                accountProperty("baseUrl")
            )
        );
    }

    @Given("create HTTPS connection")
    public void createHTTPSConnection() {
        account = accountsDirectory.get(Account.Name.HTTPS);
        createConnection(
            fromData(
                keyValue("connector", "https"),
                accountProperty("baseUrl")
            )
        );
    }

    @Given("create IRC connection")
    public void createIRCConnection() {
        account = accountsDirectory.get(Account.Name.IRC);
        createConnection(
            fromData(
                keyValue("connector", "irc"),
                accountProperty("hostname"),
                accountProperty("port")
            )
        );
    }

    @Given("create Jira connection")
    public void createJiraConnection() {
        createConnection(
            fromData(
                keyValue("connector", "jira"),
                keyValue("jiraUrl", "http://myjira.com")
            )
        );
    }

    @Given("create Kafka connection")
    public void createKafkaConnection() {
        account = accountsDirectory.get(Account.Name.KAFKA);
        createConnection(
            fromData(
                keyValue("connector", "kafka"),
                keyValue("transportProtocol", "PLAINTEXT"),
                accountProperty("brokers")
            )
        );
    }

    @Given("create Kudu connection")
    public void createKuduConnection() {
        account = accountsDirectory.get(Account.Name.KUDU);
        createConnection(
            fromData(
                keyValue("connector", "kudu"),
                accountProperty("host")
            )
        );
    }

    @Given("create MongoDB connection")
    public void createMongoDBConnection() {
        account = accountsDirectory.get(Account.Name.MONGODB36);
        createConnection(
            fromData(
                keyValue("connector", "mongodb36"),
                accountProperty("host"),
                accountProperty("user"),
                accountProperty("password"),
                accountProperty("database"),
                accountProperty("url")
            )
        );
    }

    @Given("^create OData (HTTP|HTTPS) connection$")
    public void createODataConnection(String type) {
        account = accountsDirectory.get("http".equals(type.toLowerCase()) ? Account.Name.ODATA_HTTP : Account.Name.ODATA_HTTPS);
        createConnection(
            fromData(
                keyValue("connector", "odata"),
                accountProperty("serviceUri")
            )
        );
    }

    @Given("create SalesForce connection")
    public void createSalesForceConnection() {
        account = accountsDirectory.get(Account.Name.SALESFORCE);
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

    @Given("create ServiceNow connection")
    public void createServicenowConnection() {
        account = accountsDirectory.get(Account.Name.SERVICENOW);
        createConnection(
            fromData(
                keyValue("connector", "servicenow"),
                accountProperty("instanceName"),
                accountProperty("userName"),
                accountProperty("password")
            )
        );
    }

    @Given("create SFTP connection")
    public void createSFTPConnection() {
        account = accountsDirectory.get(Account.Name.SFTP);
        createConnection(
            fromData(
                keyValue("connector", "sftp"),
                accountProperty("host"),
                accountProperty("port"),
                accountProperty("username"),
                accountProperty("password")
            )
        );
    }

    @Given("create Slack connection")
    public void createSlackConnection() {
        account = accountsDirectory.get(Account.Name.SLACK);
        createConnection(
            fromData(
                keyValue("connector", "slack"),
                accountProperty("webhookUrl"),
                accountProperty("token")
            )
        );
    }

    @Given("create SNS connection")
    public void createSNSConnection() {
        account = accountsDirectory.get(Account.Name.AWS);
        createConnection(
            fromData(
                keyValue("connector", "sns"),
                accountProperty("accessKey"),
                accountProperty("secretKey"),
                regionAccountProperty()
            )
        );
    }

    @Given("create SQS connection")
    public void createSQSConnection() {
        account = accountsDirectory.get(Account.Name.AWS);
        createConnection(
            fromData(
                keyValue("connector", "sqs"),
                accountProperty("accessKey"),
                accountProperty("secretKey"),
                regionAccountProperty()
            )
        );
    }

    @Given("create S3 connection using {string} bucket")
    public void createS3Connection(String s3Bucket) {
        account = accountsDirectory.get(Account.Name.AWS);
        log.info("Bucket name: {}", S3BucketNameBuilder.getBucketName(s3Bucket));
        createConnection(
            fromData(
                keyValue("connector", "s3"),
                accountProperty("accessKey"),
                keyValue("bucketNameOrArn", S3BucketNameBuilder.getBucketName(s3Bucket)),
                regionAccountProperty(),
                accountProperty("secretKey"),
                keyValue("connectionId", S3BucketNameBuilder.getBucketName(s3Bucket)),
                keyValue("name", "Fuse QE S3 " + S3BucketNameBuilder.getBucketName(s3Bucket))
            )
        );
    }

    @Given("create Telegram connection")
    public void createTelegramConnection() {
        account = accountsDirectory.get(Account.Name.TELEGRAM);
        createConnection(
            fromData(
                keyValue("connector", "telegram"),
                accountProperty("authorizationToken")
            )
        );
    }

    @Given("create Twitter connection using {string} account")
    public void createTwitterConnection(String twitterAccount) {
        account = accountsDirectory.get(twitterAccount);
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

    private List<String> regionAccountProperty() {
        return Arrays.asList("region", account.getProperty("region").toUpperCase().replaceAll("-", "_"));
    }
}

