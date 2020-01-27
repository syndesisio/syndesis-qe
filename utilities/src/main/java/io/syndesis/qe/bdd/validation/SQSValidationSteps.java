package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.JMSUtils;
import io.syndesis.qe.utils.SQSUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.wait.OpenShiftWaitUtils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName;

@Slf4j
public class SQSValidationSteps {
    @Autowired
    private SQSUtils sqs;

    @Given("^purge SQS queues:$")
    public void purge(DataTable queues) {
        sqs.purge(queues.asList().toArray(new String[0]));
        // Purging a queue "may take up to 60 seconds"
        TestUtils.sleepIgnoreInterrupt(60000L);
    }

    @When("^send SQS messages? to \"([^\"]*)\" with content$")
    public void sendMessages(String queue, DataTable table) {
        sqs.sendMessages(queue, table.column(0));
    }

    @When("^send (\\d+) ordered messages to \"([^\"]*)\"$")
    public void sendOrderedMessages(int count, String queue) {
        List<String> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(i + "");
        }
        sqs.sendMessages(queue, messages);
    }

    @When("^wait until the message appears in SQS queue \"([^\"]*)\"$")
    public void waitForMessage(String queue) {
        // Wait until the calibration message was processed
        try {
            OpenShiftWaitUtils.waitFor(() -> sqs.getQueueSize(queue) == 1, 1000L, 120000L);
        } catch (Exception e) {
            // ignore
        }
    }

    @Then("^verify that the SQS queue \"([^\"]*)\" has (\\d+) messages? after (\\d+) seconds?$")
    public void verifyQueueSize(String queueName, int size, int timeout) {
        TestUtils.sleepIgnoreInterrupt(timeout * 1000L);
        assertThat(sqs.getQueueSize(queueName)).isEqualTo(size);
    }

    @Then("^verify that the message from SQS queue \"([^\"]*)\" has content \'([^\']*)\'$")
    public void verifyMessageContent(String queue, String content) {
        assertThat(sqs.getMessages(queue).get(0).body()).isEqualTo(content);
    }

    @Then("^verify that (\\d+) messages were received from AMQ \"([^\"]*)\" \"([^\"]*)\" and are in order$")
    public void verifyFifoReceivedMessages(int count, String destinationType, String name) {
        List<String> messages = new ArrayList<>();
        String m = JMSUtils.getMessageText(JMSUtils.Destination.valueOf(destinationType.toUpperCase()), name);
        messages.add(m);
        while (m != null) {
            m = JMSUtils.getMessageText(JMSUtils.Destination.valueOf(destinationType.toUpperCase()), name);
            if (m != null) {
                messages.add(m);
            }
        }

        assertThat(messages).hasSize(count);

        int lastMsgBody = 0;
        for (String message : messages) {
            assertThat(new JSONObject(message).getInt("body")).isEqualTo(lastMsgBody++);
        }
    }

    @Then("^verify that all messages in SQS queue \"([^\"]*)\" have groupId \"([^\"]*)\"$")
    public void verifySameGroupId(String queueName, String groupId) {
        for (Message message : sqs.getMessages(queueName)) {
            assertThat(message.attributes().get(MessageSystemAttributeName.MESSAGE_GROUP_ID)).isEqualTo(groupId);
        }
    }

    @Then("^verify that all messages in SQS queue \"([^\"]*)\" have different groupId$")
    public void verifyDifferentGroupId(String queueName) {
        Set<String> seenIds = new HashSet<>();
        for (Message message : sqs.getMessages(queueName)) {
            assertThat(seenIds).doesNotContain(message.attributes().get(MessageSystemAttributeName.MESSAGE_GROUP_ID));
            seenIds.add(message.attributes().get(MessageSystemAttributeName.MESSAGE_GROUP_ID));
        }
    }
}
