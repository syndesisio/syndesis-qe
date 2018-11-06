package io.syndesis.qe.steps.other;

import com.google.api.services.gmail.model.Message;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.GMailUtils;
import io.syndesis.qe.utils.GoogleAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
        gmu.sendEmail("me", "jbossqa.fuse@gmail.com", "syndesis-tests", "Red Hat");
    }

    @Given("^delete emails from \"([^\"]*)\" with subject \"([^\"]*)\"$")
    public void deleteMails(String from, String subject) {
        gmu.deleteMessages(from, subject);
    }

    @Then("^check that email from \"([^\"]*)\" with subject \"([^\"]*)\" and text \"([^\"]*)\" exists$")
    public void checkMails(String from, String subject, String text) {
        try {
            List<Message> messages = gmu.getMessagesMatchingQuery("me", "subject:" + subject + " AND from:" + from);
            assertThat(messages.size()).isEqualTo(1);

            MimeMessage mime = gmu.getMimeMessage(messages.get(0).getId());
            assertThat(mime.getSubject()).isEqualToIgnoringCase(subject);
            //note that getContent() works here on because of specific message we sent (nothing special inside of the message,
            //no attachment etc. otherwise extracting text should be handled differently
            assertThat(mime.getContent().toString()).isEqualToIgnoringCase(text);

        } catch (IOException | MessagingException e) {
            fail("Exception thrown while checking mails!", e);
        }
    }
}
