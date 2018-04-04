package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.SampleDbConnectionManager;
import io.syndesis.qe.utils.TestUtils;
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

    private final DbUtils dbUtils;

    public DbValidationSteps() {
        dbUtils = new DbUtils(SampleDbConnectionManager.getConnection());
    }

    @Given("^remove all records from table \"([^\"]*)\"")
    public void cleanupDb(String tableName) {
        TestSupport.getInstance().resetDB();
        dbUtils.deleteRecordsInTable(tableName);
    }

    @Then("^validate DB created new lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\"")
    public void validateSfDbIntegration(String firstName, String lastName, String emailAddress) throws InterruptedException {
        Thread.sleep(5000);
        final long start = System.currentTimeMillis();
        // We wait for exactly 1 record to appear in DB.
        final boolean contactCreated = TestUtils.waitForEvent(leadCount -> leadCount == 1, () -> dbUtils.getNumberOfRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME()),
                TimeUnit.MINUTES,
                2,
                TimeUnit.SECONDS,
                5);
        Assertions.assertThat(contactCreated).as("Lead record has appeard in db 1").isEqualTo(true);
        log.info("Lead record appeared in DB. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
        // Now we verify, the created lead contains the correct personal information.
        Assertions.assertThat(getLeadTaskFromDb(firstName + " " + lastName).toLowerCase()).contains(emailAddress);
    }

    @Then("^validate SF on delete to DB created new task.*$")
    public void validateLead() {
        final long start = System.currentTimeMillis();
        // We wait for exactly 1 record to appear in DB.
        final boolean contactCreated = TestUtils.waitForEvent(leadCount -> leadCount == 1, () -> dbUtils.getNumberOfRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME()),
                TimeUnit.MINUTES,
                2,
                TimeUnit.SECONDS,
                5);
        Assertions.assertThat(contactCreated).as("Lead record has appeard in db 2").isEqualTo(true);
        log.info("Lead record appeared in DB. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
        // Now we verify, the created lead contains the correct personal information.
        Assertions.assertThat(getLeadTaskFromDb().toLowerCase()).isNotEmpty();
    }

    @Then("^validate add_lead procedure with last_name: \"([^\"]*)\", company: \"([^\"]*)\", period in ms: \"(\\w+)\"")
    public void validateAddLeadProcedure(String lastName, String company, Integer ms) throws InterruptedException {
        //wait for period cycle:
        Thread.sleep(ms + 1000);
        final long start = System.currentTimeMillis();
        // We wait for at least 1 record to appear in DB (procedure goes on every 5 seconds).
        final boolean contactCreated = TestUtils.waitForEvent(leadCount -> leadCount >= 1, () -> dbUtils.getNumberOfRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME()),
                TimeUnit.MINUTES,
                2,
                TimeUnit.SECONDS,
                5);
        Assertions.assertThat(contactCreated).as("Lead record has appeard in DB, todo table").isEqualTo(true);
        Assertions.assertThat(getLeadTaskFromDb(lastName).contains(company));
    }

    @Then("^inserts into \"([^\"]*)\" table$")
    public void insertsIntoTable(String tableName, DataTable data) throws SQLException {

        List<List<String>> dataTable = data.raw();

        String sql = null;
        switch (tableName.toLowerCase()) {
            case "todo":
//                INSERT INTO TODOx(task) VALUES('Joe');
                sql = "INSERT INTO todo(task) VALUES('%s'";
                break;
            case "contact":
//                INSERT INTO CONTACT(first_name, last_name, company, lead_source) VALUES('Josef','Stieranka','Istrochem','db');
                sql = "INSERT INTO CONTACT(first_name, last_name, company, lead_source) VALUES('%s'";
                break;
        }

        Iterator it;
        String next;
        for (List<String> list : dataTable) {
            it = list.iterator();
            while (it.hasNext()) {
                next = (String) it.next();
                if (it.hasNext()) {
                    sql = String.format(sql, next) + ", '%s'";
                } else {
                    sql = String.format(sql, next) + ")";
                }
            }
        }
        log.info("SQL query: *{}*", sql);
        int newId = dbUtils.executeSQLGetUpdateNumber(sql);

        //assert new row in database has been created:
        Assertions.assertThat(newId).isEqualTo(1);
    }

    @Then("^validate that all todos with task \"([^\"]*)\" have value completed \"(\\w+)\", period in ms: \"(\\w+)\"$")
    public void checksThatAllTodosHaveCompletedVal(String task, Integer val, Integer ms) throws InterruptedException, SQLException {
        Thread.sleep(ms + 1000);

        ResultSet rs;
        List<Integer> completedAll = new ArrayList<>();
        String sql = String.format("SELECT completed FROM todo WHERE task like '%s'", task);
        log.info("SQL **{}**", sql);
        rs = dbUtils.executeSQLGetResultSet(sql);
        while (rs.next()) {
            Assertions.assertThat(rs.getInt("completed")).isEqualTo(val);
        }

    }

    @Then("^validate that number of all todos with task \"([^\"]*)\" is \"(\\w+)\", period in ms: \"(\\w+)\"$")
    public void checksNumberOfTodos(String task, Integer val, Integer ms) throws InterruptedException {
        Thread.sleep(ms + 1000);
        int number = dbUtils.getNumberOfRecordsInTable("todo", "task", task);
        Assertions.assertThat(number).isEqualTo(val);
    }

    @Then("^validate that number of all todos with task \"([^\"]*)\" is greater than \"(\\w+)\"$")
    public void checkNumberOfTodosMoreThan(String task, Integer val) {
        int number = dbUtils.getNumberOfRecordsInTable("todo", "task", task);
        Assertions.assertThat(number).isGreaterThan(val);
    }

    @And("^.*checks? that query \"([^\"]*)\" has some output$")
    public void checkValuesExistInTable(String query) {
        Assertions.assertThat(dbUtils.getCountOfInvokedQuery(query)).isGreaterThanOrEqualTo(1);
    }

    @And("^.*invokes? database query \"([^\"]*)\"")
    public void invokeQuery(String query) throws SQLException {
        Assertions.assertThat(dbUtils.executeSQLGetUpdateNumber(query)).isGreaterThanOrEqualTo(0);
    }

    @Given("^.*reset content of \"([^\"]*)\" table")
    public void resetTableContent(String tableName) {
        if (tableName.equalsIgnoreCase("contact")) {
            dbUtils.resetContactTable();
        } else {
            //there is no default content in other tables
            dbUtils.deleteRecordsInTable(tableName);
        }
    }

//AUXILIARIES:

    /**
     * Used for verification of successful creation of a new task in the todo app.
     *
     * @return
     */
    private String getLeadTaskFromDb(String task) {
        String leadTask = null;
        log.info("***SELECT ID, TASK, COMPLETED FROM todo where task like '%" + task + "%'***");
        try (ResultSet rs = dbUtils.executeSQLGetResultSet("SELECT ID, TASK, COMPLETED FROM todo where task like '%"
                + task + "%'");) {
            if (rs.next()) {
                leadTask = rs.getString("TASK");
                log.debug("TASK = " + leadTask);
            }
        } catch (SQLException ex) {
            Assertions.fail("Error: " + ex);
        }
        return leadTask;
    }

    private String getLeadTaskFromDb() {
        String leadTask = null;
        try (ResultSet rs = dbUtils.executeSQLGetResultSet("SELECT ID, TASK, COMPLETED FROM todo");) {
            if (rs.next()) {
                leadTask = rs.getString("TASK");
                log.debug("TASK = " + leadTask);
            }
        } catch (SQLException ex) {
            Assertions.fail("Error: " + ex);
        }
        return leadTask;
    }
}
