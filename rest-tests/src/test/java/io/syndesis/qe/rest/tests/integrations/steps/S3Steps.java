package io.syndesis.qe.rest.tests.integrations.steps;

import java.util.HashMap;
import java.util.Map;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.syndesis.qe.rest.tests.util.RestTestsUtils;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.utils.TestUtils;

/**
 * Jan 2, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
public class S3Steps extends AbstractStep {
    @When("^create S3 polling START action step with bucket: \"([^\"]*)\"$")
    public void createS3PollingStep(String bucketName) {
        createS3PollingStep(bucketName, null);
    }

    @Given("^create S3 polling START action step with bucket: \"([^\"]*)\" and prefix \"([^\"]*)\"$")
    public void createS3PollingStep(String bucketName, String prefix) {
        super.addProperty(StepProperty.CONNECTOR_ID, RestTestsUtils.Connector.S3.getId());
        super.addProperty(StepProperty.CONNECTION_ID, S3BucketNameBuilder.getBucketName(bucketName));
        super.addProperty(StepProperty.ACTION, "aws-s3-polling-bucket-connector");
        final Map<String, String> properties = TestUtils.map(TestUtils.map("deleteAfterRead", "false",
                "maxMessagesPerPoll", "10",
                "delay", "1000"));
        if (prefix != null) {
            properties.put("prefix", prefix);
        }
        super.addProperty(StepProperty.PROPERTIES, properties);
        super.createStep();
    }

    @Given("^create S3 \"([^\"]*)\" FINISH action step with bucket: \"([^\"]*)\"$")
    public void createS3CopyStep(String action, String bucketName) {
        createS3CopyStepFile(action, bucketName, null);
    }

    @Given("^create S3 \"([^\"]*)\" FINISH action step with bucket: \"([^\"]*)\" and filename: \"([^\"]*)\"$")
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
}
