package io.syndesis.qe.steps.other;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.utils.AccountUtils;
import io.syndesis.qe.utils.GMailUtils;
import io.syndesis.qe.utils.GoogleAccount;
import io.syndesis.qe.utils.TestUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.api.services.gmail.model.Message;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.List;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GMailSteps {

    @Autowired
    @Qualifier("QE Google Mail")
    private GoogleAccount googleAccount;
    private GMailUtils gmu;

    @PostConstruct
    public void setup() {
        gmu = new GMailUtils(googleAccount);
    }

    @When("^.*send an e-mail$")
    public void sendMail() {
        sendEmail(gmu.getGmailAddress("QE Google Mail"));
    }

    @When("^.*send an e-mail to \"([^\"]*)\"$")
    public void sendEmail(String sendTo) {
        sendEmail(sendTo, "syndesis-tests");
    }

    @When("^.*send an e-mail to credentials \"([^\"]*)\"$")
    public void sendEmailToCreds(String credentials) {
        sendEmailToCreds(credentials, "syndesis-tests");
    }

    @When("^.*send an e-mail to credentials \"([^\"]*)\" with subject \"([^\"]*)\"$")
    public void sendEmailToCreds(String credentials, String subject) {
        Account account = AccountUtils.get(credentials);
        sendEmail(account.getProperty("username"), subject);
    }

    @When("^.*send an e-mail to \"([^\"]*)\" with subject \"([^\"]*)\"$")
    public void sendEmail(String sendTo, String subject) {
        gmu.sendEmail("me", sendTo, subject, "Red Hat");
    }

    @Given("^delete emails from \"([^\"]*)\" with subject \"([^\"]*)\"$")
    public void deleteMails(String from, String subject) {
        gmu.deleteMessages(gmu.getGmailAddress(from), subject);
    }

    @Given("^delete emails from credentials \"([^\"]*)\" with subject \"([^\"]*)\"$")
    public void deleteMailsFromCreds(String creds, String subject) {
        Account account = AccountUtils.get(creds);
        gmu.deleteMessages(account.getProperty("username"), subject);
    }

    @Then("^check that email from \"([^\"]*)\" with subject \"([^\"]*)\" and text \"([^\"]*)\" exists$")
    public void checkMails(String from, String subject, String text) {
        TestUtils.waitFor(() -> checkMailExists(gmu.getGmailAddress(from), subject, text),
            1, 60,
            "Could not find specified mail");
    }

    @Then("^check that email from credenitals \"([^\"]*)\" with subject \"([^\"]*)\" and text \"([^\"]*)\" exists")
    public void checkMailsFromCredentials(String credentials, String subject, String text) {
        Account account = AccountUtils.get(credentials);
        String username = account.getProperty("username");

        TestUtils.waitFor(() -> checkMailExists(username, subject, text),
            1, 60,
            "Could not find specified mail");
    }

    private boolean checkMailExists(String from, String subject, String text) {
        try {
            List<Message> messages = gmu.getMessagesMatchingQuery("me", "subject:" + subject + " AND from:" + from);
            // if messages list is empty, we haven't found anything, that's false
            if (messages.size() == 0) {
                return false;
            }

            MimeMessage mime = gmu.getMimeMessage(messages.get(0).getId());
            // now, if subject is the same and content is the same (some connectors might send trailing whitespaces)
            // then we have found our message
            if (mime.getSubject().equalsIgnoreCase(subject)
                && mime.getContent().toString().trim().equalsIgnoreCase(text.trim())) {
                return true;
            }
        } catch (IOException | MessagingException e) {
            log.debug("There has been an error while checking for existing mail", e);
        }

        return false;
    }
}
