package io.syndesis.qe.utils;

import static org.assertj.core.api.Assertions.fail;

import io.syndesis.qe.account.Account;
import io.syndesis.qe.account.AccountsDirectory;
import io.syndesis.qe.utils.http.HTTPResponse;
import io.syndesis.qe.utils.http.HTTPUtils;

import java.util.Optional;

public class TelegramUtils {
    private static String SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    private static String RECEIVE_LAST_UNREAD_MESSAGE_URL = "https://api.telegram.org/bot%s/getUpdates?offset=-1";

    public static HTTPResponse sendMessage(String chatId, String text) {
        String apiToken = "";
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.TELEGRAM);

        if (optional.isPresent()) {
            apiToken = optional.get().getProperties().get("authorizationToken");
        } else {
            fail("Api token for Telegram connector not found in credentials.json file!");
        }

        String populatedUrl = String.format(SEND_MESSAGE_URL, apiToken, chatId, text);
        return HTTPUtils.doGetRequest(populatedUrl);
    }

    public static HTTPResponse getUpdates() {
        Optional<Account> optional = AccountsDirectory.getInstance().getAccount(Account.Name.TELEGRAM);
        String apiToken = "";

        if (optional.isPresent()) {
            apiToken = optional.get().getProperties().get("authorizationToken");
        } else {
            fail("Api token for Telegram connector not found in credentials.json file!");
        }

        String populatedUrl = String.format(RECEIVE_LAST_UNREAD_MESSAGE_URL, apiToken);
        return HTTPUtils.doGetRequest(populatedUrl);
    }
}
