package io.syndesis.qe.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * S3 bucket name has to contain some random string at the end - it prevents test failures, for instance for cases, when
 * someone creates bucket with name, which we would like to use in our tests (the names of buckets on S3 have to be
 * unique).
 *
 * Jan 22, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public final class S3BucketNameBuilder {

	private static Optional<String> randomSalt = Optional.empty();

	private S3BucketNameBuilder() {
	}

	private static Optional<String> getRandomSalt() {
		if (randomSalt.isPresent()) {
		} else {
			randomSalt = Optional.of(RandomStringUtils.randomAlphanumeric(8).toLowerCase());
		}
		log.debug(randomSalt.get());
		return randomSalt;
	}

	public static String getBucketName(String bucketName) {
		final StringBuilder resp = new StringBuilder(bucketName);
		resp.append("-");
		resp.append(getRandomSalt().get());

		log.debug(resp.toString());
		return resp.toString();
	}
}
