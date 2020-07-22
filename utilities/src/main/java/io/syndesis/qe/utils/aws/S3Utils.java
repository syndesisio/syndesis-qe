package io.syndesis.qe.utils.aws;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.utils.builder.SdkBuilder;

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
    private final S3Client s3client;
    // holder for buckets created by this instance
    @Getter
    private Set<String> bucketsCreated = new HashSet<>();

    public S3Utils() {
        final Account s3Account = AccountsDirectory.getInstance().getAccount(Account.Name.AWS).get();
        s3client = S3Client.builder()
            .region(Region.of(s3Account.getProperty("region")))
            .credentialsProvider(() -> AwsBasicCredentials.create(s3Account.getProperty("accessKey"), s3Account.getProperty("secretKey")))
            .build();
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
        s3client.createBucket(b -> b.bucket(bucketName));
        bucketsCreated.add(bucketName);
    }

    /**
     * Method that checks if bucket exists in S3 instance.
     * @param bucketName name of the bucket
     * @return true if exists, false otherwise
     */
    public boolean doesBucketExist(String bucketName) {
        return s3client.listBuckets(SdkBuilder::build).buckets().stream().anyMatch(b -> bucketName.equals(b.name()));
    }

    public void deleteS3Bucket(String bucketName) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName).build();
        ListObjectsV2Response listObjectsV2Response;

        do {
            listObjectsV2Response = s3client.listObjectsV2(listObjectsV2Request);
            for (S3Object s3Object : listObjectsV2Response.contents()) {
                s3client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(s3Object.key()).build());
            }

            listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName)
                .continuationToken(listObjectsV2Response.nextContinuationToken())
                .build();
        } while (listObjectsV2Response.isTruncated());
        s3client.deleteBucket(b -> b.bucket(bucketName));
    }

    /**
     * Creates a text file in the specified S3 bucket.
     *
     * @param bucketName name of the bucket
     * @param fileName filename that will be created in s3
     * @param text file content
     */
    public void createTextFile(String bucketName, String fileName, String text) {
        try {
            final File temp = File.createTempFile(fileName, "");
            temp.deleteOnExit();
            final BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write(text);
            bw.close();
            s3client.putObject(b -> b.bucket(bucketName).key(fileName), RequestBody.fromFile(temp));
        } catch (IOException ex) {
            log.error("Error with tmp file: " + ex);
        }
    }

    /**
     * Checks if the specified text file exists in specified S3 bucket.
     *
     * @param bucketName bucket name
     * @param fileName file name
     * @return true/false
     */
    public boolean checkFileExistsInBucket(String bucketName, String fileName) {
        return s3client.listObjectsV2(b -> b.bucket(bucketName)).contents().stream().anyMatch(o -> fileName.equals(o.key()));
    }

    /**
     * Gets specified text file content from specified S3 bucket.
     *
     * @param bucketName bucket name
     * @param fileName file name
     * @return file content
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
        return s3client.getObject(b -> b.bucket(bucketName).key(fileName), ResponseTransformer.toInputStream());
    }

    public String getFileNameWithPrefix(String bucketName, String prefix) {
        Optional<S3Object> s3Object = s3client.listObjectsV2(b -> b.bucket(bucketName)).contents()
            .stream().filter(o -> o.key().startsWith(prefix)).findFirst();
        if (!s3Object.isPresent()) {
            fail("Unable to find file with " + prefix + " prefix");
        }
        return s3Object.get().key();
    }

    public int getFileCount(String bucketName) {
        return s3client.listObjectsV2(b -> b.bucket(bucketName)).contents().size();
    }

    public void cleanS3Bucket(String bucketName) {
        s3client.listObjectsV2(b -> b.bucket(bucketName)).contents().forEach(c -> s3client.deleteObject(b -> b.bucket(bucketName).key(c.key())));
    }

    /**
     * Method to delete all the buckets created by this instance of S3Utils.
     */
    @PreDestroy
    public void deleteAllBuckets() {
        for (String bucketName : bucketsCreated) {
            deleteS3Bucket(bucketName);
        }
    }
}
