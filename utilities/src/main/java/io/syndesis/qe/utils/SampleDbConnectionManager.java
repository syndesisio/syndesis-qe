package io.syndesis.qe.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import io.fabric8.kubernetes.api.model.Pod;
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
		initiateDbLocalPortForward();
	}

	public static Connection getConnection() {
		final Properties props = new Properties();
		props.setProperty("user", "sampledb");
		try {
			if (dbConnection == null || dbConnection.isClosed()) {
				dbUrl = "jdbc:postgresql://" + localPortForward.getLocalAddress().getHostAddress() + ":" + localPortForward.getLocalPort() + "/sampledb";
				dbConnection = DriverManager.getConnection(dbUrl, props);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}

		return dbConnection;
	}

	public static void closeConnection() {
		terminateLocalPortForward();
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

	private static void initiateDbLocalPortForward() {
		if (localPortForward == null || !localPortForward.isAlive()) {
			final Pod dbPod = OpenShiftUtils.getInstance().findComponentPod("syndesis-db");
			localPortForward = OpenShiftUtils.getInstance().portForward(dbPod, 5432, 5432);
		}
	}

	private static void terminateLocalPortForward() {
		if (localPortForward == null) {
			return;
		}
		if (localPortForward.isAlive()) {
			try {
				localPortForward.close();
			} catch (IOException ex) {
				log.error("Error: " + ex);
			}
		} else {
			log.info("Local Port Forward already closed.");
		}
	}
}
