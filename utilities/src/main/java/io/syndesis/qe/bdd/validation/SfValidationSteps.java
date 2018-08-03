package io.syndesis.qe.bdd.validation;

import static org.junit.Assert.fail;

import static org.assertj.core.api.Assertions.assertThat;

import com.force.api.ApiConfig;
import com.force.api.ApiException;
import com.force.api.ForceApi;
import com.force.api.QueryResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cucumber.api.Delimiter;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.salesforce.Contact;
import io.syndesis.qe.salesforce.Lead;
import io.syndesis.qe.utils.JMSUtils;
import io.syndesis.qe.utils.TestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Validation steps for Salesforce related integrations.
 * <p>
 * Dec 11, 2017 Red Hat
 *
 * @author tplevko@redhat.com
 */
@Slf4j
public class SfValidationSteps {
    private ForceApi salesforce;
    private final AccountsDirectory accountsDirectory;
    private String leadId;

    public SfValidationSteps() {
        accountsDirectory = AccountsDirectory.getInstance();
        final Account salesforceAccount = accountsDirectory.getAccount("QE Salesforce").get();
        int retries = 0;
        int timeoutInMinutes;
        while (retries < 4) {
            try {
                salesforce = new ForceApi(new ApiConfig()
                        .setClientId(salesforceAccount.getProperty("clientId"))
                        .setClientSecret(salesforceAccount.getProperty("clientSecret"))
                        .setUsername(salesforceAccount.getProperty("userName"))
                        .setPassword(salesforceAccount.getProperty("password"))
                        .setForceURL(salesforceAccount.getProperty("loginUrl")));
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                timeoutInMinutes = ++retries;
                log.error("Unable to connect to salesforce, will retry in {} minutes.", timeoutInMinutes);
                TestUtils.sleepIgnoreInterrupt(timeoutInMinutes * 60000L);
            }
        }
        fail("Unable to connect to SalesForce");
    }

    @Given("^clean SF, removes all leads with email: \"([^\"]*)\"")
    public void cleanupSfDb(@Delimiter(",") List<String> emails) {
        TestSupport.getInstance().resetDB();
        for (String email : emails) {
            deleteAllSalesforceLeadsWithEmail(salesforce, email);
        }
    }

    //twitter_talky
    @Given("^clean SF contacts related to TW account: \"([^\"]*)\"")
    public void cleanupSfContacts(String twAccount) {
        deleteSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount).get().getProperty("screenName"));
    }

    @Then("^create SF lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\" and company: \"([^\"]*)\"")
    public void createNewSalesforceLead(String firstName, String lastName, String email, String companyName) {
        final Lead lead = new Lead();
        lead.setFirstName(firstName);
        lead.setLastName(lastName);
        lead.setCompany(companyName);
        lead.setEmail(email);
        leadId = salesforce.createSObject("lead", lead);
        log.debug("Created lead with id " + leadId);
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

    @Then("^delete contact from SF with email: \"([^\"]*)\"")
    public void deleteSalesforceContact(String email) {

        final Optional<Contact> lead = getSalesforceContactByEmail(salesforce, email);
        if (lead.isPresent()) {
            salesforce.deleteSObject("contact", String.valueOf(lead.get().getId()));
            log.debug("Deleting salesforce lead: {}", lead.get());
        }
    }

    @Then("^.*deletes? contact from SF with last name: \"([^\"]*)\"")
    public void deleteSalesforceContactWithName(String name) {

        final Optional<Contact> lead = getSalesforceContactByLastName(salesforce, name);
        if (lead.isPresent()) {
            salesforce.deleteSObject("contact", String.valueOf(lead.get().getId()));
            log.info("Deleting salesforce lead: {}", lead.get());
        } else {
            log.info("Contact with name {} was not found, nothing was deleted");

        }
    }

    @Then("^.*checks? that contact from SF with last name: \"([^\"]*)\" has description \"([^\"]*)\"$")
    public void checkSalesforceContactHasDescription(String name, String description) {

        final Optional<Contact> contact = getSalesforceContactByLastName(salesforce, name);
        assertThat(contact.isPresent()).isTrue();

        assertThat(String.valueOf(contact.get().getDescription()))
                .isNotEmpty()
                .isEqualToIgnoringCase(description);
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
    public void validateIntegration(String twAccount, String record) {
        log.info("Waiting until a contact appears in salesforce...");
        final long start = System.currentTimeMillis();
        final boolean contactCreated = TestUtils.waitForEvent(Optional::isPresent,
                () -> getSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount).get().getProperty("screenName")),
                TimeUnit.MINUTES,
                3,
                TimeUnit.SECONDS,
                5);
        assertThat(contactCreated).as("Contact has appeard in salesforce").isEqualTo(true);
        log.info("Contact appeared in salesforce. It took {}s to create contact.", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));

        final Contact createdContact = getSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount).get().getProperty("screenName")).get();
        assertThat(createdContact.getDescription()).startsWith(record);
        assertThat(createdContact.getFirstName()).isNotEmpty();
        assertThat(createdContact.getLastname()).isNotEmpty();
        log.info("Salesforce contains contact with T integration test finished.");
    }

    @Then("check SF does not contain contact for tw accound: \"([^\"]*)\"")
    public void checkSfDoesNotContain(String twAccount) {
        assertThat(getSalesforceContact(salesforce, accountsDirectory.getAccount(twAccount)
                .get().getProperty("screenName")).isPresent()).isEqualTo(false);
    }

    @When("^publish message with content \'([^\']*)\' to queue \"([^\"]*)\"$")
    public void publishMessage(String content, String name) {
        JMSUtils.sendMessage(JMSUtils.Destination.QUEUE, name, content.replaceAll("LEAD_ID", leadId));
    }

    @Then("^verify that lead json object was received from queue \"([^\"]*)\"$")
    public void verifyLeadJsonReceived(String queueName) {
        final String text = JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, queueName);
        assertThat(text).contains(leadId);
    }

    @Then("^verify that lead with email \"([^\"]*)\" was created")
    public void verifyLeadCreated(String email) {
        Optional<Lead> lead = getSalesforceLeadByEmail(salesforce, email);
        assertThat(lead.get()).isInstanceOf(Lead.class);
        assertThat(lead.get().getFirstName()).isEqualTo("Joe");
    }

    @Then("^verify that lead creation response was received from queue \"([^\"]*)\"$")
    public void verifyLeadCreatedResponse(String queueName) {
        log.error("TODO: avano: https://github.com/syndesisio/syndesis/issues/2853");
    }

    @Then("^verify that lead was deleted$")
    public void verifyLeadRemoval() {
        // Add a delay for the integration processing
        TestUtils.sleepIgnoreInterrupt(5000L);
        try {
            getLeadWithId(leadId);
            fail("Getting deleted lead should result in exception");
        } catch (ApiException ex) {
            assertThat(ex.getMessage()).contains("The requested resource does not exist");
        }
    }

    @Then("^verify that leads email was updated to \"([^\"]*)\"$")
    public void verifyLeadUpdated(String email) {
        // Add a delay for the integration processing
        TestUtils.sleepIgnoreInterrupt(5000L);
        assertThat(getLeadWithId(leadId).getEmail()).isEqualTo(email);
    }

    @Then("^verify that lead name was updated$")
    public void verifyLeadNameUpdate() {
        // Add a delay for the integration processing
        TestUtils.sleepIgnoreInterrupt(5000L);
        assertThat(getLeadWithId(leadId).getFirstName()).isEqualTo("Joe");
    }

    private Lead getLeadWithId(String leadId) {
        return salesforce.getSObject("lead", leadId).as(Lead.class);
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
        final QueryResult<Contact> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Description,Title FROM contact where TwitterScreenName__c='"
                + twitterName + "'", Contact.class);
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
    }

    /**
     * Looks for leads with specified first and last name and deletes them if it finds any.
     *
     * @param salesforce salesforce object instance
     * @param email email
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
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
    }

    private Optional<Contact> getSalesforceContactByEmail(ForceApi salesforce, String emailAddress) {
        final QueryResult<Contact> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Email FROM contact where Email = '"
                + emailAddress + "'", Contact.class
        );
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();

    }

    private Optional<Contact> getSalesforceContactByLastName(ForceApi salesforce, String lastName) {
        final QueryResult<Contact> queryResult = salesforce.query("SELECT Id,FirstName,LastName,Email,Description FROM contact where LastName = '"
                + lastName + "'", Contact.class
        );
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();

    }
}
