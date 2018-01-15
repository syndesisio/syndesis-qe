package io.syndesis.qe.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

/**
 * Nov 15, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SampleDbConnectionManager {

	private static LocalPortForward localPortForward = null;
	private static String dbUrl;
	private static Connection dbConnection = null;
	private static SampleDbConnectionManager instance;

	public static SampleDbConnectionManager getInstance() {
		if (instance == null) {
			instance = new SampleDbConnectionManager();
		}
		return instance;
	}

	private SampleDbConnectionManager() {
		localPortForward = TestUtils.createLocalPortForward("syndesis-db", 5432, 5432);
	}

	public static Connection getConnection() {
		final Properties props = new Properties();
		props.setProperty("user", "sampledb");
		try {
			if (dbConnection == null || dbConnection.isClosed()) {
				dbUrl = "jdbc:postgresql://" + localPortForward.getLocalAddress().getLoopbackAddress().getHostName() + ":" + localPortForward.getLocalPort() + "/sampledb";
				dbConnection = DriverManager.getConnection(dbUrl, props);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}

		return dbConnection;
	}

	public static void closeConnection() {
		TestUtils.terminateLocalPortForward(localPortForward);
		try {
			if (dbConnection == null) {
				return;
			}
			if (!dbConnection.isClosed()) {
				dbConnection.close();
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
	}
}
