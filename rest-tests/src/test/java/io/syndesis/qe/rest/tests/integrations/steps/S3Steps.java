package io.syndesis.qe.rest.tests.integrations.steps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cucumber.api.java.en.Given;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.qe.bdd.AbstractStep;
import io.syndesis.qe.bdd.entities.StepDefinition;
import io.syndesis.qe.bdd.storage.StepsStorage;
import io.syndesis.qe.endpoints.ConnectionsEndpoint;
import io.syndesis.qe.endpoints.ConnectorsEndpoint;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 2, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class S3Steps extends AbstractStep {

    @Autowired
    private StepsStorage steps;
    @Autowired
    private ConnectionsEndpoint connectionsEndpoint;
    @Autowired
    private ConnectorsEndpoint connectorsEndpoint;

    @Given("^create S3 polling START action step with bucket: \"([^\"]*)\"$")
    public void createS3PollingStep(String bucketName) {
        createS3PollingStep(bucketName, null);
    }

    @Given("^create S3 polling START action step with bucket: \"([^\"]*)\" and prefix \"([^\"]*)\"$")
    public void createS3PollingStep(String bucketName, String prefix) {
        final Connector s3Connector = connectorsEndpoint.get(RestTestsUtils.Connector.S3.getId());
        final Connection s3Connection = connectionsEndpoint.get(S3BucketNameBuilder.getBucketName(bucketName));
        final Action s3PollingAction = TestUtils.findConnectorAction(s3Connector, "aws-s3-polling-bucket-connector");
        final Map<String, String> properties = TestUtils.map(TestUtils.map("deleteAfterRead", "false",
                "maxMessagesPerPoll", "10",
                "delay", "1000"));
        if (prefix != null) {
            properties.put("prefix", prefix);
        }
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(s3PollingAction, properties, S3BucketNameBuilder.getBucketName(bucketName));

        final Step s3Step = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(s3Connection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(s3PollingAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(s3Step));
    }

    @Given("^create S3 copy FINISH action step with bucket: \"([^\"]*)\"$")
    public void createS3CopyStep(String bucketName) {
        createS3CopyStepFile(bucketName, null);
    }

    @Given("^create S3 copy FINISH action step with bucket: \"([^\"]*)\" and filename: \"([^\"]*)\"$")
    public void createS3CopyStepFile(String bucketName, String fileName) {
        final Connector s3Connector = connectorsEndpoint.get(RestTestsUtils.Connector.S3.getId());
        final Connection s3Connection = connectionsEndpoint.get(S3BucketNameBuilder.getBucketName(bucketName));

        Map<String, String> properties;
        if(fileName != null){
            properties = TestUtils.map("fileName", fileName);
        } else {
            properties = new HashMap<>();
        }

        final Action s3CopyAction = TestUtils.findConnectorAction(s3Connector, "aws-s3-copy-object-connector");
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(s3CopyAction, properties, S3BucketNameBuilder.getBucketName(bucketName));

        final Step s3Step = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(s3Connection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(s3CopyAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(s3Step));
    }

    @Given("^create S3 delete FINISH action step with bucket: \"([^\"]*)\"$")
    public void createS3DeleteStep(String bucketName) {
        createS3DeleteStepFile(bucketName, null);
    }

    @Given("^create S3 delete FINISH action step with bucket: \"([^\"]*)\" and filename: \"([^\"]*)\"$")
    public void createS3DeleteStepFile(String bucketName, String fileName) {
        final Connector s3Connector = connectorsEndpoint.get(RestTestsUtils.Connector.S3.getId());
        final Connection s3Connection = connectionsEndpoint.get(S3BucketNameBuilder.getBucketName(bucketName));

        Map<String, String> properties;
        if(fileName != null){
            properties = TestUtils.map("fileName", fileName);
        } else {
            properties = new HashMap<>();
        }

        final Action s3DeleteAction = TestUtils.findConnectorAction(s3Connector, "aws-s3-delete-object-connector");
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(s3DeleteAction, properties, S3BucketNameBuilder.getBucketName(bucketName));

        final Step s3Step = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(s3Connection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(s3DeleteAction, connectorDescriptor))
                .configuredProperties(properties)
                .build();

        steps.getStepDefinitions().add(new StepDefinition(s3Step));
    }
}
