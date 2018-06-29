package io.syndesis.qe.utils.telegram;

import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.Map;

import io.syndesis.common.util.Json;
import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import io.syndesis.qe.utils.RestUtils;
import io.syndesis.qe.utils.telegram.wrappers.TelegramAllUpdates;
import io.syndesis.qe.utils.telegram.wrappers.TelegramUpdate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramSimpleManager {

    private static final String TELEGRAM_SEND_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    private static final String TELEGRAM_RECEIVE_URL = "https://api.telegram.org/bot%s/getUpdates";

    private static final String API_TOKEN = TelegramSimpleManager.getApiToken();

    private static final Client client = RestUtils.getInsecureClient();

    public static void sendMessageToChannel(String chatId, String text) {
        sendMessageToChannel(API_TOKEN, chatId, text);
    }

    private static String getApiToken() {
        String token = null;
        Map<String, Account> accounts = AccountsDirectory.getInstance().getAccounts();
        return accounts.get("telegram").getProperty("authorizationToken");
    }

    private static void sendMessageToChannel(String apiToken, String chatId, String text) {

        Invocation.Builder invocation = createSendInvocation(apiToken, chatId, text);

        final Response response = invocation.get();

        log.info("Telegram sending message response: *{}*", response.readEntity(String.class));
    }

    public static boolean validateReceivedMessage(String text, String chatId) {
        return validateReceivedMessage(API_TOKEN, text, chatId);
    }

    private static boolean validateReceivedMessage(String apiToken, String text, String chatId) {
        Invocation.Builder invocation = createReceiveUpdateInvocation(apiToken);

        final JsonNode response = invocation.get(JsonNode.class);

        TelegramAllUpdates ts = transformJsonNode(response, TelegramAllUpdates.class);

        return verifyChatMsgContainsText(ts, text, chatId);
    }

    private static boolean verifyChatMsgContainsText(TelegramAllUpdates allUpdates, String text, String chatId) {
        //
        TelegramUpdate lastTelegramUpdate = allUpdates.getResult().get(allUpdates.getResult().size() - 1);
        String messageText = lastTelegramUpdate.getChannel_post().getText();
        String channelName = lastTelegramUpdate.getChannel_post().getChat().getTitle();

        log.info("Message text: *" + messageText + "* channel name: *" + channelName + "*");
        return (text.equals(messageText) && chatId.equals("@" + channelName));
    }

//    AUXILIARIES:

    private static Invocation.Builder createSendInvocation(String apiToken, String chatId, String text) {
        Invocation.Builder invocation = client.target(createTelegramSendUrl(apiToken, chatId, text)).request(MediaType.TEXT_PLAIN);
        return invocation;
    }

    private static String createTelegramSendUrl(String apiToken, String chatId, String text) {
        return String.format(TELEGRAM_SEND_URL, apiToken, chatId, text);
    }

    //RECEIVE MESSAGE:
    private static Invocation.Builder createReceiveUpdateInvocation(String apiToken) {
        Invocation.Builder invocation = client.target(createTelegramReceiveUrl(apiToken)).request(MediaType.TEXT_PLAIN);
        return invocation;
    }

    private static String createTelegramReceiveUrl(String apiToken) {
        return String.format(TELEGRAM_RECEIVE_URL, apiToken);
    }

    private static TelegramAllUpdates transformJsonNode(JsonNode json, Class<TelegramAllUpdates> t) {
        TelegramAllUpdates tu = null;
        try {
            tu = Json.reader().forType(t).readValue(json.toString());
        } catch (IOException ex) {
            log.error("" + ex);
        }
        return tu;
    }
}
