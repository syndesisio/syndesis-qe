package io.syndesis.qe.utils;

import io.syndesis.qe.accounts.Account;
import io.syndesis.qe.accounts.AccountsDirectory;
import okhttp3.Response;

import java.util.Optional;

import static org.assertj.core.api.Assertions.fail;

public class TelegramUtils {
    private static String SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    private static String RECEIVE_LAST_UNREAD_MESSAGE_URL = "https://api.telegram.org/bot%s/getUpdates?offset=-1";
    public static Response sendMessage(String chatId, String text) {

        String apiToken = "";
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("Telegram");

        if (optional.isPresent()) {
            apiToken = optional.get().getProperties().get("authorizationToken");
        } else {
            fail("Api token for Telegram connector not found in credentials.json file!");
        }

        String populatedUrl = String.format(SEND_MESSAGE_URL, apiToken, chatId, text);
        return HttpUtils.doGetRequest(populatedUrl);
    }

    public static Response getUpdates() {

        Optional<Account> optional = AccountsDirectory.getInstance().getAccount("Telegram");
        String apiToken = "";

        if (optional.isPresent()) {
            apiToken = optional.get().getProperties().get("authorizationToken");
        } else {
            fail("Api token for Telegram connector not found in credentials.json file!");
        }

        String populatedUrl = String.format(RECEIVE_LAST_UNREAD_MESSAGE_URL, apiToken);
        return HttpUtils.doGetRequest(populatedUrl);
    }
}
