package io.syndesis.qe.util;

import static org.assertj.core.api.Assertions.fail;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpUtils implements FileTransferUtils {
    private FtpClientManager manager = new FtpClientManager();
    private FTPClient ftpClient = manager.getClient();

    @Override
    public void deleteFile(String path) {
        checkConnection();
        log.info("Deleting " + path + " from FTP server");
        try {
            ftpClient.deleteFile(path);
        } catch (IOException e) {
            fail("Unable to delete file", path);
        }
    }

    @Override
    public boolean isFileThere(String directory, String fileName) {
        checkConnection();
        try {
            return Arrays.stream(ftpClient.listFiles(directory)).filter(file -> file.getName().equals(fileName)).count() == 1;
        } catch (IOException ex) {
            fail("Unable to list files in FTP", ex);
        }
        return false;
    }

    @Override
    public void uploadTestFile(String testFileName, String text, String remoteDirectory) {
        checkConnection();
        log.info("Uploading file " + testFileName + " with content " + text + " to directory " + remoteDirectory + ". This may take some time");
        try (InputStream is = IOUtils.toInputStream(text, "UTF-8")) {
            ftpClient.storeFile("/" + remoteDirectory + "/" + testFileName, is);
        } catch (IOException ex) {
            fail("Unable to upload test file: ", ex);
        }
    }

    @Override
    public String getFileContent(String directory, String fileName) {
        try {
            return IOUtils.toString(ftpClient.retrieveFileStream(directory + "/" + fileName), "utf-8");
        } catch (Exception ex) {
            fail("Unable to read FTP file " + directory + "/" + fileName);
        }
        return null;
    }

    /**
     * Sometimes the Connections ends with "FTPConnectionClosedException: Connection closed without indication".
     */
    private void checkConnection() {
        try {
            ftpClient.listFiles();
        } catch (IOException ex) {
            ftpClient = manager.getClient();
        }
    }
}
