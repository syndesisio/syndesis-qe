package io.syndesis.qe.utils;

import org.assertj.core.api.Assertions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbUtils {

    private Connection dbConnection;

    public DbUtils(String dbType) {
        this.dbConnection = SampleDbConnectionManager.getConnection(dbType);
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
     * @param args check example
     * @return
     */
    public int getNumberOfRecordsInTable(String tableName, String... args) {
        if (!(args.length == 0 || args.length == 2)) {
            throw new IllegalArgumentException("Incorrect usage of this method.");
        }

        int records = 0;
        try {
            String sql = "SELECT COUNT(*) FROM " + tableName.toUpperCase();

            if (args.length == 2) {
                //be specific
                sql = "SELECT COUNT(*) FROM " + tableName.toUpperCase() + " WHERE " + args[0] + " LIKE '" + args[1] + "'";
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
            while (resultSet != null && resultSet.next()) {
                records++;
            }
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }
        log.debug("Number of records: " + records);
        return records;
    }

    /**
     * Checks if the connection is still alive.
     * @return true/false
     */
    public boolean isConnectionValid() {
        try {
            log.info("Validating DB connection; When exception printed out, the connection is not valid");
            return dbConnection.isValid(30);
        } catch (Exception e) {
            // This exception is *not* the same as mentioned in the info log ^ . The isValid method is printing out the exception by default.
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Removes all data from specified table.
     *
     * @param tableName
     */
    public void deleteRecordsInTable(String tableName) {
        String sql = "Delete FROM " + tableName.toUpperCase();
        executeSQLGetUpdateNumber(sql);
        log.debug("Cleared table: " + tableName.toUpperCase());
    }

    /**
     * Truncates a table, removing all the contents and resetting autoincrement field
     *
     * @param tableName
     */
    public void truncateTable(String tableName) {
        executeSQLGetUpdateNumber("TRUNCATE TABLE " + tableName.toUpperCase() + " RESTART IDENTITY");
        log.debug("Truncated table: " + tableName.toUpperCase());
    }

    public void resetContactTable() {
        deleteRecordsInTable("contact");
        Assertions.assertThat(executeSQLGetUpdateNumber
                ("insert into CONTACT values " +
                        "('Joe', 'Jackson', 'Red Hat', 'db', '" + LocalDateTime.now().toLocalDate() + "')"))
                .isEqualTo(1);
    }

    public void createSEmptyTableSchema() {

        final String sqlQuery1 = "DROP TABLE CONTACT";
        final String sqlQuery2 = "DROP TABLE TODO";
        final String sqlQuery3 = "CREATE TABLE CONTACT ( first_name VARCHAR(250), last_name VARCHAR(250), company VARCHAR(250), lead_source VARCHAR(250), create_date DATE)";
        final String sqlQuery4 = "CREATE TABLE TODO ( id int, task VARCHAR(250), completed int)";

        if(this.executeSQLGetUpdateNumber(sqlQuery1) == -1 || this.executeSQLGetUpdateNumber(sqlQuery2) == -1) {
            log.error("Errors thrown due to not existing table when trying to drop the table, safe to ignore");
        }
        this.executeSQLGetUpdateNumber(sqlQuery3);
        this.executeSQLGetUpdateNumber(sqlQuery4);
    }

    public void setConnection(String dbType) {
        this.dbConnection = SampleDbConnectionManager.getConnection(dbType);
    }
}
