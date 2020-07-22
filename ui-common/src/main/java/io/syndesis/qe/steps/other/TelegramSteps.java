package io.syndesis.qe.steps.other;

import io.syndesis.qe.utils.TelegramUtils;
import io.syndesis.qe.utils.http.HTTPResponse;

import org.assertj.core.api.Assertions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TelegramSteps {
    @When("^send telegram message \"([^\"]*)\" on channel with id \"([^\"]*)\"$")
    public void sendMessage(String message, String channel) throws InterruptedException {
        TelegramUtils.sendMessage(message, channel);
    }

    @Then("^check that telegram last message contains string \"([^\"]*)\"$")
    public void checkLastMessageContains(String expectedText) {
        HTTPResponse res = TelegramUtils.getUpdates();
        Assertions.assertThat(res.getCode()).isEqualTo(200);
        Assertions.assertThat(res.getBody()).containsIgnoringCase(expectedText);
    }
}
