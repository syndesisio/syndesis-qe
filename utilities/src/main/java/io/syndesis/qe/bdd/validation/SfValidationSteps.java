package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.force.api.QueryResult;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.salesforce.Contact;
import io.syndesis.qe.salesforce.Lead;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterException;

/**
 * Validation steps for Salesforce related integrations.
 *
 * Dec 11, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SfValidationSteps {

	private final ForceApi salesforce;
	private final AccountsDirectory accountsDirectory;
	private String leadId;

	public SfValidationSteps() {
		accountsDirectory = AccountsDirectory.getInstance();
		final Account salesforceAccount = accountsDirectory.getAccount("salesforce").get();
		salesforce = new ForceApi(new ApiConfig()
				.setClientId(salesforceAccount.getProperty("clientId"))
				.setClientSecret(salesforceAccount.getProperty("clientSecret"))
				.setUsername(salesforceAccount.getProperty("userName"))
				.setPassword(salesforceAccount.getProperty("password"))
				.setForceURL(salesforceAccount.getProperty("loginUrl")));
	}

	@Given("^clean SF, removes all leads with email: \"([^\"]*)\"")
	public void cleanupSfDb(String email) {
		TestSupport.getInstance().resetDB();
		deleteAllSalesforceLeadsWithEmail(salesforce, email);
	}

	//twitter_talky
	@Given("^clean SF contacts related to TW account: \"([^\"]*)\"")
	public void cleanupSfContacts(String twAccount) throws TwitterException {
		deleteSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount).get().getProperty("screenName"));
	}

	@Then("^create SF lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\" and company: \"([^\"]*)\"")
	public void createNewSalesforceLead(String firstName, String lastName, String email, String companyName) {

		final Lead lead = new Lead();
		lead.setFirstName(firstName);
		lead.setLastName(lastName);
		lead.setCompany(companyName);
		lead.setEmail(email);
		salesforce.createSObject("lead", lead);
	}

	@Then("^delete lead from SF with email: \"([^\"]*)\"")
	public void deleteSalesforceLead(String email) {

		final Optional<Lead> lead = getSalesforceLeadByEmail(salesforce, email);
		if (lead.isPresent()) {
			leadId = String.valueOf(lead.get().getId());
			salesforce.deleteSObject("lead", leadId);
			log.debug("Deleting salesforce lead: {}", lead.get());
		}
	}

	@Then("^update SF lead with email \"([^\"]*)\" to first name: \"([^\"]*)\", last name \"([^\"]*)\", email \"([^\"]*)\", company name \"([^\"]*)\"")
	public void updateLead(String origEmail, String newFirstName, String newLastName, String newEmailAddress, String companyName) {

		leadId = getSalesforceLeadByEmail(salesforce, origEmail).get().getId();

		final Lead lead = new Lead();
		lead.setEmail(newEmailAddress);
		lead.setFirstName(newFirstName);
		lead.setLastName(newLastName);
		lead.setCompany(companyName);

		salesforce.updateSObject("lead", leadId, lead);
	}

	@Then("^validate contact for TW account: \"([^\"]*)\" is present in SF with description: \"([^\"]*)\"")
	public void validateIntegration(String twAccount, String record) throws TwitterException {
		log.info("Waiting until a contact appears in salesforce...");
		final long start = System.currentTimeMillis();
		final boolean contactCreated = TestUtils.waitForEvent(contact -> contact.isPresent(),
				() -> getSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount).get().getProperty("screenName")),
				TimeUnit.MINUTES,
				2,
				TimeUnit.SECONDS,
				5);
		Assertions.assertThat(contactCreated).as("Contact has appeard in salesforce").isEqualTo(true);
		log.info("Contact appeared in salesforce. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));

		final Contact createdContact = getSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount).get().getProperty("screenName")).get();
		Assertions.assertThat(createdContact.getDescription()).startsWith(record);
		Assertions.assertThat(createdContact.getFirstName()).isNotEmpty();
		Assertions.assertThat(createdContact.getLastname()).isNotEmpty();
		log.info("Salesforce contains contact with T integration test finished.");
	}

	@Then("check SF does not contain contact for tw accound: \"([^\"]*)\"")
	public void checkSfDoesNotContain(String twAccount) {
		Assertions.assertThat(getSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount)
				.get().getProperty("screenName")).isPresent()).isEqualTo(false);
	}

	private void deleteSalesforceContact(ForceApi salesforce, String screenName) {
		final Optional<Contact> contact = getSalesforceContact(salesforce, screenName);
		if (contact.isPresent()) {
			final String id = String.valueOf(contact.get().getId());
			salesforce.deleteSObject("contact", id);
			log.info("Deleting salesforce contact: {}", contact.get());
		}
	}

	private Optional<Contact> getSalesforceContact(ForceApi salesforce, String twitterName) {
		final QueryResult<Contact> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Description,Title FROM contact where Title='"
				+ twitterName + "'", Contact.class);
		final Optional<Contact> contact = queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
		return contact;
	}

	/**
	 * Looks for leads with specified first and last name and deletes them if it finds any.
	 *
	 * @param salesforce
	 */
	private void deleteAllSalesforceLeadsWithEmail(ForceApi salesforce, String email) {
		final Optional<Lead> lead = getSalesforceLeadByEmail(salesforce, email);
		if (lead.isPresent()) {
			final String id = String.valueOf(lead.get().getId());
			salesforce.deleteSObject("lead", id);
			log.debug("Deleting salesforce lead: {}", lead.get());
			deleteAllSalesforceLeadsWithEmail(salesforce, email);
		}
	}

	private Optional<Lead> getSalesforceLeadByEmail(ForceApi salesforce, String emailAddress) {
		final QueryResult<Lead> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Email,Company FROM lead where Email = '"
				+ emailAddress + "'", Lead.class
		);
		final Optional<Lead> lead = queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
		return lead;
	}
}
