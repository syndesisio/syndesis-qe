package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.utils.S3BucketNameBuilder;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.utils.S3Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * This validation steps can be used to create/delete and content validation of S3 steps. There is a specific issue with
 * S3 buckets - the name they use has to be unique, so the names specified by the scenario will be extended with random
 * string, to enhance the possibility, that the name we want to use is not already taken by some other S3 bucket.
 *
 * Jan 3, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class S3ValidationSteps {

	private final S3Utils s3Utils;

	public S3ValidationSteps() {
		s3Utils = new S3Utils();
	}

	@Then("^clean S3 to S3 scenario, removes two sample buckets with names: \"([^\"]*)\"")
	public void cleanupS3(List<String> bucketNames) {
		for (String bucket : bucketNames) {
			s3Utils.deleteS3Bucket(S3BucketNameBuilder.getBucketName(bucket));
		}
		TestSupport.getInstance().resetDB();
	}

	@Given("^create sample buckets on S3 with name \"([^\"]*)\"")
	public void createSampleBucket(String bucketName) {
		s3Utils.forceCreateS3Bucket(S3BucketNameBuilder.getBucketName(bucketName));
	}

	@Then("^create a new text file in bucket \"([^\"]*)\" with name \"([^\"]*)\" and text \"([^\"]*)\"")
	public void createFileInBucket(String bucketName, String fileName, String text) {
		s3Utils.createTextFile(S3BucketNameBuilder.getBucketName(bucketName), fileName, text);
	}

	@Then("^validate bucket with name \"([^\"]*)\" contains file with name \"([^\"]*)\" and text \"([^\"]*)\"")
	public void validateIntegration(String bucketName, String fileName, String text) {

		try {
			//TODO(tplevko): make waiting more dynamic
			Thread.sleep(20000);
		} catch (InterruptedException ex) {
			log.error("Error: " + ex);
		}
		Assertions.assertThat(s3Utils.checkFileExistsInBucket(S3BucketNameBuilder.getBucketName(bucketName), fileName)).isEqualTo(true);
		Assertions.assertThat(s3Utils.readTextFileContentFromBucket(S3BucketNameBuilder.getBucketName(bucketName), fileName)).contains(text);
	}
}
