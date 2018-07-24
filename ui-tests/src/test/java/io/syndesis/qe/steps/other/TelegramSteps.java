package io.syndesis.qe.steps.other;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.TelegramUtils;
import okhttp3.Response;
import org.assertj.core.api.Assertions;

import java.io.IOException;

public class TelegramSteps {
    @When("^send telegram message \"([^\"]*)\" on channel with id \"([^\"]*)\"$")
    public void sendMessage(String message, String channel) throws InterruptedException {
        TelegramUtils.sendMessage(message, channel);
    }

    @Then("^check that telegram last message contains string \"([^\"]*)\"$")
    public void checkLastMessageContains(String expectedText) {
        Response res = TelegramUtils.getUpdates();
        try {
            Assertions.assertThat(res.code())
                    .isEqualTo(200);
            Assertions.assertThat(res.body().string())
                    .containsIgnoringCase(expectedText);
        } catch (IOException e) {
            Assertions.fail("Error while processing telegram response", e);
        }
    }
}
