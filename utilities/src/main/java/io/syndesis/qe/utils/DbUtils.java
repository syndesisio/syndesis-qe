package io.syndesis.qe.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

@Slf4j
public class DbUtils {

    private Connection dbConnection;

    public DbUtils(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }


    /**
     * ******************************************
     * BASIC METHODS
     * ******************************************
     */


    /**
     * Best to use with SELECT
     *
     * @param sqlCommand
     * @return a ResultSet object that contains the data produced by the query; never null
     */
    public ResultSet executeSQLGetResultSet(String sqlCommand) {
        ResultSet resultSet = null;
        final PreparedStatement preparedStatement;
        try {
            preparedStatement = dbConnection.prepareStatement(sqlCommand);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }
        return resultSet;
    }

    /**
     * Best to use with INSERT, UPDATE or DELETE
     *
     * @param sqlCommnad
     * @return either the row count for sqlCommnad statements or 0 for sqlCommnad statements that return nothing
     */
    public int executeSQLGetUpdateNumber(String sqlCommnad) {
        final PreparedStatement preparedStatement;
        int result = -1;
        try {
            preparedStatement = dbConnection.prepareStatement(sqlCommnad);
            result = preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }
        return result;
    }


    /**
     * ******************************************
     * SPECIFIC METHODS
     * ******************************************
     */


    /**
     * Get number of records in specific table. You may specify column and value.
     * <p>
     * Example:
     * <p>
     * <p>
     * getNumberOfRecordsInTable(myTable)
     * <p>
     * or
     * <p>
     * getNumberOfRecordsInTable(myTable, desiredColumn, desiredValue)
     *
     * @param tableName
     * @param args      check example
     * @return
     */
    public int getNumberOfRecordsInTable(String tableName, String... args) {
        if (!(args.length == 0 || args.length == 2)) {
            throw new IllegalArgumentException("Incorrect usage of this method.");
        }

        int records = 0;
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName;

            if (args.length == 2) {
                //be specific
                sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + args[0] + " LIKE '" + args[1] + "'";
            }
            log.info("SQL: *{}*", sql);
            final ResultSet resultSet = executeSQLGetResultSet(sql);

            while (resultSet.next()) {
                records = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }
        log.debug("Number of records: " + records);

        return records;
    }

    public int getCountOfInvokedQuery(String query) {

        int records = 0;
        try {
            log.info("SQL: *{}*", query);
            final ResultSet resultSet = executeSQLGetResultSet(query);

            //inefficient but it works :/ our table has 2 rows so it is not a problem...
            while (resultSet.next()) {
                records++;
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
        String sql = "Delete FROM " + tableName;
        executeSQLGetUpdateNumber(sql);
        log.debug("Cleared table: " + tableName);
    }

    public void resetContactTable() {
        deleteRecordsInTable("contact");
        Assertions.assertThat(executeSQLGetUpdateNumber
                ("insert into contact values " +
                        "('Joe', 'Jackson', 'Red Hat', 'db', '" + LocalDateTime.now().toLocalDate() + "')"))
                .isEqualTo(1);
    }
}
