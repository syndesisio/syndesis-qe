package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.utils.S3Utils;
import io.syndesis.qe.utils.TestUtils;

import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;

/**
 * This validation steps can be used to create/delete and content validation of S3 steps. There is a specific issue with
 * S3 buckets - the name they use has to be unique, so the names specified by the scenario will be extended with random
 * string, to enhance the possibility, that the name we want to use is not already taken by some other S3 bucket.
 * <p>
 * Jan 3, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class S3ValidationSteps {

    private final S3Utils s3Utils;

    @Autowired
    @Lazy
    public S3ValidationSteps(S3Utils s3Utils) {
        this.s3Utils = s3Utils;
    }

    @Given("^create sample buckets? on S3 with name \"([^\"]*)\"")
    public void createSampleBucket(String bucketName) {
        s3Utils.forceCreateS3Bucket(S3BucketNameBuilder.getBucketName(bucketName));
    }

    @Then("^create a new text file in bucket \"([^\"]*)\" with name \"([^\"]*)\" and text \"([^\"]*)\"")
    public void createFileInBucket(String bucketName, String fileName, String text) {
        s3Utils.createTextFile(S3BucketNameBuilder.getBucketName(bucketName), fileName, text);
    }

    @Then("^validate bucket with name \"([^\"]*)\" contains file with name \"([^\"]*)\" and text \"([^\"]*)\"")
    public void validateIntegration(String bucketName, String fileName, String text) {
        assertThat(TestUtils.waitForEvent(r -> r, () -> s3Utils.checkFileExistsInBucket(S3BucketNameBuilder.getBucketName(bucketName), fileName),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isTrue();
        assertThat(s3Utils.readTextFileContentFromBucket(S3BucketNameBuilder.getBucketName(bucketName), fileName)).contains(text);
    }

    @Then("^validate bucket with name \"([^\"]*)\" does not contain file with name \"([^\"]*)\"")
    public void checkFileNotInBucket(String bucketName, String fileName) {
        assertThat(TestUtils.waitForEvent(r -> r, () -> s3Utils.checkFileExistsInBucket(S3BucketNameBuilder.getBucketName(bucketName), fileName),
            TimeUnit.MINUTES, 2, TimeUnit.SECONDS, 15)).isFalse();
    }

    @Then("^check that buckets do exist: \"([^\"]*)\"")
    public void checkBucketsDoExist(String bucketNames) {
        checkBucketsPresence(bucketNames, true);
    }

    @Then("^check that buckets do not exist: \"([^\"]*)\"")
    public void checkBucketsDontExist(String bucketNames) {
        checkBucketsPresence(bucketNames, false);
    }

    /**
     * Method that checks presence of given buckets in S3 instance.
     *
     * @param bucketNames name of the buckets to check
     * @param shouldExist expected state to be checked against, true - should exist, false - shouldn't exist
     */
    private void checkBucketsPresence(String bucketNames, boolean shouldExist) {
        SoftAssertions softly = new SoftAssertions();
        for (String bucket : bucketNames.split(",")) {
            String bucketName = S3BucketNameBuilder.getBucketName(bucket.trim());
            log.debug("Checking presence of bucket {}.", bucketName);
            softly
                .assertThat(s3Utils.doesBucketExist(bucketName))
                .as("Bucket " + bucketName + " should " + (shouldExist ? "" : "not ") + "exist.")
                .isEqualTo(shouldExist);
        }
        softly.assertAll();
    }
}
