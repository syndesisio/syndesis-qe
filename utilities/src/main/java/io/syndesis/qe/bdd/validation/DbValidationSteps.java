package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

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
 *
 * Jan 17, 2018 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class DbValidationSteps {

	private final DbUtils dbUtils;

	public DbValidationSteps() {
		dbUtils = new DbUtils(SampleDbConnectionManager.getInstance().getConnection());
	}

	@Given("^remove all records from DB")
	public void cleanupDb() {
		TestSupport.getInstance().resetDB();
		dbUtils.deleteRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME());
	}

	@Then("^validate DB created new lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\"")
	public void validateSfDbIntegration(String firstName, String lastName, String emailAddress) {
		final long start = System.currentTimeMillis();
		// We wait for exactly 1 record to appear in DB.
		final boolean contactCreated = TestUtils.waitForEvent(leadCount -> leadCount == 1, () -> dbUtils.getNumberOfRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME()),
				TimeUnit.MINUTES,
				2,
				TimeUnit.SECONDS,
				5);
		Assertions.assertThat(contactCreated).as("Lead record has appeard in db").isEqualTo(true);
		log.info("Lead record appeared in DB. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
		// Now we verify, the created lead contains the correct personal information.
		Assertions.assertThat(getLeadTaskFromDb(firstName + " " + lastName).toLowerCase()).contains(emailAddress);
	}

	@Then("^validate SF on delete to DB created new task")
	public void validateLead() {
		final long start = System.currentTimeMillis();
		// We wait for exactly 1 record to appear in DB.
		final boolean contactCreated = TestUtils.waitForEvent(leadCount -> leadCount == 1, () -> dbUtils.getNumberOfRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME()),
				TimeUnit.MINUTES,
				2,
				TimeUnit.SECONDS,
				5);
		Assertions.assertThat(contactCreated).as("Lead record has appeard in db").isEqualTo(true);
		log.info("Lead record appeared in DB. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
		// Now we verify, the created lead contains the correct personal information.
		Assertions.assertThat(getLeadTaskFromDb().toLowerCase()).isNotEmpty();
	}

	/**
	 * Used for verification of successful creation of a new task in the todo app.
	 *
	 * @return
	 */
	private String getLeadTaskFromDb(String task) {

		String leadTask = null;
		try (ResultSet rs = dbUtils.executeSqlOnSampleDb("SELECT ID, TASK, COMPLETED FROM todo where task like '%"
				+ task + "%'");) {
			if (rs.next()) {
				leadTask = rs.getString("TASK");
				log.debug("TASK = " + leadTask);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		return leadTask;
	}

	private String getLeadTaskFromDb() {

		String leadTask = null;
		try (ResultSet rs = dbUtils.executeSqlOnSampleDb("SELECT ID, TASK, COMPLETED FROM todo");) {
			if (rs.next()) {
				leadTask = rs.getString("TASK");
				log.debug("TASK = " + leadTask);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		return leadTask;
	}
}
