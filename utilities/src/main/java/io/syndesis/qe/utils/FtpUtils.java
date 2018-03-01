package io.syndesis.qe.utils;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpUtils {

    private FTPClient ftpClient;

    public FtpUtils(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public boolean isThereFile(String fullRemoteFilename) throws IOException {

        log.info("CHECKING EXISTENCE OF *{}", fullRemoteFilename);

        InputStream inputStream = ftpClient.retrieveFileStream(fullRemoteFilename);
        int returnCode = ftpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            log.info("File *{}* IS NOT there", fullRemoteFilename);
            return false;
        } else {
            log.info("File *{}* IS there", fullRemoteFilename);
            return true;
        }
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

        System.out.println("Start uploading first file");
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
}
