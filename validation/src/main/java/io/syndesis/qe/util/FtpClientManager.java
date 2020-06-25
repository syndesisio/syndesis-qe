package io.syndesis.qe.util;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.resource.impl.FTP;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

/**
 * Mar 1, 2018 Red Hat
 *
 * @author sveres@redhat.com
 */
@Slf4j
public class FtpClientManager {

    private static LocalPortForward LOCAL_PORT_FORWARD = null;

    private final String ftpServer = "127.0.0.1";
    private final int ftpLocalPort = 2121;  //on local computer

    private String ftpPodName;
    private int ftpRemotePort;
    private String ftpUser;
    private String ftpPass;

    public FtpClientManager() {
        initProperties();
    }

    public FTPClient getClient() {

        if (LOCAL_PORT_FORWARD == null || !LOCAL_PORT_FORWARD.isAlive()) {
            LOCAL_PORT_FORWARD = OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", ftpPodName), ftpRemotePort, ftpLocalPort);
            //since we use passive FTP connection, we need to forward data ports also
            for (int i = 0; i < 10; i++) {
                OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", ftpPodName), FTP.FTP_DATA_PORT + i, FTP.FTP_DATA_PORT + i);
            }
        }
        return initClient();
    }

    public void closeFtpClient(FTPClient ftpClient) {
        OpenShiftUtils.terminateLocalPortForward(LOCAL_PORT_FORWARD);
        try {
            if (ftpClient == null) {
                return;
            }
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private FTPClient initClient() {
        FTPClient ftpClient = new FTPClient();
        TestUtils.withRetry(() -> {
            try {
                ftpClient.connect(ftpServer, ftpLocalPort);
                ftpClient.login(ftpUser, ftpPass);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                ftpClient.setDataTimeout(500000);
                return true;
            } catch (IOException e) {
                log.error("Unable to connect FTP client: " + e.getMessage());
                return false;
            }
        }, 3, 30000L, "Unable to create FTP client after 3 retries");
        return ftpClient;
    }

    private void initProperties() {
        Account account = AccountsDirectory.getInstance().get(Account.Name.FTP);
        Map<String, String> properties = new HashMap<>();
        account.getProperties().forEach((key, value) ->
            properties.put(key.toLowerCase(), value)
        );
        ftpUser = properties.get("username");
        ftpPass = properties.get("password");
        ftpPodName = properties.get("host");
        ftpRemotePort = Integer.parseInt(properties.get("port"));
    }
}
