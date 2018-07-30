package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpUtils {

    private FTPClient ftpClient;

    public FtpUtils(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public void delete(String path) {
        log.info("Deleting " + path + " from FTP server");
        try {
            ftpClient.deleteFile(path);
        } catch (IOException e) {
            fail("Unable to delete file", path);
        }
    }

    public boolean isThereFile(String directory, String fileName) {
        try {
            return Arrays.stream(ftpClient.listFiles(directory)).filter(file -> file.getName().equals(fileName)).count() == 1;
        } catch (IOException ex) {
            fail("Unable to list files in FTP", ex);
        }
        return false;
    }

    public boolean download(String fullRemoteFromFilename, String fullLocalToFilename) throws IOException {

        log.info("Trying to download file from server *{}* to *{}*", fullRemoteFromFilename, fullLocalToFilename);

        File downloadFile = new File(fullLocalToFilename);

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
        boolean success = ftpClient.retrieveFile(fullRemoteFromFilename, outputStream);

        outputStream.close();

        if (success) {
            log.info("File *{}* has been downloaded successfully!", fullRemoteFromFilename);
            return true;
        } else {
            log.info("DOWNLOAD FAILED!");
            return false;
        }
    }

    public boolean upload(String fullLocalFromFilename, String fullRemoteToFilename) throws IOException {

        log.info("Trying to upload file from *{}* to server *{}*", fullLocalFromFilename, fullRemoteToFilename);

        File localFile = new File(fullLocalFromFilename);
        InputStream inputStream = new FileInputStream(localFile);

        boolean done = ftpClient.storeFile(fullRemoteToFilename, inputStream);
        inputStream.close();
        if (done) {
            log.info("The first file *{}* is uploaded successfully to server *{}*.", fullLocalFromFilename, fullRemoteToFilename);
            return true;
        } else {
            log.info("UPLOAD FAILED!");
            return false;
        }
    }

    public void uploadTestFile(String testFileName, String text, String remoteDirectory) {
        log.info("Uploading file " + testFileName + " with content " + text + " to directory " + remoteDirectory + ". This may take some time");
        try (InputStream is = IOUtils.toInputStream(text, "UTF-8")) {
            ftpClient.storeFile("/" + remoteDirectory + "/" + testFileName, is);
        } catch (IOException ex) {
            ex.printStackTrace();
            fail("Unable to upload test file: ", ex);
        }
    }
}
