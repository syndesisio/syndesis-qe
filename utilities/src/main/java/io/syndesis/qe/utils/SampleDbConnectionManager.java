package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;

import org.assertj.core.api.Assertions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

/**
 * Nov 15, 2017 Red Hat
 *
 * @author tplevko@redhat.com, sveres@redhat.com
 */
@Slf4j
public class SampleDbConnectionManager {

    private static Map<String, DbWrapper> connectionsInfoMap = new HashMap<>();

    public static Connection getConnection(String dbType) {

        switch (dbType) {
            case "postgresql":
                SampleDbConnectionManager.handlePortForwardDatabases(dbType, 5432, 5432, "syndesis-db", "postgresql");
                break;
            case "mysql":
                SampleDbConnectionManager.handlePortForwardDatabases(dbType, 3306, 3306, "mysql", "mysql");
                break;
            case "oracle12":
                SampleDbConnectionManager.handleExternalDatabases(dbType);
                break;
        }

        Assertions.assertThat(connectionsInfoMap).containsKey(dbType);
        Assertions.assertThat(connectionsInfoMap.get(dbType).getDbConnection()).isNotNull();
        return connectionsInfoMap.get(dbType).getDbConnection();
    }

    //    AUXILIARIES:
    private static void handleExternalDatabases(String dbType) {
        DbWrapper wrap = SampleDbConnectionManager.getWrap(dbType);
        try {
            if (wrap.getDbConnection() == null || wrap.getDbConnection().isClosed()) {
                Connection dbConnection = SampleDbConnectionManager.createDbConnection(dbType);
                wrap.setDbConnection(dbConnection);
                connectionsInfoMap.put(dbType, wrap);
                Assertions.assertThat(connectionsInfoMap).isNotEmpty();
            }
        } catch (SQLException ex) {
            fail("Error when handling external database", ex);
        }
    }

    public static Connection getConnection() {
        return getConnection("postgresql");
    }

    public static void closeConnections() {
        connectionsInfoMap.forEach((key, value) -> releaseDbWrapper(value));
        connectionsInfoMap.clear();
    }

    //AUXILIARIES:

    private static void handlePortForwardDatabases(String dbType, int remotePort, int localPort, String podName, String driver) {
        //        check whether portForward and connection are alive:
        DbWrapper wrap = SampleDbConnectionManager.getWrap(dbType);
        if (wrap.getLocalPortForward() == null || !wrap.getLocalPortForward().isAlive()) {
            LocalPortForward localPortForward = createLocalPortForward(remotePort, localPort, podName);
            wrap.setLocalPortForward(localPortForward);
        }

        try {
            if (wrap.getDbConnection() == null || wrap.getDbConnection().isClosed()) {
                Connection dbConnection = SampleDbConnectionManager.createDbConnection(wrap.getLocalPortForward(), localPort, driver);
                wrap.setDbConnection(dbConnection);
                connectionsInfoMap.put(driver, wrap);
                Assertions.assertThat(connectionsInfoMap).containsKey(driver);
                Assertions.assertThat(connectionsInfoMap).containsValue(wrap);
            }
        } catch (SQLException ex) {
            log.error("ERROR: *{}* ", ex);
        }
    }

    private static Connection createDbConnection(LocalPortForward localPortForward, int localPort, String driver) throws SQLException {

        final Properties props = new Properties();
        if ("mysql".equalsIgnoreCase(driver)) {
            props.setProperty("user", "developer");
            props.setProperty("password", "developer");
        } else {
            props.setProperty("user", "sampledb");
        }

        String dbUrl;
        try {
            String hostName = localPortForward.getLocalAddress().getLoopbackAddress().getHostName();
            dbUrl = String.format("jdbc:%s://%s:%s/sampledb", driver, hostName, localPort);
        } catch (IllegalStateException ex) {
            dbUrl = String.format("jdbc:%s://%s:%s/sampledb", driver, "127.0.0.1", localPort);
        }
        if ("mysql".equalsIgnoreCase(driver)) {
            dbUrl = dbUrl.concat("?useSSL=false");
        }
        log.info("DB endpoint URL: " + dbUrl);
        return DriverManager.getConnection(dbUrl, props);
    }

    private static Connection createDbConnection(String dbType) {

        final Properties props = new Properties();

        Optional<Account> optAccount = AccountsDirectory.getInstance().getAccount(dbType);
        Assertions.assertThat(optAccount.isPresent()).isTrue();
        Account account = optAccount.get();

        props.setProperty("user", account.getProperty("user"));
        props.setProperty("password", account.getProperty("password"));

        String dbUrl = account.getProperties().get("url");

        log.debug("DB endpoint URL: *{}*", dbUrl);
        try {
            return DriverManager.getConnection(dbUrl, props);
        } catch (SQLException e) {
            fail("Error creating DB connection.", e);
        }
        return null;
    }

    private static LocalPortForward createLocalPortForward(int remotePort, int localPort, String name) {
        final Service service = OpenShiftUtils.getInstance().services().list().getItems().stream()
            .filter(s -> s.getMetadata().getName().startsWith(name)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No service with name " + name + " found"));
        return OpenShiftUtils.getInstance().services().withName(service.getMetadata().getName()).portForward(remotePort, localPort);
    }

    private static void releaseDbWrapper(DbWrapper wrap) {

        try {
            if (wrap.getDbConnection() == null) {
                log.debug("There was no connection to database created, nothing to close.");
                return;
            }
            if (!wrap.getDbConnection().isClosed()) {
                wrap.getDbConnection().close();
            }
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }

        TestUtils.terminateLocalPortForward(wrap.getLocalPortForward());
    }

    private static DbWrapper getWrap(String dbType) {
        DbWrapper wrap;
        if (connectionsInfoMap.containsKey(dbType)) {
            wrap = connectionsInfoMap.get(dbType);
        } else {
            wrap = new DbWrapper(dbType);
        }
        return wrap;
    }
}
