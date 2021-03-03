package io.syndesis.qe.utils;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;

import org.assertj.core.api.Assertions;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.github.seratch.jslack.api.methods.request.conversations.ConversationsListRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.github.seratch.jslack.api.methods.response.conversations.ConversationsListResponse;
import com.github.seratch.jslack.api.model.Conversation;
import com.github.seratch.jslack.api.model.Message;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Lazy
public class SlackUtils {
    private static final Slack SLACK = Slack.getInstance();
    private String token;

    /**
     * To create instance of slack connector, correct credentials must be set up. Class expects
     * slack connector with name "QE Slack". Syndesis connector requires "webhookUrl" with
     * web hook for desired workspace. For tests to work there must be property "Token". Token
     * is API Token for slack workspace custom integration called "Bots".
     */
    public SlackUtils() {
        Account account = AccountsDirectory.getInstance().get(Account.Name.SLACK);
        token = account.getProperty("token");
        Assertions.assertThat(token).as("There is no slack property \"Token\" in credentials file!");
    }

    /**
     * Send message to specified channel.
     * <p>
     * Note that Slack QE credentials must have "Token" property correctly set up for desired workspace.
     *
     * @param message message
     * @param channelName channel
     * @return response
     * @throws IOException when something goes wrong
     * @throws SlackApiException when something goes wrongn
     */
    public ChatPostMessageResponse sendMessage(String message, String channelName) throws IOException, SlackApiException {
        // find all channels in the workspace
        ConversationsListResponse conversationsList = SLACK.methods().conversationsList(ConversationsListRequest.builder().token(token).build());
        // find channelName
        Conversation chann = conversationsList.getChannels().stream()
                .filter(c -> c.getName().equals(channelName)).findFirst().get();

        // https://slack.com/api/chat.postMessage
        return SLACK.methods().chatPostMessage(ChatPostMessageRequest.builder()
            .token(token)
            .channel(chann.getId())
            .username("syndesis-bot")
            .text(message)
            .build());
    }

    /**
     * Check if last message on channel is expectedMessage.
     * <p>
     * Note that Slack QE credentials must have "Token" property correctly set up for desired workspace.
     *
     * @param expectedMessage expected message
     * @param channelName channel
     * @throws IOException when something goes wrong
     * @throws SlackApiException when something goes wrong
     */
    public void checkLastMessageFromChannel(String expectedMessage, String channelName) throws IOException, SlackApiException {

        // find all channels in the workspace
        ConversationsListResponse conversationsList = SLACK.methods().conversationsList(ConversationsListRequest.builder().token(token).build());
        Assertions.assertThat(conversationsList.isOk()).isTrue();
        // find channelName
        Conversation chann = conversationsList.getChannels().stream()
            .filter(c -> c.getName().equals(channelName)).findFirst().get();

        // fetch history
        ConversationsHistoryResponse history = SLACK.methods().conversationsHistory(ConversationsHistoryRequest.builder()
                .token(token)
                .channel(chann.getId())
                .build());
        Assertions.assertThat(history.isOk()).isTrue();
        log.debug("Slack history fetched successfully...");

        // get last message
        Message lastMessage = history.getMessages().get(0);
        log.info("Last slack message in channel {} is '{}'", channelName, lastMessage.getText());
        Assertions.assertThat(lastMessage.getText()).isEqualToIgnoringCase(expectedMessage);
    }
}
