package io.syndesis.qe.utils;

import org.apache.commons.io.IOUtils;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import javax.annotation.PreDestroy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import lombok.extern.slf4j.Slf4j;

/**
 * Aws S3 utils.
 *
 * Jan 3, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
@Lazy
@Component
public class S3Utils {

    private final AccountsDirectory accountsDirectory;
    private final AmazonS3 s3client;
    /**
     *   holder for buckets created by this instance
     */
    private Map<String, Bucket> bucketsCreated;

    public S3Utils() {
        accountsDirectory = AccountsDirectory.getInstance();
        final Account s3Account = accountsDirectory.getAccount(Account.Name.AWS).get();
        final AWSCredentials credentials = new BasicAWSCredentials(
                s3Account.getProperty("accessKey"), s3Account.getProperty("secretKey")
        );

        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_WEST_1)
                .build();
        bucketsCreated = new HashMap<>();
    }

    public void forceCreateS3Bucket(String bucketName) {

        createS3Bucket(bucketName, true);
    }

    public void createS3Bucket(String bucketName, boolean force) {

        if (doesBucketExist(bucketName)) {
            if (force) {
                deleteS3Bucket(bucketName);
            } else {
                log.error("Bucket name is not available."
                        + " Try again with a different Bucket name.");
                return;
            }
        }
        bucketsCreated.put(bucketName, s3client.createBucket(bucketName));
    }

    /**
     * Method that checks if bucket exists in S3 instance.
     * @param bucketName name of the bucket
     * @return true if exists, false otherwise
     */
    public boolean doesBucketExist(String bucketName) {
        return s3client.doesBucketExistV2(bucketName);
    }

    public void deleteS3Bucket(String bucketName) {
        try {
            final ObjectListing bucketObjects = s3client.listObjects(bucketName);
            for (Iterator<?> iterator = bucketObjects.getObjectSummaries().iterator(); iterator.hasNext();) {
                final S3ObjectSummary summary = (S3ObjectSummary) iterator.next();
                s3client.deleteObject(bucketName, summary.getKey());
            }
            s3client.deleteBucket(bucketName);
            bucketsCreated.remove(bucketName);
        } catch (AmazonServiceException e) {
            log.error("Could not delete the S3 bucket: {}", e.getErrorMessage());
        }
    }

    /**
     * Creates a text file in the specified S3 bucket.
     *
     * @param bucketName
     * @param fileName
     * @param text
     */
    public void createTextFile(String bucketName, String fileName, String text) {
        try {
            final File temp = File.createTempFile(fileName, "");
            temp.deleteOnExit();
            final BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(text);
            bw.close();
            s3client.putObject(bucketName, fileName, temp);
        } catch (IOException ex) {
            log.error("Error with tmp file: " + ex);
        }
    }

    /**
     * Checks if the specified text file exists in specified S3 bucket.
     *
     * @param bucketName
     * @param fileName
     * @return
     */
    public boolean checkFileExistsInBucket(String bucketName, String fileName) {

        boolean fileExists = false;
        final ObjectListing objectListing = s3client.listObjects(bucketName);
        for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
            log.debug(os.getKey());
            if (os.getKey().matches(fileName)) {
                fileExists = true;
                break;
            }
        }
        return fileExists;
    }

    /**
     * Gets specified text file content from specified S3 bucket.
     *
     * @param bucketName
     * @param fileName
     * @return
     */
    public String readTextFileContentFromBucket(String bucketName, String fileName) {

        final S3Object s3object = s3client.getObject(bucketName, fileName);
        final S3ObjectInputStream inputStream = s3object.getObjectContent();

        final StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputStream, writer, "UTF-8");
        } catch (IOException ex) {
            log.error("Error copying file from s3: " + ex);
        }
        return writer.toString();
    }

    /**
     * Method to delete all the buckets created by this instance of S3Utils.
     */
    @PreDestroy
    public void deleteAllBuckets() {
        Set<String> bucketNames = new HashSet<>(bucketsCreated.keySet());
        for (String bucketName:bucketNames) {
            deleteS3Bucket(bucketName);
        }
    }
}
