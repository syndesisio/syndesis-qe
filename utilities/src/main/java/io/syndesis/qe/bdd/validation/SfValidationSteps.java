package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.endpoints.TestSupport;
import io.syndesis.qe.salesforce.Contact;
import io.syndesis.qe.salesforce.Lead;
import io.syndesis.qe.utils.JMSUtils;
import io.syndesis.qe.utils.SalesforceAccount;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import com.force.api.ApiException;
import com.force.api.QueryResult;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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
    private String leadId;

    private static final long DEFAULT_WAIT_TIMEOUT = 30000L;

    @Given("^clean SF, removes all leads with email: \"([^\"]*)\"$")
    public void cleanupSfDb(String emails) {
        TestSupport.getInstance().resetDB();
        for (String email : emails.split(",")) {
            deleteAllSalesforceLeadsWithEmail(email.trim());
        }
    }


    @Then("^create SF lead with first name: \"([^\"]*)\", last name: \"([^\"]*)\", email: \"([^\"]*)\" and company: \"([^\"]*)\"")
    public void createNewSalesforceLead(String firstName, String lastName, String email, String companyName) {
        final Lead lead = new Lead();
        lead.setFirstName(firstName);
        lead.setLastName(lastName);
        lead.setCompany(companyName);
        lead.setEmail(email);
        leadId = SalesforceAccount.getInstance().createSObject("lead", lead);
        log.debug("Created lead with id " + leadId);
    }

    @Then("^delete lead from SF with email: \"([^\"]*)\"")
    public void deleteSalesforceLead(String email) {
        final Optional<Lead> lead = getSalesforceLeadByEmail(email);
        if (lead.isPresent()) {
            leadId = String.valueOf(lead.get().getId());
            SalesforceAccount.getInstance().deleteSObject("lead", leadId);
            log.debug("Deleting salesforce lead: {}", lead.get());
        }
    }

    @Then("^delete contact from SF with email: \"([^\"]*)\"")
    public void deleteSalesforceContactWithEmail(String email) {
        final Optional<Contact> lead = getSalesforceContactByEmail(email);
        if (lead.isPresent()) {
            SalesforceAccount.getInstance().deleteSObject("contact", String.valueOf(lead.get().getId()));
            log.debug("Deleting salesforce lead: {}", lead.get());
        }
    }

    @Then("^.*deletes? contact from SF with last name: \"([^\"]*)\"")
    public void deleteSalesforceContactWithName(String name) {
        final Optional<Contact> lead = getSalesforceContactByLastName(name);
        if (lead.isPresent()) {
            SalesforceAccount.getInstance().deleteSObject("contact", String.valueOf(lead.get().getId()));
            log.info("Deleting salesforce lead: {}", lead.get());
        } else {
            log.info("Contact with name {} was not found, nothing was deleted");
        }
    }

    @Then("^.*checks? that contact from SF with last name: \"([^\"]*)\" has description \"([^\"]*)\"$")
    public void checkSalesforceContactHasDescription(String name, String description) {
        try {
            OpenShiftWaitUtils.waitFor(() -> getSalesforceContactByLastName(name).isPresent(), DEFAULT_WAIT_TIMEOUT);
        } catch (TimeoutException | InterruptedException e) {
            fail("Salesforce contact with last name " + name + " was not found in " + DEFAULT_WAIT_TIMEOUT / 1000 + "seconds. ", e);
        }

        final Optional<Contact> contact = getSalesforceContactByLastName(name);
        assertThat(contact).isPresent();

        assertThat(String.valueOf(contact.get().getDescription()))
            .isNotEmpty()
            .isEqualToIgnoringCase(description);
    }

    @Then("^update SF lead with email \"([^\"]*)\" to first name: \"([^\"]*)\", last name \"([^\"]*)\", email \"([^\"]*)\", company name \"([^\"]*)" +
        "\"")
    public void updateLead(String origEmail, String newFirstName, String newLastName, String newEmailAddress, String companyName) {
        Optional<Lead> sfLead = getSalesforceLeadByEmail(origEmail);
        assertThat(sfLead).isPresent();
        leadId = sfLead.get().getId();

        final Lead lead = new Lead();
        lead.setEmail(newEmailAddress);
        lead.setFirstName(newFirstName);
        lead.setLastName(newLastName);
        lead.setCompany(companyName);

        SalesforceAccount.getInstance().updateSObject("lead", leadId, lead);
    }


    /**
     * Check if salesforce contact exists or does not exist, based on parameter `contains`
     *
     * @param contains if parameter contains == "contains" the method expects contact to exist
     */
    @Then("^check SF \"([^\"]*)\" contact with a email: \"([^\"]*)\"$")
    public void checkSalesforceContact(String contains, String email) {
        if ("contains".equalsIgnoreCase(contains)) {
            TestUtils.waitFor(() -> getSalesforceContactByEmail(email).isPresent(), 5, 60, "Salesforce contact does not exist!");
        } else {
            try {
                OpenShiftWaitUtils.waitFor(() -> {
                    log.info("Check that salesforce account with last name {} does not exist...", email);
                    return getSalesforceContactByEmail(email).isPresent();
                }, 5 * 1000L, 30 * 1000L);
                fail("Salesforce account with email {} should not exist!", email);
            } catch (InterruptedException | TimeoutException e) {
                log.info("Salesforce account with email {} should not exist and was not found = correct.", email);
            }
        }
    }

    private void checkSfAccount(String twAccount, boolean shouldExist) {

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
        try {
            OpenShiftWaitUtils.waitFor(() -> getSalesforceLeadByEmail(email).isPresent(), DEFAULT_WAIT_TIMEOUT);
        } catch (TimeoutException | InterruptedException e) {
            fail("Salesforce lead with email " + email + " was not found in " + DEFAULT_WAIT_TIMEOUT / 1000 + " seconds.");
        }

        Optional<Lead> lead = getSalesforceLeadByEmail(email);
        assertThat(lead).isPresent();
        assertThat(lead.get()).isInstanceOf(Lead.class);
        assertThat(lead.get().getFirstName()).isEqualTo("Joe");
    }

    @Then("^verify that lead creation response with email \"([^\"]*)\" was received from queue \"([^\"]*)\"$")
    public void verifyLeadCreatedResponse(String email, String queueName) {
        Optional<Lead> lead = getSalesforceLeadByEmail(email);
        assertThat(lead).isPresent();
        assertThat(JMSUtils.getMessageText(JMSUtils.Destination.QUEUE, queueName)).isEqualTo(String.format("{\"id\":\"%s\"}",
            lead.get().getId()));
    }

    @Then("^verify that lead was deleted$")
    public void verifyLeadRemoval() {
        try {
            OpenShiftWaitUtils.waitFor(() -> {
                try {
                    getLeadWithId(leadId);
                    return false;
                } catch (ApiException ex) {
                    return ex.getMessage().contains("The requested resource does not exist");
                }
            }, DEFAULT_WAIT_TIMEOUT);
        } catch (TimeoutException | InterruptedException e) {
            fail("Salesforce lead with id " + leadId + " was not deleted in " + DEFAULT_WAIT_TIMEOUT / 1000 + " seconds.");
        }
    }

    @Then("^verify that leads email was updated to \"([^\"]*)\"$")
    public void verifyLeadUpdated(String email) {
        try {
            OpenShiftWaitUtils.waitFor(() -> email.equals(getLeadWithId(leadId).getEmail()), DEFAULT_WAIT_TIMEOUT);
        } catch (TimeoutException | InterruptedException e) {
            fail("Salesforce email of lead with id " + leadId + " was not changed in " + DEFAULT_WAIT_TIMEOUT / 1000 + " seconds.");
        }
    }

    @Then("^verify that lead name was updated$")
    public void verifyLeadNameUpdate() {
        try {
            OpenShiftWaitUtils.waitFor(() -> "Joe".equals(getLeadWithId(leadId).getFirstName()), DEFAULT_WAIT_TIMEOUT);
        } catch (TimeoutException | InterruptedException e) {
            fail("Salesforce email of lead with id " + leadId + " was not changed in " + DEFAULT_WAIT_TIMEOUT / 1000 + " seconds.");
        }
    }

    private Lead getLeadWithId(String id) {
        return SalesforceAccount.getInstance().getSObject("lead", id).as(Lead.class);
    }

    private void deleteSalesforceContact(String screenName) {
        final Optional<Contact> contact = getSalesforceContact(screenName);
        if (contact.isPresent()) {
            final String id = String.valueOf(contact.get().getId());
            SalesforceAccount.getInstance().deleteSObject("contact", id);
            log.info("Deleting salesforce contact: {}", contact.get());
        }
    }

    private Optional<Contact> getSalesforceContact(String lastName) {
        final QueryResult<Contact> queryResult =
            SalesforceAccount.getInstance().query("SELECT Id,FirstName,LastName,Description,Title FROM contact where LastName='"
                + lastName + "'", Contact.class);
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
    }

    /**
     * Looks for leads with specified first and last name and deletes them if it finds any.
     *
     * @param email email
     */
    private void deleteAllSalesforceLeadsWithEmail(String email) {
        final Optional<Lead> lead = getSalesforceLeadByEmail(email);
        if (lead.isPresent()) {
            final String id = String.valueOf(lead.get().getId());
            SalesforceAccount.getInstance().deleteSObject("lead", id);
            log.debug("Deleting salesforce lead: {}", lead.get());
            deleteAllSalesforceLeadsWithEmail(email);
        }
    }

    private Optional<Lead> getSalesforceLeadByEmail(String emailAddress) {
        final QueryResult<Lead> queryResult =
            SalesforceAccount.getInstance().query("SELECT Id,FirstName,LastName,Email,Company FROM lead where Email = '"
                + emailAddress + "'", Lead.class
            );
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
    }

    private Optional<Contact> getSalesforceContactByEmail(String emailAddress) {
        final QueryResult<Contact> queryResult =
            SalesforceAccount.getInstance().query("SELECT Id,FirstName,LastName,Email FROM contact where Email = '"
                + emailAddress + "'", Contact.class
            );
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
    }

    private Optional<Contact> getSalesforceContactByLastName(String lastName) {
        final QueryResult<Contact> queryResult =
            SalesforceAccount.getInstance().query("SELECT Id,FirstName,LastName,Email,Description FROM contact where LastName = '"
                + lastName + "'", Contact.class
            );
        return queryResult.getTotalSize() > 0 ? Optional.of(queryResult.getRecords().get(0)) : Optional.empty();
    }
}
