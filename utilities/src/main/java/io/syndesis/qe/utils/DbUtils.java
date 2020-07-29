package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.endpoint.ConnectionsActionsEndpoint;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbUtils {
    private Connection dbConnection;
    private String databaseType;

    public DbUtils(String dbType) {
        this.databaseType = dbType;
        this.dbConnection = SampleDbConnectionManager.getConnection(dbType);
    }

    public void setConnection(String dbType) {
        this.databaseType = dbType;
        this.dbConnection = SampleDbConnectionManager.getConnection(dbType);
    }

    /**
     * Best to use with SELECT.
     *
     * @param sqlCommand sql query to execute
     * @return a ResultSet object that contains the data produced by the query; never null
     */
    public ResultSet executeSQLGetResultSet(String sqlCommand) {
        reopenConnectionIfIsClosed();
        ResultSet resultSet = null;
        try {
            log.debug("Executing SQL query: " + sqlCommand);
            resultSet = dbConnection.prepareStatement(sqlCommand).executeQuery();
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }
        return resultSet;
    }

    /**
     * Best to use with INSERT, UPDATE or DELETE.
     *
     * @param sqlCommand sql query to execute
     * @return either the row count for sqlCommand statements or 0 for sqlCommand statements that return nothing
     */
    public int executeSQLGetUpdateNumber(String sqlCommand) {
        reopenConnectionIfIsClosed();
        int result = -1;
        try {
            log.debug("Executing SQL query: " + sqlCommand);
            result = dbConnection.prepareStatement(sqlCommand).executeUpdate();
        } catch (SQLException ex) {
            log.error("Error: " + ex);
        }
        return result;
    }

    /**
     * Get the row count of given table.
     *
     * @param tableName table name
     * @return row count
     */
    public int getNumberOfRecordsInTable(String tableName) {
        return getNumberOfRecordsInTable(tableName, null, null);
    }

    /**
     * Get number of records in specific table. You may specify column and value.
     *
     * @param tableName table name
     * @param column table column
     * @param value value to compare the column to
     * @return number of records in the table
     */
    public int getNumberOfRecordsInTable(String tableName, String column, String value) {
        String query = "SELECT * FROM " + tableName.toUpperCase();
        if (column != null && value != null) {
            query += " WHERE " + column + " LIKE '" + value + "'";
        }
        return getCountOfInvokedQuery(query);
    }

    /**
     * Gets the count of records returned by given query.
     *
     * @param query sql query
     * @return count of rows for given query
     */
    public int getCountOfInvokedQuery(String query) {
        int records = 0;
        try {
            ResultSet resultSet = executeSQLGetResultSet(query);
            while (resultSet != null && resultSet.next()) {
                records++;
            }
        } catch (SQLException e) {
            fail("Unable to get count from ResultSet: " + e);
        }
        return records;
    }

    /**
     * Checks if the connection is still alive.
     *
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
     * @param tableName table name to delete
     */
    public void deleteRecordsInTable(String tableName) {
        executeSQLGetUpdateNumber("Delete FROM " + tableName.toUpperCase());
        log.debug("Cleared table: " + tableName.toUpperCase());
    }

    /**
     * Truncates a table, removing all the contents and resetting autoincrement field
     *
     * @param tableName table name to truncate
     */
    public void truncateTable(String tableName) {
        executeSQLGetUpdateNumber("TRUNCATE TABLE " + tableName.toUpperCase() + " RESTART IDENTITY");
        log.debug("Truncated table: " + tableName.toUpperCase());
    }

    /**
     * Creates an empty table schema in contact and todo tables.
     */
    public void createEmptyTableSchema() {
        final String sqlQuery1 = "DROP TABLE CONTACT";
        final String sqlQuery2 = "DROP TABLE TODO";
        final String sqlQuery3 =
            "CREATE TABLE CONTACT ( first_name VARCHAR(250), last_name VARCHAR(250), company VARCHAR(250), lead_source VARCHAR(250), create_date " +
                "DATE)";
        final String sqlQuery4 = "CREATE TABLE TODO ( id int, task VARCHAR(250), completed int)";

        if (this.executeSQLGetUpdateNumber(sqlQuery1) == -1 || this.executeSQLGetUpdateNumber(sqlQuery2) == -1) {
            log.error("Errors thrown due to not existing table when trying to drop the table, safe to ignore");
        }
        this.executeSQLGetUpdateNumber(sqlQuery3);
        this.executeSQLGetUpdateNumber(sqlQuery4);
    }

    public static String getStoredProcedureTemplate(String connectionId, String storedProcedureName, boolean start) {
        return new ConnectionsActionsEndpoint(connectionId).getStoredProcedureTemplate(storedProcedureName, start);
    }

    public void reopenConnectionIfIsClosed() {
        try {
            if (dbConnection.isClosed()) {
                dbConnection = SampleDbConnectionManager.getConnection(databaseType);
            }
        } catch (SQLException ex) {
            log.error("Error: " + ex);
            fail("SQLException occurred");
        }
    }
}
