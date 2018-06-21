package io.syndesis.qe.bdd.validation;

import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Then;
import io.syndesis.qe.utils.telegram.TelegramClient;
import io.syndesis.qe.utils.telegram.TelegramSimpleManager;

public class TelegramValidationSteps {

    @Autowired
    TelegramClient telegramClient;

    @Then("^send telegram message with text \"([^\"]*)\" to \"([^\"]*)\" chat$")
    public void sendMessageTelegram(String text, String chatFromId) {
        telegramClient.sendMessageToChannel(chatFromId, text);

    }

    @Then("^validate telegram message with text \"([^\"]*)\" was sent to \"([^\"]*)\" chat$")
    public void validateTelegramReceive(String text, String chatToId) {
        Assertions.assertThat(telegramClient.validateReceivedMessage(text, chatToId)).isTrue();
    }
}
