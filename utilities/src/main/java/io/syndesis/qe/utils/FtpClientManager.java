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
            localPortForward = OpenShiftUtils.portForward(OpenShiftUtils.xtf().getAnyPod("app", ftpPodName), ftpPort, ftpPort);
            //since we use passive FTP connection, we need to forward data ports also
            for (int i = 0; i < 10; i++) {
                OpenShiftUtils.portForward(OpenShiftUtils.xtf().getAnyPod("app", ftpPodName), 2300+i, 2300+i);
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
        int i = 0;
        while (i < 3) {
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(ftpServer, ftpPort);
                ftpClient.login(ftpUser, ftpPass);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                log.info("Connected: {}", ftpClient.isConnected());
                return ftpClient;
            } catch (IOException e) {
                log.error(e.getMessage());
                log.info("Retrying in 30 seconds");
                i++;
                TestUtils.sleepIgnoreInterrupt(30000L);
            }
        }
        return null;
    }
}
