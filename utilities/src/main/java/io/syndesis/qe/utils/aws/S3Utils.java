package io.syndesis.qe.utils.aws;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

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
import com.amazonaws.services.s3.model.S3ObjectSummary;

import javax.annotation.PreDestroy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
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
    // holder for buckets created by this instance
    @Getter
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
            .withRegion(Regions.valueOf(s3Account.getProperty("region").toUpperCase().replaceAll("-", "_")))
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
        final StringWriter writer = new StringWriter();
        try (InputStream is = getObjectInputStream(bucketName, fileName)) {
            IOUtils.copy(is, writer, "UTF-8");
        } catch (IOException ex) {
            log.error("Error copying file from s3: " + ex);
        }
        return writer.toString();
    }

    public void downloadFile(String bucketName, String fileName, Path localFile) {
        try (InputStream is = getObjectInputStream(bucketName, fileName)) {
            Files.copy(is, localFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            fail("Error copying file from s3: " + ex);
        }
    }

    private InputStream getObjectInputStream(String bucketName, String fileName) {
        return s3client.getObject(bucketName, fileName).getObjectContent();
    }

    public String getFileNameWithPrefix(String bucketName, String prefix) {
        Optional<S3ObjectSummary> s3Object = s3client.listObjects(bucketName).getObjectSummaries().stream()
            .filter(os -> os.getKey().startsWith(prefix)).findFirst();
        if (!s3Object.isPresent()) {
            fail("Unable to find file with " + prefix + " prefix");
        }
        return s3Object.get().getKey();
    }

    public int getFileCount(String bucketName) {
        return s3client.listObjects(bucketName).getObjectSummaries().size();
    }

    public void cleanS3Bucket(String bucketName) {
        s3client.listObjects(bucketName).getObjectSummaries().forEach(os -> s3client.deleteObject(bucketName, os.getKey()));
    }

    /**
     * Method to delete all the buckets created by this instance of S3Utils.
     */
    @PreDestroy
    public void deleteAllBuckets() {
        Set<String> bucketNames = new HashSet<>(bucketsCreated.keySet());
        for (String bucketName : bucketNames) {
            deleteS3Bucket(bucketName);
        }
    }
}
