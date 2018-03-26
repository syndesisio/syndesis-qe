package io.syndesis.qe.rest.tests.integrations;

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

    public S3Steps() {
    }

    @Given("^create S3 polling step with bucket: \"([^\"]*)\"")
    public void createS3PollingStep(String bucketName) {
        final Connector s3Connector = connectorsEndpoint.get("aws-s3");
        final Connection s3Connection = connectionsEndpoint.get(S3BucketNameBuilder.getBucketName(bucketName));
        final Action s3PollingAction = TestUtils.findConnectorAction(s3Connector, "aws-s3-polling-bucket-connector");
        final Map<String, String> properties = TestUtils.map(TestUtils.map("deleteAfterRead", "false",
                "maxMessagesPerPoll", "10",
                "delay", "1000"));
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

    @Given("^create S3 copy step with bucket: \"([^\"]*)\"")
    public void createS3CopyStep(String bucketName) {
        final Connector s3Connector = connectorsEndpoint.get("aws-s3");
        final Connection s3Connection = connectionsEndpoint.get(S3BucketNameBuilder.getBucketName(bucketName));
        final Action s3PollingAction = TestUtils.findConnectorAction(s3Connector, "aws-s3-copy-object-connector");
        final ConnectorDescriptor connectorDescriptor = getConnectorDescriptor(s3PollingAction, new HashMap(), S3BucketNameBuilder.getBucketName(bucketName));

        final Step s3Step = new Step.Builder()
                .stepKind(StepKind.endpoint)
                .connection(s3Connection)
                .id(UUID.randomUUID().toString())
                .action(generateStepAction(s3PollingAction, connectorDescriptor))
                .build();

        steps.getStepDefinitions().add(new StepDefinition(s3Step));
    }
}
