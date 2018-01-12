package io.syndesis.qe.validation;

import org.assertj.core.api.Assertions;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.force.api.QueryResult;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.salesforce.Lead;
import io.syndesis.qe.utils.DbUtils;
import io.syndesis.qe.utils.RestConstants;
import io.syndesis.qe.utils.SampleDbConnectionManager;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

/**
 * Validation steps for Salesforce to database integrations.
 *
 * Dec 11, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SfDbValidationSteps {

	private final DbUtils dbUtils;
	private final ForceApi salesforce;
	private final AccountsDirectory accountsDirectory;

	public SfDbValidationSteps() throws IOException, TwitterException {
		accountsDirectory = AccountsDirectory.getInstance();
		final Account salesforceAccount = accountsDirectory.getAccount("salesforce").get();
		dbUtils = new DbUtils(SampleDbConnectionManager.getInstance().getConnection());
		salesforce = new ForceApi(new ApiConfig()
				.setClientId(salesforceAccount.getProperty("clientId"))
				.setClientSecret(salesforceAccount.getProperty("clientSecret"))
				.setUsername(salesforceAccount.getProperty("userName"))
				.setPassword(salesforceAccount.getProperty("password"))
				.setForceURL(salesforceAccount.getProperty("loginUrl")));
	}

	@Given("^clean before SF to DB, removes user with first name: \"([^\"]*)\" and last name: \"([^\"]*)\"")
	public void cleanupSfDb(String firstName, String lastName) throws TwitterException {
		TestSupport.getInstance().resetDB();
		deleteSalesforceLead(salesforce, firstName, lastName);
		dbUtils.deleteRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME());
	}

	@Then("^clean after SF to DB, removes user with first name: \"([^\"]*)\" and last name: \"([^\"]*)\"")
	public void tearDownSfDb(String firstName, String lastName) throws TwitterException {
		cleanupSfDb(firstName, lastName);
		SampleDbConnectionManager.getInstance().closeConnection();
	}

	@Then("^validate SF to DB created new lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\"")
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
		Assertions.assertThat(getLeadTaskFromDb(firstName, lastName).toLowerCase()).contains(emailAddress);
	}

	/**
	 * Used for verification of successful creation of a new task in the todo app.
	 *
	 * @return
	 */
	private String getLeadTaskFromDb(String firstName, String lastName) {

		String leadTask = null;
		try (ResultSet rs = dbUtils.executeSqlOnSampleDb("SELECT ID, TASK, COMPLETED FROM todo where task like '%"
				+ firstName + " " + lastName + "%'");) {
			if (rs.next()) {
				leadTask = rs.getString("TASK");
				log.debug("TASK = " + leadTask);
			}
		} catch (SQLException ex) {
			log.error("Error: " + ex);
		}
		return leadTask;
	}

	@Then("^create SF lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\" and company: \"([^\"]*)\"")
	public void createNewSalesforceLead(String firstName, String lastName, String email, String companyName) {
		// The table has to be empty prior to testing.
		Assertions.assertThat(dbUtils.getNumberOfRecordsInTable(RestConstants.getInstance().getTODO_APP_NAME())).isEqualTo(0);
		final Lead lead = new Lead();
		lead.setFirstName(firstName);
		lead.setLastName(lastName);
		lead.setCompany(companyName);
		lead.setEmail(email);

		final String id = salesforce.createSObject("lead", lead);
	}

	/**
	 * Looks for leads with specified first and last name and deletes them if it finds any.
	 *
	 * @param salesforce
	 */
	private void deleteSalesforceLead(ForceApi salesforce, String firstName, String lastName) {
		final Optional<Lead> lead = getSalesforceLead(salesforce, firstName, lastName);
		if (lead.isPresent()) {
			final String id = String.valueOf(lead.get().getId());
			salesforce.deleteSObject("lead", id);
			log.info("Deleting salesforce lead: {}", lead.get());
			deleteSalesforceLead(salesforce, firstName, lastName);
		}
	}

	private Optional<Lead> getSalesforceLead(ForceApi salesforce, String firstName, String lastName) {
		final QueryResult<Lead> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Email,Company FROM lead where FirstName = '"
				+ firstName + "' and LastName='" + lastName + "'", Lead.class
		);
		final Optional<Lead> lead = queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
		return lead;
	}
}
