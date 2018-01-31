package io.syndesis.qe.utils;

import org.assertj.core.api.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbUtils {

	private Connection dbConnection;

	public DbUtils(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}

	/**
	 * Execute given SQL command on sampledb in syndesis-db pod.
	 *
	 * @param sqlCommnad
	 * @return
	 */
	public ResultSet readSqlOnSampleDb(String sqlCommnad) {
		ResultSet resultSet = null;
		final PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement(sqlCommnad);
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		return resultSet;
	}

	public int updateSqlOnSampleDb(String sqlCommnad) {
		int result=-2;
		final PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement(sqlCommnad);
			result = preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		return result;
	}

	/**
	 * Get number of records in table.
	 *
	 * @param tableName - name of the table, of which we want the number of items.
	 * @return
	 */
	public int getNumberOfRecordsInTable(String tableName) {

		int records = 0;
		final PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
			final ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				records = resultSet.getInt(1);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		log.debug("Number of records: " + records);

		return records;
	}

	/**
	 * Get number of records in table specified.
	 *
	 * @param tableName - name of the DB table.
	 * @param columnName - name of column in that table.
	 * @param value - value of the parameter.
	 * @return
	 */
	public int getNumberOfRecordsInTable(String tableName, String columnName, String value) {

		int records = 0;
		final PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement("SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " LIKE " + value);
			final ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				records = resultSet.getInt(1);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		log.debug("Number of records: " + records);

		return records;
	}

	/**
	 * Removes all data from specified table.
	 *
	 * @param tableName
	 */
	public void deleteRecordsInTable(String tableName) {
		final PreparedStatement preparedStatement;
		try {
			preparedStatement = dbConnection.prepareStatement("Delete FROM " + tableName);
			preparedStatement.executeUpdate();
			log.debug("Cleared table: " + tableName);
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
	}

}
