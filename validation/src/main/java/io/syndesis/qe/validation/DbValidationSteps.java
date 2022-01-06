package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.dballoc.DBAllocatorClient;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

/**
 * DB related validation steps.
 * <p>
 * Jan 17, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class DbValidationSteps {
    private DbUtils dbUtils;

    @Autowired
    private DBAllocatorClient dbAllocatorClient;

    public DbValidationSteps() {
        dbUtils = new DbUtils("postgresql");
    }

    @Given("remove all records from table {string}")
    public void cleanupDb(String tableName) {
        dbUtils.deleteRecordsInTable(tableName);
    }

    @When("insert into {string} table on {string}")
    public void insertsIntoTable(String tableName, String dbType, DataTable data) {
        dbUtils.setConnection(dbType);
        this.insertsIntoTable(tableName, data);
    }

    @When("insert into {string} table")
    public void insertsIntoTable(String tableName, DataTable data) {
        List<List<String>> dataTable = data.cells();

        String sql;

        for (List<String> list : dataTable) {
            switch (tableName.toUpperCase()) {
                case "TODO":
                    sql = "INSERT INTO TODO(task) VALUES(";
                    break;
                case "TODO WITH ID":
                    sql = "INSERT INTO TODO(id, task) VALUES(";
                    break;
                case "CONTACT":
                    sql = "INSERT INTO CONTACT(first_name, last_name, company, lead_source) VALUES(";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported table name " + tableName);
            }
            sql += list.stream().map(s -> "'" + s + "'").collect(Collectors.joining(",")) + ")";
            int newId = dbUtils.executeSQLGetUpdateNumber(sql);
            //assert new row in database has been created:
            assertThat(newId).isEqualTo(1);
        }
    }

    @Then("validate that all todos with task {string} have value completed {int}, period in ms: {int} on {string}")
    public void checksThatAllTodosHaveCompletedValDb(String task, Integer val, Integer ms, String dbType) {
        dbUtils.setConnection(dbType);
        this.checksThatAllTodosHaveCompletedVal(task, val, ms);
    }

    @Then("validate that all todos with task {string} have value completed {int}, period in ms: {int}")
    public void checksThatAllTodosHaveCompletedVal(String task, int count, int timeout) {
        String sql = String.format("SELECT * FROM TODO WHERE task like '%s' and completed != %d", task, count);
        TestUtils.withRetry(() -> dbUtils.executeSQLGetResultSet(sql) != null, 5, timeout, "Could not fetch data from database");
        assertThat(dbUtils.getCountOfInvokedQuery(sql)).isEqualTo(0);
    }

    @Then("^validate that number of all todos with task \"([^\"]*)\" (is|is greater than) (\\d+)$")
    public void checksNumberOfTodos(String task, String method, int expected) {
        AbstractIntegerAssert<?> a = assertThat(dbUtils.getNumberOfRecordsInTable("todo", "task", task));
        if ("is".equals(method)) {
            TestUtils.waitForNoFail(() -> dbUtils.getNumberOfRecordsInTable("todo", "task", task) == expected, 5, 30);
            assertThat(dbUtils.getNumberOfRecordsInTable("todo", "task", task)).isEqualTo(expected);
        } else {
            TestUtils.waitForNoFail(() -> dbUtils.getNumberOfRecordsInTable("todo", "task", task) > expected, 5, 30);
            assertThat(dbUtils.getNumberOfRecordsInTable("todo", "task", task)).isGreaterThan(expected);
        }
    }

    @Then("^check that query \"([^\"]*)\" has (\\d+ rows?|no|some) output$")
    public void checkQueryOutput(String query, String method) {
        switch (method) {
            case "no":
                assertThat(dbUtils.getCountOfInvokedQuery(query)).isEqualTo(0);
                break;
            case "some":
                TestUtils.waitForNoFail(() -> dbUtils.getCountOfInvokedQuery(query) > 0, 1, 30);
                assertThat(dbUtils.getCountOfInvokedQuery(query)).isGreaterThan(0);
                break;
            default:
                int expected = Integer.parseInt(method.split(" ")[0]);
                TestUtils.waitForNoFail(() -> dbUtils.getCountOfInvokedQuery(query) == expected, 1, 30);
                assertThat(dbUtils.getCountOfInvokedQuery(query)).isEqualTo(expected);
                break;
        }
    }

    @When("invoke database query {string}")
    public void invokeQuery(String query) {
        Assertions.assertThat(dbUtils.executeSQLGetUpdateNumber(query)).isGreaterThanOrEqualTo(0);
    }

    @When("insert into contact database randomized concur contact with name {string} and list ID {string}")
    public void createContactRowForConcur(String name, String listId) {
        String surname = RandomStringUtils.randomAlphabetic(12);
        String lead = RandomStringUtils.randomAlphabetic(12);

        invokeQuery("insert into CONTACT values ('" + name + "' , '" + surname + "', '" + listId + "' , '" + lead + "', '1999-01-01')");
    }

    @Given("reset content of {string} table")
    public void resetTableContent(String tableName) {
        dbUtils.deleteRecordsInTable(tableName);
    }

    @Given("truncate {string} table")
    public void truncateTable(String tableName) {
        // no special handling for contact table, use resetTableContent if that's what you need
        dbUtils.truncateTable(tableName);
    }

    @Given("execute SQL command {string}")
    public void executeSql(String sqlCmd) {
        new DbUtils("postgresql").executeSQLGetUpdateNumber(sqlCmd);
    }

    @Given("execute SQL command {string} on {string} driver")
    public void executeSqlOnDriver(String sqlCmd, String dbType) {
        new DbUtils(dbType).executeSQLGetUpdateNumber(sqlCmd);
    }

    @Given("create standard table schema on {string} driver")
    public void createStandardDBSchemaOn(String dbType) {
        new DbUtils(dbType).createEmptyTableSchema();
    }

    @Given("allocate new {string} database for {string} connection")
    public void allocateNewDatabase(String dbLabel, String connectionName) {

        dbAllocatorClient.allocate(dbLabel);
        log.info("Allocated database: '{}'", dbAllocatorClient.getDbAllocation());
        TestUtils.setDatabaseCredentials(connectionName.toLowerCase(), dbAllocatorClient.getDbAllocation());
    }

    @Given("free allocated {string} database")
    public void freeAllocatedDatabase(String dbLabel) {
        Assertions.assertThat(dbAllocatorClient.getDbAllocation().getDbLabel()).isEqualTo(dbLabel);
        dbAllocatorClient.free();
    }

    @Then("check rows number of table {string} is greater than {int}")
    public void checkRowsNumberIsGreaterThan(String table, int threshold) {
        TestUtils.waitFor(() -> this.dbUtils.getNumberOfRecordsInTable(table) > threshold,
                          5, 30,
                          "Not enough entries in the database");
    }

    @Then("verify that contact with first name {string} exists in database")
    public void verifyContactExists(String firstName) {
        checkQueryOutput(String.format("SELECT * FROM CONTACT WHERE first_name = '%s'", firstName), "1 row");
    }

    @When("wait until query {string} has output with timeout {int}")
    public void waitTillQueryHasOutput(String query, int timeout) {
        TestUtils.waitFor(() -> this.dbUtils.getCountOfInvokedQuery(query) > 0,
                          1, timeout,
                          "Data is not in the table after " + timeout + " seconds.");
    }

    @Then("check that contact table contains contact where first name is senderId for {string} account and company is {string}")
    public void checkThatTwitterMessageIsInContactTable(String account, String company) {
        long senderId = new TwValidationSteps().getSenderIdForAccount(account);
        checkQueryOutput(String.format("select * from contact where first_name = '%d' and company = '%s'", senderId, company), "1 row");
    }
}
