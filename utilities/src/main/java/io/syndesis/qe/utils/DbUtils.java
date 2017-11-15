package io.syndesis.qe.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.LocalPortForward;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbUtils {

	private static DbUtils INSTANCE;

	public static DbUtils getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DbUtils();
		}
		return INSTANCE;
	}

	private DbUtils() {
		//no op
	}

	/**
	 * Execute given SQL command on sampledb in syndesis-db pod
	 * @param sqlCommnad
	 * @return resultSet
	 */
	public ResultSet executeQuery(String sqlCommnad) {
		Pod dbPod = OpenShiftUtils.getInstance().findComponentPod("syndesis-db");
		ResultSet resultSet = null;
		try (LocalPortForward localPortForward = OpenShiftUtils.getInstance().portForward(dbPod, 5432, 5432) ) {
			String url = "jdbc:postgresql://" + localPortForward.getLocalAddress().getHostAddress() +":" + localPortForward.getLocalPort() + "/sampledb";
			Properties props = new Properties();
			props.setProperty("user", "sampledb");

			try (Connection conn = DriverManager.getConnection(url, props)){
				log.info("Starting JDBC connection");
				PreparedStatement preparedStatement = conn.prepareStatement(sqlCommnad);
				resultSet = preparedStatement.executeQuery();

			} catch (SQLException e) {
				log.error("Error: ", e);
			}

		} catch (IOException ex) {
			log.error("Error: ", ex);
		}
		return resultSet;
	}

	/**
	 * Execute update command like DROP on sampledb
	 * @param sqlCommnad
	 * @return
	 */
	public int executeUpdate(String sqlCommnad) {
		Pod dbPod = OpenShiftUtils.getInstance().findComponentPod("syndesis-db");
		int result = 0;
		try (LocalPortForward localPortForward = OpenShiftUtils.getInstance().portForward(dbPod, 5432, 5432) ) {
			String url = "jdbc:postgresql://" + localPortForward.getLocalAddress().getHostAddress() +":" + localPortForward.getLocalPort() + "/sampledb";
			Properties props = new Properties();
			props.setProperty("user", "sampledb");

			try (Connection conn = DriverManager.getConnection(url, props)){
				log.info("Starting JDBC connection");
				PreparedStatement preparedStatement = conn.prepareStatement(sqlCommnad);
				result = preparedStatement.executeUpdate();
			} catch (SQLException e) {
				log.error("Error: ", e);
			}

		} catch (IOException ex) {
			log.error("Error: ", ex);
		}
		return result;
	}

}
