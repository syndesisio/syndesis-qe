package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;

import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 * Dec 17, 2019 Red Hat
 *
 * @author sveres@redhat.com
 */
@Slf4j
public class SftpClientManager {

    private final String sftpServer = "127.0.0.1";
    private final int sftpLocalPort = 2222;
    private int sftpRemotePort;
    private String sftpPodName;
    private String sftpUser;
    private String sftpPass;

    private static LocalPortForward localPortForward = null;

    public SftpClientManager() {
        initProperties();
    }

    public SSHClient getSshClient() {
        if (localPortForward == null || !localPortForward.isAlive()) {
            localPortForward = OpenShiftUtils.portForward(OpenShiftUtils.getInstance().getAnyPod("app", sftpPodName), sftpRemotePort,
                sftpLocalPort);
        }
        return initSshClient();
    }

    public void closeClient(SSHClient sshClient, SFTPClient sftpClient) {
        TestUtils.terminateLocalPortForward(localPortForward);
        try {
            if (sshClient == null) {
                return;
            }
            if (!sshClient.isConnected()) {
                sftpClient.close();
                sshClient.disconnect();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private SSHClient initSshClient() {
        SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        TestUtils.withRetry(() -> {
            try {
                sshClient.connect(sftpServer, sftpLocalPort);
                sshClient.authPassword(sftpUser, sftpPass);
                log.info("SSH client connected: {}", sshClient.isConnected());
                return true;
            } catch (IOException e) {
                fail("SSH client failed to be created", e);
                log.info("Retrying create ssh client in 30 seconds");
                return false;
            }
        }, 9, 30000L, "Unable to create ssh client after 9 retries");
        return sshClient;
    }

    public SFTPClient getSftpClient(SSHClient sshClient) {
        try {
            log.info("SSH client connected: {}", sshClient.isConnected());
            SFTPClient sftpClient = sshClient.newSFTPClient();
            return sftpClient;
        } catch (IOException e) {
            fail("creation of SFTP client failed", e);
            return null;
        }
    }

    private void initProperties() {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.SFTP);
        if (optional.isPresent()) {
            Map<String, String> properties = new HashMap<>();
            optional.get().getProperties().forEach((key, value) ->
                properties.put(key.toLowerCase(), value)
            );
            sftpUser = properties.get("username");
            sftpPass = properties.get("password");
            sftpPodName = properties.get("host");
            sftpRemotePort = Integer.parseInt(properties.get("port"));
        } else {
            Assert.fail("Credentials for " + Account.Name.SFTP + " were not found!");
        }
    }
}
