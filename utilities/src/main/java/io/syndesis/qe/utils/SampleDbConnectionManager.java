package io.syndesis.qe.utils;

import org.assertj.core.api.Assertions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.Data;
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
                log.info("*POSTRGRES*");
                SampleDbConnectionManager.handleDbWrapper(dbType, 5432, 5432, "syndesis-db", "postgresql");
                break;
            case "mysql":
                log.info("*MYSQL*");
                SampleDbConnectionManager.handleDbWrapper(dbType, 3306, 3306, "mysql", "mysql");
                break;
        }

        Assertions.assertThat(connectionsInfoMap.get(dbType).getDbConnection()).isNotNull();
        return connectionsInfoMap.get(dbType).getDbConnection();
    }

    public static Connection getConnection() {
        return getConnection("postgresql");
    }

    public static void closeConnections() {
        connectionsInfoMap.entrySet().stream().forEach(ent -> releaseDbWrapper(ent.getValue()));
    }

    //AUXILIARIES:

    private static void handleDbWrapper(String dbType, int remotePort, int localPort, String podName, String driver) {
        //        check whether portForward and connection are alive:
        DbWrapper wrap;

        if (connectionsInfoMap.containsKey(dbType)) {
            log.info("*0*");
            wrap = connectionsInfoMap.get(dbType);
        } else {
            log.info("*1*");
            wrap = new DbWrapper(dbType);
        }

        log.info("*2*");
        if (wrap.getLocalPortForward() == null || !wrap.getLocalPortForward().isAlive()) {
            log.info("*3*");
            LocalPortForward localPortForward = createLocalPortForward(remotePort, localPort, podName);
            wrap.setLocalPortForward(localPortForward);
        }
        try {
            if (wrap.getDbConnection() == null || wrap.getDbConnection().isClosed()) {
                log.info("*4*");
                Connection dbConnection = SampleDbConnectionManager.createDbConnection(wrap.getLocalPortForward(), localPort, driver);
                wrap.setDbConnection(dbConnection);
                log.info("Putting dirver :*{}* and wrap: *{}* to map", driver, wrap.getDbType());
                connectionsInfoMap.put(driver, wrap);
                Assertions.assertThat(connectionsInfoMap).isNotEmpty();
            }
        } catch (SQLException ex) {
            log.info("*5*");
            log.error("Error: " + ex);
        }
    }

    private static Connection createDbConnection(LocalPortForward localPortForward, int localPort, String driver) throws SQLException {

        final Properties props = new Properties();
        if (driver.equalsIgnoreCase("mysql")) {
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
        log.info("DB endpoint URL: " + dbUrl);
        Connection dbConnection = DriverManager.getConnection(dbUrl, props);
        return dbConnection;
    }

    private static LocalPortForward createLocalPortForward(int remotePort, int localPort, String podName) {

        LocalPortForward localPortForward;

        OpenShiftUtils.getInstance().getPods().stream().forEach(p -> log.info("PODS: *{}*", p.getMetadata().getName()));

        Optional<Pod> dbPodOpt = OpenShiftUtils.getInstance().getPods().stream().filter(p -> p.getMetadata().getName().contains(podName)).findFirst();

        localPortForward = OpenShiftUtils.portForward(dbPodOpt.get(), remotePort, localPort);

        return localPortForward;
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
}
