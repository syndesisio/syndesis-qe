package io.syndesis.qe.util;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.resource.impl.SFTP;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

@Slf4j
public class SftpUtils implements FileTransferUtils {

    private SftpClientManager manager = new SftpClientManager();
    private SSHClient sshClient = manager.getSshClient();
    private SFTPClient sftpClient = manager.getSftpClient(sshClient);

    public void prepareServerForTest() {
        checkConnection();
        log.info("Preparing SFTP server for tests");
        try {
            //0. set 'test' as home folder
            //not possible - there is no such method in com.hierynomus.sshj, so we have to bypass this.
            //1. create download / upload folders
            sftpClient.mkdir("/" + SFTP.TEST_DIRECTORY + "/download");
            sftpClient.mkdir("/" + SFTP.TEST_DIRECTORY + "/upload");
        } catch (IOException e) {
            fail("Unable to prepare server", e);
        }
    }

    public void closeConnection(SSHClient sshCl, SFTPClient sftpCl) {
        manager.closeClient(sshCl, sftpCl);
    }

    @Override
    public void deleteFile(String path) {
        checkConnection();
        log.info("Deleting " + path + " from SFTP server");
        try {
            sftpClient.rm(path);
        } catch (IOException e) {
            fail("Unable to delete file", path);
        }
    }

    @Override
    public boolean isFileThere(String directory, String fileName) {
        checkConnection();
        try {
            List<RemoteResourceInfo> directoryContent = sftpClient.ls(directory);
            RemoteResourceInfo[] itemsArray = new RemoteResourceInfo[directoryContent.size()];
            itemsArray = directoryContent.toArray(itemsArray);
            return Arrays.stream(itemsArray).filter(file -> file.getName().equals(fileName)).count() == 1;
        } catch (IOException ex) {
            fail("Unable to list files in FTP", ex);
        }
        return false;
    }

    @Override
    public void uploadTestFile(String testFileName, String text, String remoteDirectory) {
        checkConnection();
        log.info("Uploading file " + testFileName + " with content " + text + " to directory " + remoteDirectory + ". This may take some time");
        try {
            Path tempFile = Files.createTempFile(testFileName, null,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
            FileUtils.write(tempFile.toFile(), text, "UTF-8");
            sftpClient.put(tempFile.toAbsolutePath().toString(), remoteDirectory + "/" + testFileName);
        } catch (IOException ex) {
            fail("Unable to SFTP upload test file: ", ex);
        }
    }

    @Override
    public String getFileContent(String directory, String fileName) {
        checkConnection();
        try {
            sftpClient.get(directory + "/" + fileName, fileName);
            FileInputStream inputStream = new FileInputStream(fileName);
            return IOUtils.toString(inputStream, "utf-8");
        } catch (Exception ex) {
            fail("Unable to read SFTP file " + directory + "/" + fileName);
        }
        return null;
    }

    /**
     * Sometimes the Connections ends with "FTPConnectionClosedException: Connection closed without indication".
     */
    private void checkConnection() {
        try {
            sftpClient.ls(".");
        } catch (IOException ex) {
            sshClient = manager.getSshClient();
            sftpClient = manager.getSftpClient(sshClient);
        }
    }
}
