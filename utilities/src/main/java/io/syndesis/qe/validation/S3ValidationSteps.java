package io.syndesis.qe.validation;

import org.assertj.core.api.Assertions;

import java.util.List;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.utils.S3Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * Jan 3, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class S3ValidationSteps {

	private final S3Utils s3Utils;
	private final AccountsDirectory accountsDirectory;

	public S3ValidationSteps() {
		accountsDirectory = AccountsDirectory.getInstance();
		s3Utils = new S3Utils();
	}

	@Given("^clean S3 to S3 scenario, removes two sample buckets with names: \"([^\"]*)\"")
	public void cleanupTwSf(List<String> bucketNames) {
		for (String bucket : bucketNames) {
			s3Utils.deleteS3Bucket(bucket);
		}
		TestSupport.getInstance().resetDB();
	}

	@Given("^create sample buckets on S3 with name \"([^\"]*)\"")
	public void createSampleBucket(String bucketName) {
		s3Utils.forceCreateS3Bucket(bucketName);
	}

	@Then("^create a new text file in bucket \"([^\"]*)\" with name \"([^\"]*)\" and text \"([^\"]*)\"")
	public void createFileInBucket(String bucketName, String fileName, String text) {
		s3Utils.createTextFile(bucketName, fileName, text);
	}

	@Then("^validate bucket with name \"([^\"]*)\" contains file with name \"([^\"]*)\" and text \"([^\"]*)\"")
	public void validateIntegration(String bucketName, String fileName, String text) {

		try {
			//TODO(tplevko): make waiting more dynamic
			Thread.sleep(20000);
		} catch (InterruptedException ex) {
			log.error("Error: " + ex);
		}
		Assertions.assertThat(s3Utils.checkFileExistsInBucket(bucketName, fileName)).isEqualTo(true);
		Assertions.assertThat(s3Utils.readTextFileContentFromBucket(bucketName, fileName)).contains(text);
	}
}
