package io.syndesis.qe.steps.other;

import org.assertj.core.api.Assertions;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.syndesis.qe.utils.HTTPResponse;
import io.syndesis.qe.utils.TelegramUtils;

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
