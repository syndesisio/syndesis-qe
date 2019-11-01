package io.syndesis.qe.bdd.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.utils.SQSUtils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import cucumber.api.java.en.Then;
import io.cucumber.datatable.DataTable;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.model.Message;

@Slf4j
public class SNSValidationSteps {
    @Autowired
    private SQSUtils sqs;

    @Then("^verify that the SQS queue \"([^\"]*)\" contains notifications related to$")
    public void verifySNSMessage(String queue, DataTable content) {
        final List<Message> messages = sqs.getMessages(queue);
        Map<String, String> contentMap = content.asMap(String.class, String.class);

        for (Map.Entry<String, String> entry : contentMap.entrySet()) {
            assertThat(messages.stream().filter(m -> {
                JSONObject messageJson = new JSONObject(m.body());
                return messageJson.getString("Subject").equals(entry.getKey())
                    && messageJson.getString("Message").equals(entry.getValue());
            }).findAny()).as("Expected messages were not received").isPresent();
        }
    }
}
