package io.syndesis.qe.utils.telegram;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class TelegramClient {
    public void sendMessageToChannel(String chatId, String text) {
        TelegramSimpleManager.sendMessageToChannel(chatId, text);
    }

    public boolean validateReceivedMessage(String text, String chatId) {
        return TelegramSimpleManager.validateReceivedMessage(text, chatId);
    }
}
