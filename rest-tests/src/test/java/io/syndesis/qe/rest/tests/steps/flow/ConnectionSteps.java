package io.syndesis.qe.rest.tests.steps.flow;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.AccountUtils;
import io.syndesis.qe.utils.BoxUtils;
import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.utils.SNSUtils;
import io.syndesis.qe.utils.SQSUtils;
import io.syndesis.qe.utils.TestUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

/**
 * Class holding the steps for creating the flow steps related to connections.
 */
public class ConnectionSteps extends AbstractStep {
    @When("add \"([^\"]*)\" endpoint with connector id \"([^\"]*)\" and \"([^\"]*)\" action and with properties:$")
    public void createConnectorStep(String id, String connectorId, String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, connectorId);
        super.addProperty(StepProperty.CONNECTION_ID, id);
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMaps(String.class, String.class).get(0));
        super.createStep();
    }

    @When("^create ActiveMQ \"([^\"]*)\" action step with destination type \"([^\"]*)\" and destination name \"([^\"]*)\"$")
    public void createAmqStep(String action, String destinationType, String destinationName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.ACTIVEMQ.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.ACTIVEMQ.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
            "destinationType", destinationType.toLowerCase().equals("queue") ? "queue" : "topic",
            "destinationName", destinationName
        ));
        super.createStep();
    }

    @When("^create AMQP \"([^\"]*)\" action step with properties:$")
    public void createAmqpStep(String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.AMQP.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.AMQP.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create Box download action step (with|without) fileId$")
    public void createBoxDownload(String withFileId) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.BOX.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.BOX.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:box-download");
        Map<String, String> properties = new HashMap<>();
        if ("with".equals(withFileId)) {
            properties.put("fileId", BoxUtils.getFileIds().get(0));
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @When("create Box upload action step with file name {string}")
    public void createBoxUpload(String fileName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.BOX.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.BOX.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:box-upload");
        Map<String, String> properties = TestUtils.map("parentFolderId", AccountUtils.get(Account.Name.BOX).getProperty("folderId"));
        if (!fileName.isEmpty()) {
            properties.put("fileName", fileName);
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @When("^create start DB periodic sql invocation action step with query \"([^\"]*)\" and period \"([^\"]*)\" ms$")
    public void createStartDbPeriodicSqlStep(String sqlQuery, Integer ms) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-start-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("query", sqlQuery, "schedulerExpression", ms));
        super.createStep();
    }

    @When("^create start DB periodic stored procedure invocation action step named \"([^\"]*)\" and period \"([^\"]*)\" ms$")
    public void createStartDbPeriodicProcedureStep(String procedureName, Integer ms) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-stored-start-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
            "procedureName", procedureName,
            "schedulerExpression", ms,
            "template", DbUtils.getStoredProcedureTemplate(RestTestsUtils.Connection.DB.getId(), procedureName, true))
        );
        super.createStep();
    }

    @When("^create finish DB invoke sql action step with query \"([^\"]*)\"$")
    public void createFinishDbInvokeSqlStep(String sqlQuery) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
            "query", sqlQuery,
            "batch", true
        ));
        super.createStep();
    }

    @When("^create finish DB invoke stored procedure \"([^\"]*)\" action step$")
    public void createFinishDbInvokeProcedureStep(String procedureName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DB.getId());
        super.addProperty(StepProperty.ACTION, "sql-stored-connector");
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
            "procedureName", procedureName,
            "template", DbUtils.getStoredProcedureTemplate(RestTestsUtils.Connection.DB.getId(), procedureName, false),
            "batch", true
        ));
        super.createStep();
    }

    @When("^create Dropbox \"([^\"]*)\" action step with file path: \"([^\"]*)\"$")
    public void createDropboxStep(String mode, String filePath) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DROPBOX.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DROPBOX.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:dropbox-" + mode);
        final Map<String, String> properties = TestUtils.map("remotePath", filePath);
        if ("upload".equals(mode.toLowerCase())) {
            properties.put("uploadMode", "add");
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @When("^create DynamoDB \"([^\"]*)\" action step with properties:$")
    public void createDynamoDBStep(String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.DYNAMO_DB.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.DYNAMO_DB.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:aws-ddb-" + action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create Email \"([^\"]*)\" action with properties:$")
    public void createEmailStep(String action, DataTable properties) {
        if (action.contains("send")) {
            super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.EMAIL_SEND.getId());
            super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.EMAIL_SEND.getId());
        } else {
            super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.EMAIL_RECEIVE.getId());
            super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.EMAIL_RECEIVE.getId());
        }
        super.addProperty(StepProperty.ACTION, "io.syndesis:email-" + action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create FHIR \"([^\"]*)\" action with resource type \"([^\"]*)\"")
    public void createFhirStep(String action, String type) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.FHIR.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.FHIR.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:fhir-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("resourceType", type));
        super.createStep();
    }

    @When("^create FTP \"([^\"]*)\" action with values$")
    public void createFtpStep(String action, DataTable sourceMappingData) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.FTP.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.FTP.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:ftp-" + action);
        super.addProperty(StepProperty.PROPERTIES, sourceMappingData.asMaps(String.class, String.class).get(0));
        super.createStep();
    }

    private void createHttpStep(String protocol, String method, String path, long period, String timeunit) {
        if ("HTTP".equals(protocol)) {
            super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.HTTP.getId());
            super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.HTTP.getId());
            super.addProperty(StepProperty.ACTION, period == -1 ? "http4-invoke-url" : "http4-periodic-invoke-url");
        } else {
            super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.HTTPS.getId());
            super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.HTTPS.getId());
            super.addProperty(StepProperty.ACTION, period == -1 ? "https4-invoke-url" : "https4-periodic-invoke-url");
        }
        final Map<String, String> properties = TestUtils.map(
            "path", path,
            "httpMethod", method
        );

        if (period != -1) {
            properties.put("schedulerExpression", TimeUnit.MILLISECONDS.convert(period, TimeUnit.valueOf(timeunit.toUpperCase())) + "");
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @When("^create (HTTP|HTTPS) \"([^\"]*)\" step with period \"([^\"]*)\" \"([^\"]*)\"$")
    public void createHTTPStepWithPeriod(String protocol, String method, long period, String timeunit) {
        createHttpStep(protocol, method, "/", period, timeunit);
    }

    @When("^create (HTTP|HTTPS) \"([^\"]*)\" step with path \"([^\"]*)\" and period \"([^\"]*)\" \"([^\"]*)\"$")
    public void createHTTPStepWithPeriodAndPath(String protocol, String method, String path, long period, String timeunit) {
        createHttpStep(protocol, method, path, period, timeunit);
    }

    @When("^create (HTTP|HTTPS) \"([^\"]*)\" step$")
    public void createBasicHTTPStep(String protocol, String method) {
        createHttpStep(protocol, method, "/", -1, null);
    }

    @When("^create IRC \"([^\"]*)\" step with nickname \"([^\"]*)\" and channels \"([^\"]*)\"$")
    public void createIrcStep(String action, String nickname, String channels) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.IRC.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.IRC.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map(
            "nickname", nickname,
            "channels", channels
        ));
        super.createStep();
    }

    @When("^create JIRA \"([^\"]*)\" step with JQL \'([^\']*)\'$")
    public void createJiraStep(String action, String jql) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.JIRA.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.JIRA.getId());
        super.addProperty(StepProperty.ACTION, "jira-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("jql", jql));
        super.createStep();
    }

    @When("^create Kafka \"([^\"]*)\" step with topic \"([^\"]*)\"$")
    public void createKafkaStep(String action, String topic) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.KAFKA.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.KAFKA.getId());
        super.addProperty(StepProperty.ACTION, "kafka-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("topic", topic));
        super.createStep();
    }

    @When("^create Kudu \"([^\"]*)\" step with table \"([^\"]*)\"$")
    public void createKuduStep(String action, String table) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.KUDU.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.KUDU.getId());
        super.addProperty(StepProperty.ACTION, "kudu-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("table", table));
        super.createStep();
    }

    @When("^create MongoDB \"([^\"]*)\" with collection \"([^\"]*)\"$")
    public void createMongoDBStep(String action, String collection) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.MONGODB36.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.MONGODB36.getId());
        super.addProperty(StepProperty.ACTION, "mongodb-" + action);
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("collection", collection));
        super.createStep();
    }

    @When("^create OData \"([^\"]*)\" action with properties$")
    public void createODataStep(String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.ODATA.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.ODATA.getId());
        super.addProperty(StepProperty.ACTION, "odata-" + action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create SF \"([^\"]*)\" action step with properties$")
    public void createSfStepWithActionAndProperties(String action, DataTable props) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SALESFORCE.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SALESFORCE.getId());
        super.addProperty(StepProperty.ACTION, action);
        super.addProperty(StepProperty.PROPERTIES, props.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create ServiceNow \"([^\"]*)\" action with properties$")
    public void createServiceNowStep(String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SERVICENOW.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SERVICENOW.getId());
        super.addProperty(StepProperty.ACTION, "servicenow-action-" + action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create SFTP \"([^\"]*)\" action with values$")
    public void createSftpStep(String action, DataTable sourceMappingData) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SFTP.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SFTP.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:sftp-" + action);
        super.addProperty(StepProperty.PROPERTIES, sourceMappingData.asMaps(String.class, String.class).get(0));
        super.createStep();
    }

    @When("^create Slack \"([^\"]*)\" action with properties$")
    public void createSlackStep(String action, DataTable properties) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SLACK.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SLACK.getId());
        super.addProperty(StepProperty.ACTION, "io.syndesis:slack-" + action);
        super.addProperty(StepProperty.PROPERTIES, properties.asMap(String.class, String.class));
        super.createStep();
    }

    @When("^create SNS publish action step with topic \"([^\"]*)\"$")
    public void createSNSStepWithProperties(String topic) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SNS.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SNS.getId());
        super.addProperty(StepProperty.ACTION, "send-object");
        final String topicNameOrArn = topic.contains("arn:") ? SNSUtils.getTopicArn(StringUtils.substringAfter(topic, "arn:")) : topic;
        super.addProperty(StepProperty.PROPERTIES, TestUtils.map("topicNameOrArn", topicNameOrArn));
        super.createStep();
    }

    @When("^create SQS \"([^\"]*)\" action step with properties$")
    public void createSQSStepWithProperties(String action, DataTable props) {
        // Send-Batch needs a special handling related to datamapping, see AtlasMapperGenerator#generateMapMapping
        if (action.contains("batch")) {
            System.setProperty("sqs.batch", "true");
        }
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.SQS.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.SQS.getId());
        super.addProperty(StepProperty.ACTION, action);
        Map<String, String> properties = new HashMap<>(props.asMap(String.class, String.class));
        if (properties.get("queueNameOrArn").startsWith("arn")) {
            properties.put("queueNameOrArn", SQSUtils.getQueueArn(StringUtils.substringAfter(properties.get("queueNameOrArn"), "arn:")));
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @When("^create S3 polling START action step with bucket: \"([^\"]*)\"$")
    public void createS3PollingStep(String bucketName) {
        createS3PollingStep(bucketName, null);
    }

    @When("^create S3 polling START action step with bucket: \"([^\"]*)\" and properties$")
    public void createS3PollingStepWithProperties(String bucketName, DataTable properties) {
        createS3PollingStep(bucketName, properties.asMap(String.class, String.class));
    }

    private void createS3PollingStep(String bucketName, Map<String, String> properties) {
        Map<String, String> defaultProperties = TestUtils.map(
            "deleteAfterRead", "false",
            "maxMessagesPerPoll", "10",
            "delay", "1000"
        );
        if (properties != null) {
            defaultProperties.putAll(properties);
        }
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.S3.getId());
        super.addProperty(StepProperty.CONNECTION_ID, S3BucketNameBuilder.getBucketName(bucketName));
        super.addProperty(StepProperty.ACTION, "aws-s3-polling-bucket-connector");
        super.addProperty(StepProperty.PROPERTIES, defaultProperties);
        super.createStep();
    }

    @When("^create S3 \"([^\"]*)\" FINISH action step with bucket: \"([^\"]*)\"$")
    public void createS3CopyStep(String action, String bucketName) {
        createS3CopyStepFile(action, bucketName, null);
    }

    @When("^create S3 \"([^\"]*)\" FINISH action step with bucket: \"([^\"]*)\" and filename: \"([^\"]*)\"$")
    public void createS3CopyStepFile(String action, String bucketName, String fileName) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.S3.getId());
        super.addProperty(StepProperty.CONNECTION_ID, S3BucketNameBuilder.getBucketName(bucketName));
        super.addProperty(StepProperty.ACTION, "aws-s3-" + action + "-object-connector");
        final Map<String, String> properties = new HashMap<>();
        if (fileName != null) {
            properties.put("fileName", fileName);
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @When("^create telegram (send|receive) action( with chat id \"([^\"]*)\")?$")
    public void createTelegramStep(String action, String chatId) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.TELEGRAM.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.TELEGRAM.getId());
        super.addProperty(StepProperty.ACTION, "telegram-chat-" + ("send".equals(action) ? "to" : "from"));
        if ("send".equals(action)) {
            super.addProperty(StepProperty.ACTION, "telegram-chat-to");
            super.addProperty(StepProperty.PROPERTIES, TestUtils.map("chatId", chatId));
        } else {
            super.addProperty(StepProperty.ACTION, "telegram-chat-from");
        }
        super.createStep();
    }

    @When("^create TW mention step with \"([^\"]*)\" action$")
    public void createTwitterStep(String twitterAction) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.TWITTER.getId());
        super.addProperty(StepProperty.CONNECTION_ID, RestTestsUtils.Connection.TWITTER.getId());
        super.addProperty(StepProperty.ACTION, twitterAction);
        super.createStep();
    }
}
