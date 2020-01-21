package io.syndesis.qe.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 1, 2018 Red Hat
 *
 * @author sveres@redhat.com
 */
@Slf4j
public class FtpClientManager {

    private static String ftpServer = "127.0.0.1";
    private static String ftpPodName = "ftpd";
    private static int ftpPort = 2121;
    private static String ftpUser = "anonymous";
    private static String ftpPass = "";

    private static LocalPortForward localPortForward = null;

    private FtpClientManager() {
    }

    public static FTPClient getClient() {
        if (localPortForward == null || !localPortForward.isAlive()) {
            localPortForward = OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", ftpPodName), ftpPort, ftpPort);
            //since we use passive FTP connection, we need to forward data ports also
            for (int i = 0; i < 10; i++) {
                OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", ftpPodName), 2300 + i, 2300 + i);
            }

        }
        return FtpClientManager.initClient();

    }

    public static void closeFtpClient(FTPClient ftpClient) {
        TestUtils.terminateLocalPortForward(localPortForward);
        try {
            if (ftpClient == null) {
                return;
            }
            if (!ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static FTPClient initClient() {
        FTPClient ftpClient = new FTPClient();
        TestUtils.withRetry(() -> {
            try {
                ftpClient.connect(ftpServer, ftpPort);
                ftpClient.login(ftpUser, ftpPass);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                return true;
            } catch (IOException e) {
                log.error("Unable to connect FTP client: " + e.getMessage());
                return false;
            }
        }, 3, 30000L, "Unable to create FTP client after 3 retries");
        return ftpClient;
    }
}
