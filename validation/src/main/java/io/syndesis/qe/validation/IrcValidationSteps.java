package io.syndesis.qe.validation;

import static org.assertj.core.api.Assertions.assertThat;

import io.syndesis.qe.resource.impl.IRC;
import io.syndesis.qe.utils.OpenShiftUtils;
import io.syndesis.qe.utils.TestUtils;
import io.syndesis.qe.utils.http.HTTPUtils;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IrcValidationSteps {
    private String controllerRoute;

    @When("connect IRC controller to channels {string}")
    public void connectController(String channels) {
        controllerRoute = "http://" + OpenShiftUtils.getInstance().routes()
            .withName(IRC.CONTROLLER_APP_NAME).get().getSpec().getHost() + "/irc";
        HTTPUtils.doPostRequest(
            controllerRoute + "/connect",
            "{\"channels\":\"" + channels + "\"}",
            "application/json",
            null);
    }

    @Then("verify that the message with content {string} was posted to channels {string}")
    public void verifyThatMessageWasPosted(String content, String channels) {
        TestUtils.sleepIgnoreInterrupt(30000L);
        final int channelsCount = channels.split(",").length;
        Map<String, List<String>> receivedMessages = new Gson().fromJson(HTTPUtils.doGetRequest(controllerRoute + "/messages").getBody(), Map.class);
        assertThat(receivedMessages.keySet()).size().isEqualTo(channelsCount);
        for (String channel : channels.split(",")) {
            assertThat(receivedMessages.get(channel)).hasSize(1);
            assertThat(receivedMessages.get(channel).get(0)).isEqualTo(content);
        }
    }

    @When("send message to IRC user {string} with content {string}")
    public void sendMessage(String target, String msg) {
        HTTPUtils.doPostRequest(
            controllerRoute + "/messages",
            "{\"username\": \"" + target + "\", \"message\":\"" + msg + "\"}",
            "application/json",
            null);
    }
}
